package com.nttdata.bootcamp.msmovement.application;

import com.nttdata.bootcamp.msmovement.config.WebClientConfig;
import com.nttdata.bootcamp.msmovement.dto.MovementDto;
import com.nttdata.bootcamp.msmovement.exception.ResourceNotFoundException;
import com.nttdata.bootcamp.msmovement.infrastructure.MovementRepository;
import com.nttdata.bootcamp.msmovement.model.BankAccount;
import com.nttdata.bootcamp.msmovement.model.Credit;
import com.nttdata.bootcamp.msmovement.model.Movement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.Calendar;

@Service
@Slf4j
public class MovementServiceImpl implements MovementService {

    @Autowired
    private MovementRepository movementRepository;

    public Mono<BankAccount> findBankAccountByAccountNumber(String accountNumber) {
        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://localhost:8085").flatMap(
                d -> {
                    return webconfig.getWebclient().get().uri("/api/bankaccounts/accountNumber/" + accountNumber).retrieve().bodyToMono(BankAccount.class);
                }
        );
    }

    @Override
    public Flux<Movement> findAll() {
        return movementRepository.findAll();
    }

    @Override
    public Mono<Movement> findById(String idMovementCredit) {
        return Mono.just(idMovementCredit)
                .flatMap(movementRepository::findById)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Movement", "IdMovement", idMovementCredit)));
    }

    @Override
    public Mono<Movement> save(MovementDto movementDto) {
        return movementDto.validateMovementType()
                .flatMap(a -> findBankAccountByAccountNumber(movementDto.getAccountNumber()))
                .flatMap(account -> validateMovementLimit(account, "save").then(Mono.just(account)))
                .flatMap(account -> validateAvailableAmount(account, movementDto, "save"))
                .flatMap(a -> movementDto.MapperToMovement(null))
                .flatMap(mvt -> movementRepository.save(mvt));
    }

    public Mono<Boolean> validateMovementLimit(BankAccount bankAccount, String method) {
        log.info("ini validateMovementLimit-------: ");


        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        LocalDateTime dateNow = LocalDateTime.now();
        String dayValue = "" + dateNow.getDayOfMonth();
        String monthValue = (dateNow.getMonthValue() < 10 ? "0" : "") + dateNow.getMonthValue();

        String endDate = dateNow.getYear() + "-" + monthValue + "-" + calendar.getActualMaximum(Calendar.DAY_OF_MONTH) + "T23:59:59.999Z";
        String startDate = dateNow.getYear() + "-" + monthValue + "-0" + 1 + "T00:00:00.000Z";

        log.info("ini validateMovementLimit-------bankAccount.getAccountNumber(): " + bankAccount.getAccountNumber());
        log.info("ini validateMovementLimit-------startDate: " + startDate);
        log.info("ini validateMovementLimit-------endDate: " + endDate);

        if (bankAccount.getAccountType().equals("FixedTerm-account")) {
            String movementDate = bankAccount.getMovementDate().toString();
            log.info("ini validateMovementLimit-------movementDate: " + movementDate);
            if (!dayValue.equals(movementDate)) {
                return Mono.error(new ResourceNotFoundException("Fecha movimientos", "movementDate", movementDate));
            }
        }

        if (bankAccount.getMaximumMovement() != null) {
            return movementRepository.findMovementsByDateRange(startDate, endDate, bankAccount.getAccountNumber()).count()
                    .flatMap(c -> {
                        log.info("ini validateMovementLimit-------cantidad: " + c);
                        log.info("ini validateMovementLimit-------cantidad: " + bankAccount.getMaximumMovement());
                        if (c >= bankAccount.getMaximumMovement()) {
                            return Mono.error(new ResourceNotFoundException("Máximo movimientos", "MaximumMovement", bankAccount.getMaximumMovement().toString()));
                        } else {
                            return Mono.just(true);
                        }
                    });
        } else {
            return Mono.just(true);
        }
    }

    @Override
    public Mono<Movement> saveCreditLoan(MovementDto movementDto) {
        return findCreditByCreditNumber(String.valueOf(movementDto.getCreditNumber()))
                .flatMap(credit -> {
                    return movementDto.validateMovementTypeCreditLoan()
                            .flatMap(a -> movementDto.MapperToMovement(credit))
                            .flatMap(mvt -> movementRepository.save(mvt));
                });
    }


    public Mono<Credit> findCreditByCreditNumber(String creditNumber) { //RACH
        log.info("Inicio----findLastMovementByMovementNumber-------: ");
        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://localhost:8084/").flatMap(
                d -> {
                    return webconfig.getWebclient().get().uri("/api/credits/creditNumber/" + creditNumber).retrieve().bodyToMono(Credit.class);
                }
        );
    }

    public Mono<Boolean> validateAvailableAmount(BankAccount bankAccount, MovementDto movementDto, String method) {
        log.info("ini validateAvailableAmount-------: ");
        if (method.equals("save")) {
            return movementRepository.findLastMovementByAccount(movementDto.getAccountNumber())
                    .switchIfEmpty(Mono.defer(() -> {
                        log.info("----1 switchIfEmpty-------: ");
                        return Mono.just(movementDto);
                    }))
                    .flatMap(mvn -> movementDto.validateAvailableAmount(bankAccount, mvn));
        } else {
            log.info("ini validateAvailableAmount-------movementDto.getAccountNumber(): " + movementDto.getAccountNumber());
            log.info("ini validateAvailableAmount-------movementDto.getIdMovement(): " + movementDto.getIdMovement());
            return movementRepository.findLastMovementByAccountExceptCurrentId(movementDto.getAccountNumber(), movementDto.getIdMovement())
                    .switchIfEmpty(Mono.defer(() -> {
                        log.info("----2 switchIfEmpty-------: ");
                        return Mono.just(movementDto);
                    }))
                    .flatMap(mvn -> movementDto.validateAvailableAmount(bankAccount, mvn));
        }
    }

    @Override
    public Mono<Movement> update(MovementDto movementDto, String idMovement) {

        return movementDto.validateMovementType()
                .flatMap(at -> findBankAccountByAccountNumber(movementDto.getAccountNumber()))
                .flatMap(account -> validateAvailableAmount(account, movementDto, "update"))
                .flatMap(a -> movementRepository.findById(idMovement)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Movement", "IdMovement", idMovement)))
                        .flatMap(c -> {
                            //c.setNumberMovement(movementDto.getNumberMovement());
                            c.setAccountNumber(movementDto.getAccountNumber());
                            c.setMovementType(movementDto.getMovementType());
                            c.setAmount(movementDto.getAmount());
                            c.setBalance(movementDto.getBalance());
                            c.setCurrency(movementDto.getCurrency());
                            c.setMovementDate(LocalDateTime.now());
                            //c.setIdCredit(movementDto.getIdCredit());
                            //c.setIdBankAccount(movementDto.getIdBankAccount());
                            //c.setIdLoan(movementDto.getIdLoan());
                            return movementRepository.save(c);
                        })
                );
    }

    @Override
    public Mono<Movement> updateCreditCardLoan(MovementDto movementDto, String idMovement) {
        return findCreditByCreditNumber(String.valueOf(movementDto.getCreditNumber()))
                .flatMap(credit -> {
                    return movementDto.validateMovementTypeCreditLoan()
                            .flatMap(a -> movementRepository.findById(idMovement)
                                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Movement", "IdMovement", idMovement)))
                                    .flatMap(c -> {
                                        c.setCredit(credit);
                                        c.setAccountNumber(movementDto.getAccountNumber());
                                        c.setMovementType(movementDto.getMovementType());
                                        c.setAmount(movementDto.getAmount());
                                        c.setBalance(movementDto.getBalance());
                                        c.setCurrency(movementDto.getCurrency());
                                        c.setMovementDate(LocalDateTime.now());
                                        return movementRepository.save(c);
                                    })
                            );
                });
    }

    @Override
    public Mono<Void> delete(String idMovement) {
        return movementRepository.findById(idMovement)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Movement", "IdMovement", idMovement)))
                .flatMap(movementRepository::delete);
    }

    @Override
    public Mono<MovementDto> findLastMovementsByAccountNumber(String accountNumber) {
        log.info("ini findLastMovementsByAccountNumber-------accountNumber: " + accountNumber);
        return Mono.just(accountNumber)
                .flatMap(movementRepository::findLastMovementByAccount);
    }

    @Override
    public Flux<MovementDto> findMovementsByAccountNumber(String accountNumber) {
        log.info("ini findMovementsByAccountNumber-------accountNumber: " + accountNumber);
        return movementRepository.findMovementsByAccount(accountNumber);
    }

    @Override
    public Mono<Movement> creditByCreditNumber(Integer creditNumber) {
        return Mono.just(creditNumber)
                .flatMap(movementRepository::findByCreditNumber);
        //.switchIfEmpty(Mono.error(new ResourceNotFoundException("Movimiento", "creditNumber", String.valueOf(creditNumber))));
    }


}

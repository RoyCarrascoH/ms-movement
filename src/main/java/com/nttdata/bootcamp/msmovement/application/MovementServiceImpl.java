package com.nttdata.bootcamp.msmovement.application;

import com.nttdata.bootcamp.msmovement.config.WebClientConfig;
import com.nttdata.bootcamp.msmovement.dto.MovementDto;
import com.nttdata.bootcamp.msmovement.exception.ResourceNotFoundException;
import com.nttdata.bootcamp.msmovement.infrastructure.MovementRepository;
import com.nttdata.bootcamp.msmovement.model.BankAccount;
import com.nttdata.bootcamp.msmovement.model.Movement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Service
@Slf4j
public class MovementServiceImpl implements MovementService {

    @Autowired
    private MovementRepository movementRepository;

    public Mono<BankAccount> findBankAccountByAccountNumber(String accountNumber) {
        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://localhost:8090").flatMap(
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
                .flatMap(account -> validateAvailableAmount(account, movementDto, "save"))
                .flatMap(a -> movementDto.MapperToMovement())
                .flatMap(mvt -> movementRepository.save(mvt));

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
                            c.setMovementDate(new Date());
                            //c.setIdCredit(movementDto.getIdCredit());
                            //c.setIdBankAccount(movementDto.getIdBankAccount());
                            //c.setIdLoan(movementDto.getIdLoan());
                            return movementRepository.save(c);
                        })
                );
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
}

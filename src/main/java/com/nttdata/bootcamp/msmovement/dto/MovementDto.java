package com.nttdata.bootcamp.msmovement.dto;

import com.nttdata.bootcamp.msmovement.exception.ResourceNotFoundException;
import com.nttdata.bootcamp.msmovement.model.BankAccount;
import com.nttdata.bootcamp.msmovement.model.Movement;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.bson.codecs.jsr310.LocalDateCodec;
import org.springframework.data.annotation.Id;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@ToString
public class MovementDto {

    @Id
    private String idMovement;

    @NotEmpty(message = "no debe estar vacío")
    private String accountNumber;

    @NotEmpty(message = "no debe estar vacío")
    private String numberDocument;

    //@NotNull(message = "no debe estar nulo")
    //private Integer numberMovement;

    @NotEmpty(message = "no debe estar vacío")
    private String movementType;

    @NotNull(message = "no debe estar nulo")
    private Double amount;

    private Double balance;

    @NotEmpty(message = "no debe estar vacio")
    private String currency;

    public Mono<Boolean> validateMovementType(){
        log.info("ini validateMovementType-------: " );
        return Mono.just(this.getMovementType()).flatMap( ct -> {
            Boolean isOk = false;
            if(this.getMovementType().equals("deposit")){ // deposito.
                isOk = true;
            }
            else if(this.getMovementType().equals("withdrawal")){ // retiro.
                isOk = true;
            }
            else{
                return Mono.error(new ResourceNotFoundException("Tipo movimiento", "getMovementType", this.getMovementType()));
            }
            log.info("fn validateMovementType-------: " );
            return Mono.just(isOk);
        });
    }

    public Mono<Boolean> validateAvailableAmount(BankAccount bankAccount, MovementDto lastMovement) {
        log.info("ini validateMovementType-------: " );
        log.info("ini validateMovementType-------: lastMovement.toString() " + lastMovement.toString());
        return Mono.just(this.getMovementType()).flatMap(ct -> {
            Boolean isOk = false;

            if (lastMovement.getIdMovement() != null) { // Existe almenos un movimiento
                if (this.getMovementType().equals("withdrawal")) {
                    if (lastMovement.getBalance() < this.getAmount()) {
                        return Mono.error(new ResourceNotFoundException("Monto", "Amount", this.getAmount().toString()));
                    }
                    this.setBalance(lastMovement.getBalance() - this.getAmount());
                } else if (this.getMovementType().equals("deposit")) {
                    this.setBalance(lastMovement.getBalance() + this.getAmount());
                } else {
                    return Mono.error(new ResourceNotFoundException("Tipo movimiento", "getMovementType", this.getMovementType()));
                }

            } else { // No tiene movimientos y se usa el monto incial
                if (this.getMovementType().equals("withdrawal")) {

                    if (bankAccount.getStartingAmount() < this.getAmount()) {
                        return Mono.error(new ResourceNotFoundException("Monto", "Amount", this.getAmount().toString()));
                    }
                    this.setBalance(bankAccount.getStartingAmount() - this.getAmount());

                } else if (this.getMovementType().equals("deposit")) {
                    this.setBalance(bankAccount.getStartingAmount() + this.getAmount());
                } else {
                    return Mono.error(new ResourceNotFoundException("Tipo movimiento", "getMovementType", this.getMovementType()));
                }

            }
            return Mono.just(isOk);
        });
    }

    public Mono<Movement> MapperToMovement() {
        LocalDateTime date = LocalDateTime.now();
        log.info("ini validateMovementLimit-------: LocalDateTime.now()" + LocalDateTime.now());
        log.info("ini validateMovementLimit-------date: " + date);

        Movement movement = Movement.builder()
                //.idBankAccount(this.getIdBankAccount())
                .accountNumber(this.getAccountNumber())
                //.numberMovement(this.getNumberMovement())
                .movementType(this.getMovementType())
                .amount(this.getAmount())
                .balance(this.getBalance())
                .currency(this.getCurrency())
                .movementDate(date)
                //.idCredit(this.getIdCredit())
                //.idBankAccount(this.getIdBankAccount())
                //.idLoan(this.getIdLoan())
                .build();

        return Mono.just(movement);
    }
}

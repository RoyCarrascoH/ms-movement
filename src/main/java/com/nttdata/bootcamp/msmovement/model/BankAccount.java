package com.nttdata.bootcamp.msmovement.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class BankAccount {

    @Id
    private String idBankAccount;

    // private Client client;

    @NotEmpty(message = "no debe estar vacío")
    private String accountType;

    private String cardNumber;

    @NotEmpty(message = "no debe estar vacío")
    private String accountNumber;

    private Double commission;

    private Integer movementDate;

    private Integer maximumMovement;

    private Double startingAmount;

    private Double transactionLimit;

    private Double commissionTransaction;

    @NotEmpty(message = "no debe estar vacío")
    private String currency;

}

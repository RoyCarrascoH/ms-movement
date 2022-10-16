package com.nttdata.bootcamp.msmovement.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Document(collection = "Movement")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Movement {

    @Id
    private String idMovement;

    private String accountNumber;

    @NotNull(message = "no debe estar nulo")
    private Integer numberMovement;

    @NotEmpty(message = "no debe estar vacío")
    private String movementType;

    @NotNull(message = "no debe estar nulo")
    private Double amount;

    private Double balance;

    @NotEmpty(message = "no debe estar vacio")
    private String currency;

    private Date movementDate;

    private String idCredit;

    private String idBankAccount;

    private String idLoan;

}
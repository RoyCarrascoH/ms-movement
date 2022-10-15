package com.nttdata.bootcamp.msmovement.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Document(collection = "Movements")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Movement {

    @Id
    private String idMovement;

    @NotNull(message = "no debe estar nulo")
    private Integer numberMovement;

    @NotEmpty(message = "no debe estar vacío")
    private String movementType;

    @NotNull(message = "no debe estar nulo")
    private Double amount;

    @NotEmpty(message = "no debe estar vacio")
    private String currency;

    private Date movementDate;

    private String idCredit;

    private String idBankAccount;

    private String idLoan;

}
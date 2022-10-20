package com.nttdata.bootcamp.msmovement.model;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Credit {

    @Id
    private String idCredit;
    private Client client;
    private Integer creditNumber;
    private String creditType;
    private Double creditLineAmount;
    private String currency;
    private Boolean status;
    private Double balance;

}

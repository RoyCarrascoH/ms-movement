package com.nttdata.bootcamp.msmovement.model;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Client {

    @Id
    private String idClient;
    private String names;
    private String surnames;
    private String clientType;
    private String documentType;
    private String documentNumber;
    private Integer cellphone;
    private String email;
    private Boolean state;

}

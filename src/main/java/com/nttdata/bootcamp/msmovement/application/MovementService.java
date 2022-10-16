package com.nttdata.bootcamp.msmovement.application;

import com.nttdata.bootcamp.msmovement.dto.MovementDto;
import com.nttdata.bootcamp.msmovement.model.Movement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovementService {

    public Flux<Movement> findAll();

    public Mono<Movement> findById(String idMovement);

    public Mono<Movement> save(MovementDto movementDto);

    public Mono<Movement> update(MovementDto movementDto, String idMovement);

    public Mono<Void> delete(String idMovement);
}

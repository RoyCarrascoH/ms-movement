package com.nttdata.bootcamp.msmovement.application;

import com.nttdata.bootcamp.msmovement.model.Movement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovementService {

    public Flux<Movement> findAll();

    public Mono<Movement> findById(String idMovement);

    public Mono<Movement> save(Movement Movement);

    public Mono<Movement> update(Movement Movement, String idMovement);

    public Mono<Void> delete(String idMovement);
}

package com.nttdata.bootcamp.msmovement.infrastructure;

import com.nttdata.bootcamp.msmovement.dto.MovementDto;
import com.nttdata.bootcamp.msmovement.model.Movement;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovementRepository extends ReactiveMongoRepository<Movement, String> {
    @Aggregation(pipeline = {"{ '$match': { 'accountNumber' : ?0 } }","{ '$sort' : { 'movementDate' : -1 } }","{'$limit': 1}"})
    Mono<MovementDto> findLastMovementByAccount(String accountNumber) ;

    @Aggregation(pipeline = {"{ '$match': { 'accountNumber' : ?0, 'idMovement' : { $ne: ?1 } } }","{ '$sort' : { 'movementDate' : -1 } }","{'$limit': 1}"})
    Mono<MovementDto> findLastMovementByAccountExceptCurrentId(String accountNumber, String idMovement) ;

    @Aggregation(pipeline = {"{ '$match': { 'accountNumber' : ?0 } }","{ '$sort' : { 'movementDate' : -1 } }"})
    Flux<MovementDto> findMovementsByAccount(String accountNumber) ;

    @Query(value = "{'credit.creditNumber' : ?0}")
    public Mono<Movement> findByCreditNumber(Integer creditNumber);

}
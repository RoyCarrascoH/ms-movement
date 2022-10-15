package com.nttdata.bootcamp.msmovement.infrastructure;

import com.nttdata.bootcamp.msmovement.model.Movement;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MovementRepository extends ReactiveMongoRepository<Movement, String> {

}
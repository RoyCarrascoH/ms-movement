package com.nttdata.bootcamp.msmovement.application;

import com.nttdata.bootcamp.msmovement.exception.ResourceNotFoundException;
import com.nttdata.bootcamp.msmovement.infrastructure.MovementRepository;
import com.nttdata.bootcamp.msmovement.model.Movement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MovementServiceImpl implements MovementService {

    @Autowired
    private MovementRepository movementRepository;

    @Override
    public Flux<Movement> findAll() {
        return movementRepository.findAll();
    }

    @Override
    public Mono<Movement> findById(String idMovementCredit) {
        return Mono.just(idMovementCredit)
                .flatMap(movementRepository::findById)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("MovementCredit", "IdMovementCredit", idMovementCredit)));
    }

    @Override
    public Mono<Movement> save(Movement Movement) {
        return movementRepository.save(Movement);
    }

    @Override
    public Mono<Movement> update(Movement Movement, String idMovement) {
        return movementRepository.findById(idMovement)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Movement", "IdMovement", idMovement)))
                .flatMap(c -> {
                    c.setNumberMovement(Movement.getNumberMovement());
                    c.setMovementType(Movement.getMovementType());
                    c.setAmount(Movement.getAmount());
                    c.setCurrency(Movement.getCurrency());
                    c.setMovementDate(Movement.getMovementDate());
                    c.setIdCredit(Movement.getIdCredit());
                    c.setIdBankAccount(Movement.getIdBankAccount());
                    c.setIdLoan(Movement.getIdLoan());
                    return movementRepository.save(c);
                });
    }

    @Override
    public Mono<Void> delete(String idMovement) {
        return movementRepository.findById(idMovement)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Movement", "IdMovement", idMovement)))
                .flatMap(movementRepository::delete);
    }

}

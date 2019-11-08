package com.hyu.my.service;

import com.hyu.my.model.Vehicle;
import com.hyu.my.repo.VehicleRepo;
import com.hyu.my.state.Events;
import com.hyu.my.config.PersistStateMachineHandler;
import com.hyu.my.state.States;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {

    @Autowired
    VehicleRepo vehicleRepo;

    private PersistStateMachineHandler handler;

    public VehicleService(PersistStateMachineHandler handler) {
        this.handler = handler;
    }

    public Vehicle newVehicle(Vehicle vehicle){
        vehicle.setState(States.START);
        vehicleRepo.save(vehicle);
        return vehicle;
    }

    public boolean change(Integer vehicleId,Events event) {
        Vehicle vehicle = vehicleRepo.findById(vehicleId).orElse(null);
        if(null != vehicle){
            return handler.handleEventWithState(
                    MessageBuilder.withPayload(event)
                            .setHeader("vehicleId", vehicleId)
                            .setHeader("vehicle",vehicle)
                            .build(),
                    vehicle.getState());
        }
        return false;
    }
}

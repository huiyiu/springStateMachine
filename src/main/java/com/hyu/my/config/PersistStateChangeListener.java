package com.hyu.my.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

import com.hyu.my.model.Vehicle;
import com.hyu.my.repo.LogRepo;
import com.hyu.my.repo.VehicleRepo;
import com.hyu.my.state.Events;
import com.hyu.my.state.States;

public class PersistStateChangeListener implements PersistStateMachineHandler.PersistStateChangeListener {

    @Autowired
    VehicleRepo vehicleRepo;
    @Autowired
    LogRepo logRepo;

    @Override
    public void onPersist(State<States, Events> state, Message<Events> message, Transition<States, Events> transition,
            StateMachine<States, Events> stateMachine) {
        /**
         * 更新状态
         */
        if (message != null && message.getHeaders().containsKey("vehicle")) {
            Vehicle vehicle = message.getHeaders().get("vehicle", Vehicle.class);
            vehicle.setState(state.getId());
            vehicleRepo.save(vehicle);
        }
    }
}

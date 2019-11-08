package com.hyu.my.config;

import com.hyu.my.service.VehicleService;
import com.hyu.my.state.Events;
import com.hyu.my.state.States;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;

@Configuration
public class PersistHandlerConfig {

    @Autowired
    StateMachine<States,Events> stateMachine;

    @Bean
    PersistStateMachineHandler persistStateMachineHandler(){
        return new PersistStateMachineHandler(stateMachine);
    }

    @Bean
    public PersistStateChangeListener persistStateChangeListener(){
        return new PersistStateChangeListener();
    }

    @Bean
    public VehicleService persist() {
        PersistStateMachineHandler handler = persistStateMachineHandler();
        handler.addPersistStateChangeListener(persistStateChangeListener());
        return new VehicleService(handler);
    }

}

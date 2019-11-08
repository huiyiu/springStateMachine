package com.hyu.my.config;

import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;

@WithStateMachine
        //(id="vehicleMachine")
public class VehicleEventConfig {

    @OnTransition(source = "TRAFFICCONFIRMED",target = "JONED")
    public void state1(){
        System.out.println(String.format("state from %s to %s.","TRAFFICCONFIRMED","JONED"));
    }


    @OnTransition(source = "COMPARED",target = "PHYSICALED")
    public void state2(){
        System.out.println(String.format("state from %s to %s.","COMPARED","PHYSICALED"));
    }

    @OnTransition(source = "COMPARED",target = "RELEASECONFIRMED")
    public void state3(){
        System.out.println(String.format("state from %s to %s.","COMPARED","RELEASECONFIRMED"));
    }

    @OnTransition(source = "RELEASECONFIRMED",target = "LOGICSED")
    public void state4(){
        System.out.println(String.format("state from %s to %s.","RELEASECONFIRMED","LOGICSED"));
    }

    @OnTransition(source = "RELEASECONFIRMED",target = "AUTORELEASED")
    public void state5(){
        System.out.println(String.format("state from %s to %s.","RELEASECONFIRMED","AUTORELEASED"));
    }
}

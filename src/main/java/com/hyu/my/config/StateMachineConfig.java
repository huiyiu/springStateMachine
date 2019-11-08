package com.hyu.my.config;

import com.hyu.my.model.Vehicle;
import com.hyu.my.state.Events;
import com.hyu.my.state.States;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Optional;

@Configuration
@EnableStateMachine
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

    /**
     * ss
     * 
     * @param config
     * @throws Exception
     */
    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception {
         config
                .withConfiguration()
                 //.machineId("vehicleMachine")
                .autoStartup(true).listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
        //v1 choice
       /* states.withStates()
                .initial(States.START)
                .choice(States.RELEASECONFIRMED)
                .states(EnumSet.allOf(States.class));*/

       // v2 fork
        states.withStates()
                .initial(States.START)
                .choice(States.CHECKINED)
                .choice(States.JOINED)
                .choice(States.COMPARED)
                .choice(States.PHYSICALED)
                .choice(States.RELEASECONFIRMED)
                .end(States.END)
                .states(EnumSet.allOf(States.class));

    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
        // v1   choice
       /* transitions
                .withExternal().source(States.START).target(States.CHECKINED).event(Events.CHECKIN)
                .and()
                .withExternal().source(States.CHECKINED).target(States.MEASURED).event(Events.MEASURE)
                .and()
                .withExternal().source(States.TRAFFICCONFIRMED).target(States.MEASURED).event(Events.REMEASURE)
                .and()
                .withExternal().source(States.MEASURED).target(States.TRAFFICCONFIRMED).event(Events.TRAFFICCONFIRM)
                .and()
                .withExternal().source(States.TRAFFICCONFIRMED).target(States.JOINED).event(Events.JOIN)
                .and()
                .withExternal().source(States.JOINED).target(States.COMPARED).event(Events.COMPARE)
                .and()
                .withExternal().source(States.COMPARED).target(States.RELEASECONFIRMED).event(Events.WUXIANYI)
                .and()
                .withExternal().source(States.COMPARED).target(States.PHYSICALED).event(Events.YOUXIANYI)
                .and()
                .withExternal().source(States.COMPARED).target(States.PHYSICALED).event(Events.REDRISK)
                .and()
                .withExternal().source(States.PHYSICALED).target(States.RELEASECONFIRMED).event(Events.RELEASECONFIRM)
                .and()
                .withChoice().source(States.RELEASECONFIRMED).first(
                        States.AUTORELEASED,c ->
                             c.getMessageHeaders().containsKey("conclusion") && c.getMessageHeaders().containsValue("no")
                        )
                .last(States.LOGICSED)
                .and()
                .withExternal().source(States.LOGICSED).target(States.AUTORELEASED).event(Events.AUTORELEAS)
        ;*/
       //v2 fork
        //@formatter:off
        transitions.withChoice()
                    .source(States.CHECKINED)
                    //出境大巴
                    .first(States.AUTORELEASED, busExportGuard())
                    //入境大巴
                    .then(States.MANUALRELEASED,busImportGuard())
                    //出入境超限货车
                    .then(States.JOINED,overCarGuard())
                    .last(States.MEASURED)
                .and()
                .withChoice()
                    .source(States.JOINED)
                    //入境超限，人工放行
                    .first(States.MANUALRELEASED,importOverCarGuard())
                    .last(States.COMPARED)
                .and()
                .withChoice()
                    .source(States.PHYSICALED)
                //空车自助放行
                    .first(States.AUTORELEASED,emptyAutoReleaseGuard())
                // //其他放行确认
                    .last(States.RELEASECONFIRMED)
                .and()
                .withChoice()
                    .source(States.COMPARED)
                    //有嫌疑手检
                    .first(States.PHYSICALED, supiciousToPhusicalGuard())
                    // 无嫌疑空车 边防 到 人工放行
                    .then(States.MANUALRELEASED,emptyPassToManualReleaseGuard())
                    //无嫌疑空车 到 自助放行
                    .then(States.AUTORELEASED,emptyPassToAutoReleaseGuard())
                    .last(States.RELEASECONFIRMED)
                .and()
                .withChoice()
                    .source(States.RELEASECONFIRMED)
                    .first(States.AUTORELEASED,autoReleaseGuard())
                    .last(States.LOGICSED)
                .and()
                .withExternal().source(States.START).target(States.CHECKINED).event(Events.CHECKIN)
                .and()
                .withExternal().source(States.LOGICSED).target(States.AUTORELEASED).event(Events.AUTORELEAS)
                .and()
                .withExternal().source(States.MEASURED).target(States.TRAFFICCONFIRMED).event(Events.TRAFFICCONFIRM)
                .and()
                .withExternal().source(States.TRAFFICCONFIRMED).target(States.MEASURED).event(Events.REMEASURE)
                .and()
                .withExternal().source(States.TRAFFICCONFIRMED).target(States.JOINED).event(Events.JOIN).action(trafficToJoinedAction(),errorAction())
                .and().withExternal().source(States.AUTORELEASED).target(States.END).event(Events.END)

                .and().withExternal().source(States.MANUALRELEASED).target(States.END).event(Events.END)

        ;

    }

    @Bean
    public Guard<States, Events> busExportGuard() {
        return c-> {
            Vehicle vehicle = getVehicle(c);
            return vehicle !=null &&"EXPORT".equalsIgnoreCase(vehicle.getBusinessType())
                        &&"bus". equalsIgnoreCase(vehicle.getType());
        };
    }

    @Bean
    public Guard<States, Events> busImportGuard() {
        return c-> {
            Vehicle vehicle = getVehicle(c);
            return vehicle !=null &&"IMPORT".equalsIgnoreCase(vehicle.getBusinessType())
                    &&"bus". equalsIgnoreCase(vehicle.getType());
        };
    }

    @Bean
    public Guard<States, Events> overCarGuard() {
        return c-> {
            Vehicle vehicle = getVehicle(c);
            return vehicle !=null && "overCar". equalsIgnoreCase(vehicle.getType());
        };
    }

    @Bean
    public Guard<States, Events> importOverCarGuard() {
        return c-> {
            Vehicle vehicle = getVehicle(c);
            return vehicle !=null && "overCar". equalsIgnoreCase(vehicle.getType())
                    && "IMPORT". equalsIgnoreCase(vehicle.getBusinessType());
        };
    }

    @Bean
    public Guard<States, Events> emptyAutoReleaseGuard() {
        return c-> {
            Vehicle vehicle = getVehicle(c);
            return vehicle !=null && "emptyCar". equalsIgnoreCase(vehicle.getType());
        };
    }

    @Bean
    public Guard<States, Events> supiciousToPhusicalGuard() {
        return c-> {
            Vehicle vehicle = getVehicle(c);
            return vehicle !=null && "supicious".equalsIgnoreCase(vehicle.getRiskType());
        };
    }

    // 无嫌疑空车 边防 到 人工放行
    @Bean
    public Guard<States, Events> emptyPassToManualReleaseGuard() {
        return c-> {
            Vehicle vehicle = getVehicle(c);
            return vehicle !=null && "emptyCar". equalsIgnoreCase(vehicle.getType())
                    && "pass". equalsIgnoreCase(vehicle.getRiskType())
                    && "no". equalsIgnoreCase(vehicle.getLogistics())
                    && "IMPORT".equalsIgnoreCase(vehicle.getBusinessType())
                    ;
        };
    }

    ////无嫌疑空车 到 自助放行
    public Guard<States, Events> emptyPassToAutoReleaseGuard() {
        return c-> {
            Vehicle vehicle = getVehicle(c);
            return vehicle !=null && "emptyCar". equalsIgnoreCase(vehicle.getType())
                    && "pass". equalsIgnoreCase(vehicle.getRiskType())
                    && ("IMPORT". equalsIgnoreCase(vehicle.getBusinessType()) && "yes". equalsIgnoreCase(vehicle.getLogistics()))
                    ||
                    ( "no". equalsIgnoreCase(vehicle.getLogistics()) && "EXPORT". equalsIgnoreCase(vehicle.getBusinessType()))
                    ;
        };
    }

    @Bean
    public Guard<States, Events> autoReleaseGuard() {
        return c-> {
            Vehicle vehicle = getVehicle(c);
            return vehicle !=null
                    && "no". equalsIgnoreCase(vehicle.getLogistics())
                    ;
        };
    }


    /**
     * 从message获取车辆信息
     * @param context
     * @return
     */
    private Vehicle getVehicle(StateContext<States, Events> context){
        if(null == context){
            return null;
        }
        Vehicle vehicle = null;
        if(context.getMessageHeaders().containsKey("vehicleId") && context.getMessageHeaders().containsKey("vehicle")){
            vehicle = context.getMessageHeaders().get("vehicle", Vehicle.class);
        }
        return vehicle;
    }

    @Bean
    public StateMachineListener<States, Events> listener() {
        return new StateMachineListenerAdapter<States, Events>() {

            @Override
            public void stateChanged(State<States, Events> from, State<States, Events> to) {
                System.out.println(String.format("states  %s -> %s.",Optional.ofNullable(from).map(State::getId).orElse(null),to.getId()));
            }
        };

        /*return new StateMachineListenerAdapter<States, Events>() {
            @Override
            public void transition(Transition<States, Events> transition) {

            }
        };*/
    }

    /**
     * action
     * @return
     */
    @Bean
    public Action<States, Events> trafficToJoinedAction() {
        return c->{
            Vehicle vehicle = getVehicle(c);
            System.out.println(String.format("do some action from %s to %s(vehicleId=%s)",c.getSource().getId(),c.getTarget().getId(),vehicle.getId()));
        };
    }

    /**
     * action 错误处理
     * @return
     */
    @Bean
    public Action<States, Events> errorAction() {
        return c->{
            Exception exception = c.getException();
            exception.getMessage();
        };
    }

}
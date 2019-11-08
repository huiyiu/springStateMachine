package com.hyu.my.config;

import java.util.Iterator;
import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.listener.AbstractCompositeListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.LifecycleObjectSupport;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.util.Assert;

import com.hyu.my.state.Events;
import com.hyu.my.state.States;

public class PersistStateMachineHandler extends LifecycleObjectSupport {

    private final StateMachine<States, Events> stateMachine;
    private final PersistingStateChangeInterceptor interceptor = new PersistingStateChangeInterceptor();
    private final CompositePersistStateChangeListener listeners = new CompositePersistStateChangeListener();

    public PersistStateMachineHandler(StateMachine<States, Events> stateMachine) {
        Assert.notNull(stateMachine, "State machine must be set");
        this.stateMachine = stateMachine;
    }

    /**
     * 注册拦截器
     * 
     * @throws Exception
     */
    @Override
    protected void onInit() throws Exception {
        super.onInit();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(stateMachineFunction -> stateMachineFunction.addStateMachineInterceptor(interceptor));
    }

    /**
     * 添加listener
     *
     * @param listener
     *        the listener
     */
    public void addPersistStateChangeListener(PersistStateChangeListener listener) {
        listeners.register(listener);
    }

    /**
     * 状态变更触发事件，状态机重新重启
     *
     * @param event
     * @param state
     * @return 如果事件被接受处理，返回true
     */
    public boolean handleEventWithState(Message<Events> event, States state) {
        stateMachine.stop();
        List<StateMachineAccess<States, Events>> withAllRegions = stateMachine.getStateMachineAccessor()
                .withAllRegions();
        for (StateMachineAccess<States, Events> a : withAllRegions) {
            a.resetStateMachine(new DefaultStateMachineContext<>(state, null, null, null));
        }
        stateMachine.start();
        return stateMachine.sendEvent(event);
    }

    public interface PersistStateChangeListener {

        /**
         * 当状态被持久化，调用此方法
         *
         * @param state
         * @param message
         * @param transition
         * @param stateMachine
         *        状态机实例
         */
        void onPersist(State<States, Events> state, Message<Events> message, Transition<States, Events> transition,
                StateMachine<States, Events> stateMachine);
    }

    private class CompositePersistStateChangeListener extends AbstractCompositeListener<PersistStateChangeListener>
            implements PersistStateChangeListener {

        @Override
        public void onPersist(State<States, Events> state, Message<Events> message,
                Transition<States, Events> transition, StateMachine<States, Events> stateMachine) {
            for (Iterator<PersistStateChangeListener> iterator = getListeners().reverse(); iterator.hasNext();) {
                PersistStateChangeListener listener = iterator.next();
                listener.onPersist(state, message, transition, stateMachine);
            }
        }
    }

    private class PersistingStateChangeInterceptor extends StateMachineInterceptorAdapter<States, Events> {

        @Override
        public void preStateChange(State<States, Events> state, Message<Events> message,
                Transition<States, Events> transition, StateMachine<States, Events> stateMachine) {
            listeners.onPersist(state, message, transition, stateMachine);
        }
    }
}

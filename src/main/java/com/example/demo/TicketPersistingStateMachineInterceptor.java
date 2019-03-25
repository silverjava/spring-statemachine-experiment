package com.example.demo;

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.persist.AbstractPersistingStateMachineInterceptor;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.support.StateMachineInterceptor;

public class TicketPersistingStateMachineInterceptor
        extends AbstractPersistingStateMachineInterceptor<TicketState, TicketEvent, String>
        implements StateMachineRuntimePersister<TicketState, TicketEvent, String> {

    private TicketStateMachinePersist persist;

    public TicketPersistingStateMachineInterceptor(TicketStateMachinePersist ticketStateMachinePersist) {
        this.persist = ticketStateMachinePersist;
    }

    @Override
    public void write(StateMachineContext<TicketState, TicketEvent> context, String contextObj) {
        persist.write(context, contextObj);
    }

    @Override
    public StateMachineContext<TicketState, TicketEvent> read(String contextObj) {
        return persist.read(contextObj);
    }

    @Override
    public StateMachineInterceptor<TicketState, TicketEvent> getInterceptor() {
        return this;
    }
}

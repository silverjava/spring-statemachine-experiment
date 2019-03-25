package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.statemachine.service.StateMachineService;

import java.util.EnumSet;

import static com.example.demo.TicketEvent.DELIVER;
import static com.example.demo.TicketEvent.PAY;
import static com.example.demo.TicketState.*;

@Configuration
public class StateMachineConfig {

    @Configuration
    public static class TicketPersisterConfig {
        @Bean
        public StateMachineRuntimePersister<TicketState, TicketEvent, String> stateMachineRuntimePersister(
                TicketStateMachinePersist ticketStateMachinePersist) {
            return new TicketPersistingStateMachineInterceptor(ticketStateMachinePersist);
        }
    }

    @Configuration
    @EnableStateMachineFactory
    public static class MachineConfig extends EnumStateMachineConfigurerAdapter<TicketState, TicketEvent> {
        @Autowired
        private StateMachineRuntimePersister<TicketState, TicketEvent, String> stateMachinePersister;

        @Override
        public void configure(StateMachineConfigurationConfigurer<TicketState, TicketEvent> config) throws Exception {
            config.withPersistence()
                    .runtimePersister(stateMachinePersister);
            config.withConfiguration()
                    .autoStartup(false);
        }

        @Override
        public void configure(StateMachineStateConfigurer<TicketState, TicketEvent> states) throws Exception {
            states.withStates()
                    .initial(FREE)
                    .states(EnumSet.allOf(TicketState.class));
        }

        @Override
        public void configure(StateMachineTransitionConfigurer<TicketState, TicketEvent> transitions) throws Exception {
            transitions
                .withExternal()
                    .source(FREE).target(PAID).event(PAY).and()
                .withExternal()
                    .source(PAID).target(DELIVERED).event(DELIVER);
        }
    }

    @Configuration
    public static class ServiceConfig {
        @Bean
        public StateMachineService<TicketState, TicketEvent> stateMachineService(
                StateMachineFactory<TicketState, TicketEvent> stateMachineFactory,
                StateMachineRuntimePersister<TicketState, TicketEvent, String> stateMachineRuntimePersister) {
            return new DefaultStateMachineService<>(stateMachineFactory, stateMachineRuntimePersister);
        }
    }
}

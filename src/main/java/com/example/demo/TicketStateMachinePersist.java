package com.example.demo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.example.demo.entites.TicketStateMachineContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.kryo.MessageHeadersSerializer;
import org.springframework.statemachine.kryo.StateMachineContextSerializer;
import org.springframework.statemachine.kryo.UUIDSerializer;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class TicketStateMachinePersist implements StateMachinePersist<TicketState, TicketEvent, String> {

    private TicketStateMachineContextRepository repository;

    public TicketStateMachinePersist(TicketStateMachineContextRepository repository) {
        this.repository = repository;
    }

    @Override
    public void write(StateMachineContext<TicketState, TicketEvent> context, String contextObj) {
        log.info("start to write to database {} {} {}", context, serialize(context).length(), contextObj);

        Optional<TicketStateMachineContext> machineContext = repository.findByTicketId(Long.valueOf(contextObj));
        if (machineContext.isPresent()) {
            machineContext.ifPresent(ctx -> {
                ctx.setCurState(context.getState().name());
                ctx.setContext(serialize(context));
                repository.save(ctx);
            });
        } else {
            repository.save(TicketStateMachineContext
                                .builder()
                                .ticketId(Long.valueOf(context.getId()))
                                .context(serialize(context))
                                .curState(context.getState().name())
                                .build());
        }
    }

    @Override
    public StateMachineContext<TicketState, TicketEvent> read(String contextObj) {
        log.info("start to read from database {}", contextObj);
        return repository
                .findByTicketId(Long.valueOf(contextObj))
                .map(context -> {
                    StateMachineContext<TicketState, TicketEvent> stateMachineContext = deserialize(context.getContext());
                    log.info("read the current state {} ", stateMachineContext.getState().name());
                    return stateMachineContext;
                })
                .orElse(null);
    }

    private String serialize(StateMachineContext<TicketState, TicketEvent> context) {
        return Optional.ofNullable(context).map(ctx -> {
            Kryo kryo = kryoThreadLocal.get();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Output output = new Output(outputStream);
            kryo.writeObject(output, context);
            byte[] bytes = output.toBytes();
            output.flush();
            output.close();
            return Base64.getEncoder().encodeToString(bytes);
        }).orElse(null);
    }

    @SuppressWarnings("unchecked")
    private StateMachineContext<TicketState, TicketEvent> deserialize(String data) {
        return Optional.ofNullable(data).map(str -> {
            Kryo kryo = kryoThreadLocal.get();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            Input input = new Input(inputStream);
            return kryo.readObject(input, StateMachineContext.class);
        }).orElse(null);
    }

    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.addDefaultSerializer(StateMachineContext.class, new StateMachineContextSerializer());
        kryo.addDefaultSerializer(MessageHeaders.class, new MessageHeadersSerializer());
        kryo.addDefaultSerializer(UUID.class, new UUIDSerializer());
        return kryo;
    });
}

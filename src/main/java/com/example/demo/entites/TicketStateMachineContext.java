package com.example.demo.entites;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ticket_state_machine_context")
@Builder
public class TicketStateMachineContext {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private Long ticketId;
    private String curState;
    private String context;
}

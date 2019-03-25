package com.example.demo;

import com.example.demo.entites.Ticket;
import com.example.demo.entites.TicketStateMachineContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.http.ResponseEntity.*;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

//	@Bean
//	public ApplicationRunner runner(
//			StateMachineService<TicketState, TicketEvent> stateMachineService,
//			TicketRepository ticketRepository) {
//		return args -> {
////			ticketRepository.findById(2L).ifPresent(ticket -> {
////				stateMachineService.acquireStateMachine(String.valueOf(ticket.getId()));
////			});
//		};
//	}
}

interface TicketStateMachineContextRepository extends JpaRepository<TicketStateMachineContext, Long> {
	Optional<TicketStateMachineContext> findByTicketId(Long ticketId);
}

interface TicketRepository extends JpaRepository<Ticket, Long> {}

@RestController
@Slf4j
class TicketController {

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private TicketStateMachineContextRepository ticketStateMachineContextRepository;

	@Autowired
	private StateMachineService<TicketState, TicketEvent> stateMachineService;

	@PostMapping(value = "/tickets")
	public ResponseEntity<Ticket> create(@RequestBody Ticket ticket) {
		return ok(ticketRepository.save(ticket));
	}

	@GetMapping(value = "/tickets/{ticketId}")
	public ResponseEntity<TicketViewModel> ticketById(@PathVariable Long ticketId) {
		return ticketRepository
					.findById(ticketId)
					.map(ticket -> TicketViewModel.builder().ticket(ticket))
					.flatMap(builder ->
						ticketStateMachineContextRepository
								.findByTicketId(ticketId)
								.map(builder::context)
								.map(TicketViewModel.TicketViewModelBuilder::build))
					.map(ResponseEntity::ok)
					.orElse(notFound().build());
	}

	@GetMapping(value = "/tickets/{ticketId}/{event}")
	public ResponseEntity<Ticket> changeEvent(@PathVariable Long ticketId, @PathVariable String event) {
		String machineId = String.valueOf(ticketId);
		StateMachine<TicketState, TicketEvent> sm = stateMachineService.acquireStateMachine(machineId);
		log.info("ticketId {}, event {}", ticketId, event);
		log.info("event {}", sm.getState().getId().name());
		if (sm.sendEvent(TicketEvent.valueOf(event))) {
			log.info("event sent {}", TicketEvent.valueOf(event));
			return ticketRepository.findById(ticketId).map(ResponseEntity::ok).orElse(badRequest().build());
		}
		return badRequest().build();
	}

	@GetMapping(value = "/statemachines/{machineId}/acquire")
	public ResponseEntity<String> acquireStateMachine(@PathVariable String machineId) {
		stateMachineService.acquireStateMachine(machineId);
		return ok("success");
	}

	@GetMapping(value = "/statemachines/{machineId}/release")
	public ResponseEntity<String> releaseStateMachine(@PathVariable String machineId) {
		stateMachineService.releaseStateMachine(machineId);
		return ok("success");
	}
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TicketViewModel {
	private Ticket ticket;
	private TicketStateMachineContext context;
}


enum TicketState {
	FREE,
	PAID,
	DELIVERED
}

enum TicketEvent {
	PAY,
	DELIVER
}

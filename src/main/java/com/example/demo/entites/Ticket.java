package com.example.demo.entites;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tickets")
@Builder
public class Ticket {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String name;
}

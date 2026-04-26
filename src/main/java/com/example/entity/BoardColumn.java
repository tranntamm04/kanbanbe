package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "columns")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class BoardColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer position;

    @ManyToOne(optional = false)
    private Project project;
}
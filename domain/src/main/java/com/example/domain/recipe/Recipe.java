package com.example.domain.recipe;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "recipes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "ingredients")
    private String ingredients;

    @Column(name = "cookingTime")
    private int cookingTime;
}

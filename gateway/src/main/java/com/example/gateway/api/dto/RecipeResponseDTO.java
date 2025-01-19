package com.example.gateway.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeResponseDTO {
    private String id;
    private String name;
    private String ingredients;
    private int cookingTime;
}

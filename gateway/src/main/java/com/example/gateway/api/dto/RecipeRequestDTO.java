package com.example.gateway.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeRequestDTO {
    private String id;
    private String name;
    private String ingredients;
    private int cookingTime;
}

package com.example.domain.service;

import com.example.domain.recipe.Recipe;
import com.example.domain.recipe.RecipeRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RecipeService {

    private final RecipeRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public RecipeService(RecipeRepository repository, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<Recipe> getAllRecipes() {
        return repository.findAll();
    }

    public Recipe getRecipeById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found with ID: " + id));
    }

    public void createRecipe(Recipe recipe) {
        validateRecipe(recipe);
        rabbitTemplate.convertAndSend("recipeExchange", "recipe.post", recipe);
    }

    public void updateRecipe(UUID id, Recipe recipe) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Recipe not found with ID: " + id);
        }
        validateRecipe(recipe);
        recipe.setId(id); // Устанавливаем ID перед отправкой
        rabbitTemplate.convertAndSend("recipeExchange", "recipe.put", recipe);
    }

    public void deleteRecipe(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Recipe not found with ID: " + id);
        }
        rabbitTemplate.convertAndSend("recipeExchange", "recipe.delete", id.toString());
    }

    private void validateRecipe(Recipe recipe) {
        if (recipe.getName() == null || recipe.getName().isEmpty()) {
            throw new IllegalArgumentException("Recipe name cannot be null or empty");
        }
        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("Ingredients cannot be null or empty");
        }
        if (recipe.getCookingTime() <= 0) {
            throw new IllegalArgumentException("Cooking time must be greater than zero");
        }
    }
}

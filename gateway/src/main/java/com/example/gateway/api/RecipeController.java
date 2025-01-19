package com.example.gateway.api;

import com.example.gateway.api.dto.RecipeRequestDTO;
import com.example.gateway.api.dto.RecipeResponseDTO;
import com.example.gateway.service.RecipeService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
@Slf4j
public class RecipeController {

    private final RecipeService recipeService;

    @Timed(value = "gateway.getAllRecipes", description = "Time taken to fetch all recipes")
    @GetMapping
    public ResponseEntity<List<RecipeResponseDTO>> getAllRecipes() {
        log.info("Received request to fetch all recipes");
        List<RecipeResponseDTO> recipes = recipeService.getAllRecipes();
        log.info("Returning {} recipes", recipes.size());
        return ResponseEntity.ok(recipes);
    }

    @Timed(value = "gateway.getRecipeById", description = "Time taken to fetch recipe by ID")
    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponseDTO> getRecipeById(@PathVariable String id) {
        log.info("Received request to fetch recipe by ID: {}", id);
        RecipeResponseDTO recipe = recipeService.getRecipeById(id);
        log.info("Returning recipe: {}", recipe);
        return ResponseEntity.ok(recipe);
    }

    @Timed(value = "gateway.createRecipe", description = "Time taken to create a recipe")
    @PostMapping
    public ResponseEntity<?> createRecipe(@RequestBody RecipeRequestDTO recipeRequestDTO) {
        log.info("Received request to create new recipe: {}", recipeRequestDTO);
        try {
            recipeService.saveNewRecipe(recipeRequestDTO);
            log.info("Recipe successfully created: {}", recipeRequestDTO);
            return ResponseEntity.ok("Created");
        } catch (Exception e) {
            log.error("Error occurred while creating recipe: {}", recipeRequestDTO, e);
            return ResponseEntity.status(500).body(e.getLocalizedMessage());
        }
    }

    @Timed(value = "gateway.updateRecipe", description = "Time taken to update a recipe")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecipe(@PathVariable String id, @RequestBody RecipeRequestDTO recipeRequestDTO) {
        log.info("Received request to update recipe with ID: {}", id);
        try {
            recipeService.updateRecipe(id, recipeRequestDTO);
            log.info("Recipe with ID {} successfully updated.", id);
            return ResponseEntity.ok("Updated");
        } catch (Exception e) {
            log.error("Error occurred while updating recipe with ID {}: {}", id, e);
            return ResponseEntity.status(500).body(e.getLocalizedMessage());
        }
    }

    @Timed(value = "gateway.deleteRecipe", description = "Time taken to delete a recipe")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable String id) {
        log.info("Received request to delete recipe with ID: {}", id);
        try {
            recipeService.deleteRecipe(id);
            log.info("Recipe with ID {} successfully deleted.", id);
            return ResponseEntity.ok("Deleted");
        } catch (Exception e) {
            log.error("Error occurred while deleting recipe with ID {}: {}", id, e);
            return ResponseEntity.status(500).body(e.getLocalizedMessage());
        }
    }
}

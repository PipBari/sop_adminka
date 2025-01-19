package com.example.domain.service;

import com.example.domain.recipe.Recipe;
import com.example.domain.recipe.RecipeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.example.domain.config.RabbitMQConfig.*;

@Service
@Slf4j
public class RecipeExchangeListener {

    private final RecipeRepository recipeRepository;
    private final ObjectMapper objectMapper;

    public RecipeExchangeListener(RecipeRepository recipeRepository, ObjectMapper objectMapper) {
        this.recipeRepository = recipeRepository;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = recipePostRequestQueueName)
    public void onPostMethod(byte[] message) {
        try {
            log.info("Received POST request: {}", new String(message));
            Recipe recipe = objectMapper.readValue(message, Recipe.class);
            recipeRepository.save(recipe);
            log.info("Recipe successfully created: {}", recipe);
        } catch (Exception e) {
            log.error("Error processing POST message", e);
        }
    }

    @RabbitListener(queues = recipePutRequestQueueName)
    public void onPutMethod(byte[] message) {
        try {
            log.info("Received PUT request: {}", new String(message));
            Recipe recipe = objectMapper.readValue(message, Recipe.class);
            if (recipeRepository.existsById(recipe.getId())) {
                recipeRepository.save(recipe);
                log.info("Recipe successfully updated: {}", recipe);
            } else {
                log.warn("Recipe not found for update: {}", recipe.getId());
            }
        } catch (Exception e) {
            log.error("Error processing PUT message", e);
        }
    }

    @RabbitListener(queues = recipeDeleteRequestQueueName)
    public void onDeleteMethod(byte[] message) {
        try {
            log.info("Received DELETE request: {}", new String(message));
            UUID id = UUID.fromString(new String(message));
            recipeRepository.deleteById(id);
            log.info("Recipe successfully deleted for ID: {}", id);
        } catch (Exception e) {
            log.error("Error processing DELETE message", e);
        }
    }
}

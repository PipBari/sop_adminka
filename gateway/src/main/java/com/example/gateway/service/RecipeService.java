package com.example.gateway.service;

import com.example.domain.grpc.RecipeProto;
import com.example.domain.grpc.RecipeServiceGrpc;
import com.example.gateway.api.dto.RecipeRequestDTO;
import com.example.gateway.api.dto.RecipeResponseDTO;
import com.example.gateway.config.RabbitMQConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.gateway.config.RedisConfig.REDIS_ALL_RECIPES_CACHE_KEY;
import static com.example.gateway.config.RedisConfig.REDIS_RECIPE_BY_ID_CACHE_KEY;

@Service
@Slf4j
public class RecipeService {

    private final ModelMapper modelMapper = new ModelMapper();

    @GrpcClient("recipeService")
    private RecipeServiceGrpc.RecipeServiceBlockingStub recipeServiceGrpc;

    private final RabbitTemplate rabbitTemplate;
    private final CacheManager cacheManager;

    public RecipeService(RabbitTemplate rabbitTemplate, CacheManager cacheManager) {
        this.rabbitTemplate = rabbitTemplate;
        this.cacheManager = cacheManager;
    }

    @Cacheable(REDIS_ALL_RECIPES_CACHE_KEY)
    public List<RecipeResponseDTO> getAllRecipes() {
        log.info("Fetching all recipes via gRPC...");
        var response = recipeServiceGrpc.listRecipes(RecipeProto.Empty.newBuilder().build());
        return response.getRecipesList()
                .stream()
                .map(recipe -> modelMapper.map(recipe, RecipeResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Cacheable(value = REDIS_RECIPE_BY_ID_CACHE_KEY, key = "#id")
    public RecipeResponseDTO getRecipeById(String id) {
        log.info("Fetching recipe by ID via gRPC: {}", id);
        var request = RecipeProto.RecipeRequest.newBuilder().setId(id).build();
        var recipe = recipeServiceGrpc.getRecipe(request);
        return modelMapper.map(recipe.getRecipe(), RecipeResponseDTO.class);
    }

    public void saveNewRecipe(RecipeRequestDTO recipeRequestDTO) {
        try {
            if (recipeRequestDTO.getId() == null || recipeRequestDTO.getId().isBlank()) {
                String generatedId = UUID.randomUUID().toString();
                recipeRequestDTO.setId(generatedId);
                log.info("Generated new ID for recipe: {}", generatedId);
            }

            byte[] message = new ObjectMapper().writeValueAsBytes(recipeRequestDTO);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.directExchangeName,
                    RabbitMQConfig.recipePostRequestRoutingKey,
                    message
            );

            RecipeResponseDTO recipeResponseDTO = modelMapper.map(recipeRequestDTO, RecipeResponseDTO.class);
            saveToCache(recipeResponseDTO);

            deleteFromCache(null);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize RecipeRequestDTO to JSON", e);
            throw new RuntimeException("Serialization error", e);
        } catch (Exception e) {
            log.error("Error while sending message to RabbitMQ", e);
            throw e;
        }
    }

    public void updateRecipe(String id, RecipeRequestDTO recipeRequestDTO) {
        try {
            recipeRequestDTO.setId(id);
            byte[] message = new ObjectMapper().writeValueAsBytes(recipeRequestDTO);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.directExchangeName,
                    RabbitMQConfig.recipePutRequestRoutingKey,
                    message
            );

            RecipeResponseDTO recipeResponseDTO = modelMapper.map(recipeRequestDTO, RecipeResponseDTO.class);
            saveToCache(recipeResponseDTO);

            deleteFromCache(null);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize RecipeRequestDTO to JSON", e);
            throw new RuntimeException("Serialization error", e);
        } catch (Exception e) {
            log.error("Error while sending update request to RabbitMQ for ID: {}", id, e);
            throw e;
        }
    }

    public void deleteRecipe(String id) {
        try {
            byte[] message = id.getBytes();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.directExchangeName,
                    RabbitMQConfig.recipeDeleteRequestRoutingKey,
                    message
            );

            deleteFromCache(id);
            deleteFromCache(null);
        } catch (Exception e) {
            log.error("Error while sending delete request to RabbitMQ for ID: {}", id, e);
            throw e;
        }
    }

    private void saveToCache(RecipeResponseDTO recipeResponseDTO) {
        Cache cacheById = cacheManager.getCache(REDIS_RECIPE_BY_ID_CACHE_KEY);
        if (cacheById != null) {
            cacheById.put(recipeResponseDTO.getId(), recipeResponseDTO);
            log.info("Saved recipe to cache with ID: {}", recipeResponseDTO.getId());
        }
    }

    private void deleteFromCache(String id) {
        if (id == null) {
            Cache allRecipesCache = cacheManager.getCache(REDIS_ALL_RECIPES_CACHE_KEY);
            if (allRecipesCache != null) {
                allRecipesCache.clear();
            }
        } else {
            Cache recipeByIdCache = cacheManager.getCache(REDIS_RECIPE_BY_ID_CACHE_KEY);
            if (recipeByIdCache != null) {
                recipeByIdCache.evictIfPresent(id);
            }
        }
    }
}

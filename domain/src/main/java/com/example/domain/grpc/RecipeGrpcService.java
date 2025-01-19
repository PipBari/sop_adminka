package com.example.domain.grpc;

import com.example.domain.config.RabbitMQConfig;
import com.example.domain.recipe.Recipe;
import com.example.domain.recipe.RecipeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class RecipeGrpcService extends RecipeServiceGrpc.RecipeServiceImplBase {

    private final RecipeRepository recipeRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public RecipeGrpcService(RecipeRepository recipeRepository, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.recipeRepository = recipeRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Timed(value = "grpc.get_recipe", description = "Time taken to get a recipe by ID")
    public void getRecipe(RecipeProto.RecipeRequest request, StreamObserver<RecipeProto.RecipeResponse> responseObserver) {
        log.info("Received GET request for ID: {}", request.getId());
        Recipe recipe = recipeRepository.findById(UUID.fromString(request.getId()))
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
        RecipeProto.Recipe grpcRecipe = RecipeProto.Recipe.newBuilder()
                .setId(recipe.getId().toString())
                .setName(recipe.getName())
                .setIngredients(recipe.getIngredients())
                .setCookingTime(recipe.getCookingTime())
                .build();

        RecipeProto.RecipeResponse response = RecipeProto.RecipeResponse.newBuilder()
                .setRecipe(grpcRecipe)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @Timed(value = "grpc.list_recipes", description = "Time taken to list all recipes")
    public void listRecipes(RecipeProto.Empty request, StreamObserver<RecipeProto.RecipeListResponse> responseObserver) {
        log.info("Received LIST request for all recipes");

        List<RecipeProto.Recipe> grpcRecipes = recipeRepository.findAll().stream()
                .map(recipe -> RecipeProto.Recipe.newBuilder()
                        .setId(recipe.getId().toString())
                        .setName(recipe.getName())
                        .setIngredients(recipe.getIngredients())
                        .setCookingTime(recipe.getCookingTime())
                        .build())
                .collect(Collectors.toList());

        RecipeProto.RecipeListResponse response = RecipeProto.RecipeListResponse.newBuilder()
                .addAllRecipes(grpcRecipes)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @Timed(value = "grpc.create_recipe", description = "Time taken to create a recipe")
    public void createRecipe(RecipeProto.Recipe request, StreamObserver<RecipeProto.Empty> responseObserver) {
        try {
            log.info("Received CREATE request for Recipe: {}", request);
            Recipe recipe = new Recipe(UUID.randomUUID(), request.getName(), request.getIngredients(), request.getCookingTime());
            byte[] message = objectMapper.writeValueAsBytes(recipe);
            rabbitTemplate.convertAndSend(RabbitMQConfig.directExchangeName, RabbitMQConfig.recipePostRequestQueueRoutingKey, message);

            responseObserver.onNext(RecipeProto.Empty.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error while processing CREATE request", e);
            responseObserver.onError(e);
        }
    }

    @Override
    @Timed(value = "grpc.update_recipe", description = "Time taken to update a recipe")
    public void updateRecipe(RecipeProto.Recipe request, StreamObserver<RecipeProto.Empty> responseObserver) {
        try {
            log.info("Received UPDATE request for Recipe: {}", request);
            Recipe recipe = new Recipe(UUID.fromString(request.getId()), request.getName(), request.getIngredients(), request.getCookingTime());
            byte[] message = objectMapper.writeValueAsBytes(recipe);
            rabbitTemplate.convertAndSend(RabbitMQConfig.directExchangeName, RabbitMQConfig.recipePutRequestQueueRoutingKey, message);

            responseObserver.onNext(RecipeProto.Empty.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error while processing UPDATE request", e);
            responseObserver.onError(e);
        }
    }

    @Override
    @Timed(value = "grpc.delete_recipe", description = "Time taken to delete a recipe by ID")
    public void deleteRecipe(RecipeProto.RecipeRequest request, StreamObserver<RecipeProto.Empty> responseObserver) {
        try {
            log.info("Received DELETE request for ID: {}", request.getId());
            byte[] message = request.getId().getBytes();
            rabbitTemplate.convertAndSend(RabbitMQConfig.directExchangeName, RabbitMQConfig.recipeDeleteRequestQueueRoutingKey, message);

            responseObserver.onNext(RecipeProto.Empty.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error while processing DELETE request", e);
            responseObserver.onError(e);
        }
    }
}

package com.example.gateway.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String recipePostRequestQueueName = "recipePostRequestQueue";
    public static final String recipePutRequestQueueName = "recipePutRequestQueue";
    public static final String recipeDeleteRequestQueueName = "recipeDeleteRequestQueue";

    public static final String recipePostRequestRoutingKey = "recipe.post";
    public static final String recipePutRequestRoutingKey = "recipe.put";
    public static final String recipeDeleteRequestRoutingKey = "recipe.delete";

    public static final String directExchangeName = "recipeExchange";

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(directExchangeName);
    }

    @Bean("recipePostRequestQueue")
    public Queue recipePostRequestQueue() {
        return new Queue(recipePostRequestQueueName, true);
    }

    @Bean("recipePutRequestQueue")
    public Queue recipePutRequestQueue() {
        return new Queue(recipePutRequestQueueName, true);
    }

    @Bean("recipeDeleteRequestQueue")
    public Queue recipeDeleteRequestQueue() {
        return new Queue(recipeDeleteRequestQueueName, true);
    }

    @Bean
    public Binding postRequestBinding(@Qualifier("recipePostRequestQueue") Queue recipePostRequestQueue, DirectExchange exchange) {
        return BindingBuilder.bind(recipePostRequestQueue).to(exchange).with(recipePostRequestRoutingKey);
    }

    @Bean
    public Binding putRequestBinding(@Qualifier("recipePutRequestQueue") Queue recipePutRequestQueue, DirectExchange exchange) {
        return BindingBuilder.bind(recipePutRequestQueue).to(exchange).with(recipePutRequestRoutingKey);
    }

    @Bean
    public Binding deleteRequestBinding(@Qualifier("recipeDeleteRequestQueue") Queue recipeDeleteRequestQueue, DirectExchange exchange) {
        return BindingBuilder.bind(recipeDeleteRequestQueue).to(exchange).with(recipeDeleteRequestRoutingKey);
    }
}

package com.example.domain.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String recipePostRequestQueueName = "recipePostRequestQueue";
    public static final String recipePutRequestQueueName = "recipePutRequestQueue";
    public static final String recipeDeleteRequestQueueName = "recipeDeleteRequestQueue";

    public static final String recipePostRequestQueueRoutingKey = "recipe.post";
    public static final String recipePutRequestQueueRoutingKey = "recipe.put";
    public static final String recipeDeleteRequestQueueRoutingKey = "recipe.delete";

    public static final String directExchangeName = "recipeExchange";

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange exchange() {
        return ExchangeBuilder.directExchange(directExchangeName)
                .durable(true)
                .build();
    }

    @Bean
    public Queue recipePostRequestQueue() {
        return QueueBuilder.durable(recipePostRequestQueueName).build();
    }

    @Bean
    public Queue recipePutRequestQueue() {
        return QueueBuilder.durable(recipePutRequestQueueName).build();
    }

    @Bean
    public Queue recipeDeleteRequestQueue() {
        return QueueBuilder.durable(recipeDeleteRequestQueueName).build();
    }

    @Bean
    public Binding postRequestBinding(Queue recipePostRequestQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(recipePostRequestQueue).to(directExchange).with(recipePostRequestQueueRoutingKey);
    }

    @Bean
    public Binding putRequestBinding(Queue recipePutRequestQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(recipePutRequestQueue).to(directExchange).with(recipePutRequestQueueRoutingKey);
    }

    @Bean
    public Binding deleteRequestBinding(Queue recipeDeleteRequestQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(recipeDeleteRequestQueue).to(directExchange).with(recipeDeleteRequestQueueRoutingKey);
    }
}

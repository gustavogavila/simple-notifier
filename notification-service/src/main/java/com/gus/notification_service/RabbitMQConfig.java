package com.gus.notification_service;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyEventApplicationListener(RabbitAdmin rabbitAdmin) {
        return event -> rabbitAdmin.initialize();
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public Queue queueUserRegistration() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "users.v1.user-created.dlx"); // envia para a exchange
//        args.put("x-dead-letter-routing-key", "users.v1.user-created.dlx.email-notification.dlq"); // envia direto para a fila dlq
        return new Queue("users.v1.user-created.email-notification", true, false, false, args);
    }

    @Bean
    public Binding binding() {
        var queue = new Queue("users.v1.user-created.email-notification");
        var exchange = new FanoutExchange("users.v1.user-created");
        return BindingBuilder.bind(queue).to(exchange);
    }

    @Bean
    public Queue queueUserRegistrationDLQ() {
        return new Queue("users.v1.user-created.dlx.email-notification.dlq");
    }

    @Bean
    public Binding bindingDLQ() {
        var dlqQueue = new Queue("users.v1.user-created.dlx.email-notification.dlq");
        var dlqExchange = new FanoutExchange("users.v1.user-created.dlx");
        return BindingBuilder.bind(dlqQueue).to(dlqExchange);
    }

    @Bean
    public Queue queueParkingLot() {
        return new Queue("users.v1.user-created.dlx.email-notification.dlq.parking-lot");
    }
}

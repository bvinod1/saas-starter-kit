package com.saasstarterkit.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the notification service
 * Sets up exchanges, queues, and bindings for event-driven notifications
 */
@Configuration
public class RabbitMQConfig {
    
    @Value("${notification.dead-letter.queue:notification.dlq}")
    private String deadLetterQueue;
    
    @Value("${notification.dead-letter.exchange:notification.dlx}")
    private String deadLetterExchange;
    
    @Value("${notification.dead-letter.routing-key:notification.dlq}")
    private String deadLetterRoutingKey;
    
    // =============================================
    // EXCHANGES
    // =============================================
    
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange("notification.exchange", true, false);
    }
    
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(deadLetterExchange, true, false);
    }
    
    // =============================================
    // QUEUES
    // =============================================
    
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable("notification.queue")
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }
    
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable("notification.email.queue")
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .withArgument("x-message-ttl", 600000) // 10 minutes TTL
                .build();
    }
    
    @Bean
    public Queue smsQueue() {
        return QueueBuilder.durable("notification.sms.queue")
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }
    
    @Bean
    public Queue pushQueue() {
        return QueueBuilder.durable("notification.push.queue")
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }
    
    @Bean
    public Queue inAppQueue() {
        return QueueBuilder.durable("notification.inapp.queue")
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }
    
    @Bean
    public Queue scheduledQueue() {
        return QueueBuilder.durable("notification.scheduled.queue")
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .withArgument("x-message-ttl", 86400000) // 24 hours TTL
                .build();
    }
    
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(deadLetterQueue).build();
    }
    
    @Bean
    public Queue retryQueue() {
        return QueueBuilder.durable("notification.retry.queue")
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }
    
    // =============================================
    // BINDINGS
    // =============================================
    
    @Bean
    public Binding notificationQueueBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with("notification.*");
    }
    
    @Bean
    public Binding emailQueueBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(notificationExchange())
                .with("notification.email");
    }
    
    @Bean
    public Binding smsQueueBinding() {
        return BindingBuilder.bind(smsQueue())
                .to(notificationExchange())
                .with("notification.sms");
    }
    
    @Bean
    public Binding pushQueueBinding() {
        return BindingBuilder.bind(pushQueue())
                .to(notificationExchange())
                .with("notification.push");
    }
    
    @Bean
    public Binding inAppQueueBinding() {
        return BindingBuilder.bind(inAppQueue())
                .to(notificationExchange())
                .with("notification.inapp");
    }
    
    @Bean
    public Binding scheduledQueueBinding() {
        return BindingBuilder.bind(scheduledQueue())
                .to(notificationExchange())
                .with("notification.scheduled");
    }
    
    @Bean
    public Binding deadLetterQueueBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(deadLetterRoutingKey);
    }
    
    @Bean
    public Binding retryQueueBinding() {
        return BindingBuilder.bind(retryQueue())
                .to(notificationExchange())
                .with("notification.retry");
    }
    
    // =============================================
    // MESSAGE CONVERTER
    // =============================================
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    // =============================================
    // RABBIT TEMPLATE
    // =============================================
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                // Handle negative acknowledgment
                System.err.println("Message was not acknowledged: " + cause);
            }
        });
        rabbitTemplate.setReturnsCallback(returned -> {
            // Handle returned messages
            System.err.println("Message returned: " + returned);
        });
        return rabbitTemplate;
    }
    
    // =============================================
    // LISTENER CONTAINER FACTORY
    // =============================================
    
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(5);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(1);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}

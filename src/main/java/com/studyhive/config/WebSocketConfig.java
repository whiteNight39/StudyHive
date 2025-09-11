package com.studyhive.config;

import com.studyhive.util.websocket.JwtChannelInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;

    public WebSocketConfig(JwtChannelInterceptor jwtChannelInterceptor) {
        this.jwtChannelInterceptor = jwtChannelInterceptor;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // In-memory broker for dev / portfolio
        config.enableSimpleBroker("/topic", "/queue");
        // user destination prefix for convertAndSendToUser(...)
        config.setUserDestinationPrefix("/user");
        // application destination prefix for @MessageMapping controllers
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Client connects here: /ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")    // tighten for prod
                .withSockJS();                    // fallback for browsers
    }
}

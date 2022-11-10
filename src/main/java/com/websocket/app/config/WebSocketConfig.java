package com.websocket.app.config;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/*
 *啟用Springboot對WebSocket的支援
 */
@Configuration
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	/*
	 *把ServerEndpointExporter這個類注入至Spring容器中
	 */
    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();
    }
}

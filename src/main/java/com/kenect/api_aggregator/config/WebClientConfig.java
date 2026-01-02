package com.kenect.api_aggregator.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Value("${kenect.api.base-url}")
    private String baseUrl;

    @Value("${kenect.api.bearer-token}")
    private String bearerToken;

    @Value("${kenect.api.timeout.connect:5000}")
    private int connectTimeout;

    @Value("${kenect.api.timeout.read:30000}")
    private int readTimeout;

    @Value("${kenect.api.timeout.write:30000}")
    private int writeTimeout;

    @Bean
    public WebClient kenectApiWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .responseTimeout(Duration.ofMillis(readTimeout))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + bearerToken)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}

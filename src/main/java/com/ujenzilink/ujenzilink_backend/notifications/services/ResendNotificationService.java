package com.ujenzilink.ujenzilink_backend.notifications.services;

import com.ujenzilink.ujenzilink_backend.configs.ResendConfig;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class ResendNotificationService {

    private final WebClient webClient;
    private final String fromEmail;

    public ResendNotificationService(WebClient resendWebClient, ResendConfig resendConfig) {
        this.webClient = resendWebClient;
        this.fromEmail = resendConfig.getFromEmail();
    }

    public Mono<String> sendEmail(String to, String subject, String htmlContent) {

        Map<String, Object> body = Map.of(
                "from", fromEmail,
                "to", to,
                "subject", subject,
                "html", htmlContent
        );

        return webClient.post()
                .uri("/emails")
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            System.err.println("Resend API Error Body: " + errorBody);
                            return Mono.error(new RuntimeException("Resend API error: " + errorBody));
                        })
                )
                .bodyToMono(String.class)
                .doOnNext(response -> System.out.println("Email sent successfully: " + response))
                .doOnError(e -> System.err.println("Failed to send email: " + e.getMessage()));
    }
}
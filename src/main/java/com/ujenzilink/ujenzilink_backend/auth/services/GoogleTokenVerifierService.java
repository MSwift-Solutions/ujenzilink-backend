package com.ujenzilink.ujenzilink_backend.auth.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleTokenVerifierService {

    @Value("${google.oauth.client-id}")
    private String clientId;

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifierService(@Value("${google.oauth.client-id:}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .setIssuer("https://accounts.google.com")
                .build();
    }

    public GoogleIdToken.Payload verifyToken(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null) {
            throw new IllegalArgumentException("Invalid ID token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        if (!payload.getEmailVerified()) {
            throw new IllegalArgumentException("Email not verified by Google");
        }

        return payload;
    }
}

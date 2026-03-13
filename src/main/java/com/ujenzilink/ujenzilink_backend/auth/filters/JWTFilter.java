package com.ujenzilink.ujenzilink_backend.auth.filters;

import com.ujenzilink.ujenzilink_backend.auth.admin.services.AdminAuthService;
import com.ujenzilink.ujenzilink_backend.auth.services.SignInService;
import com.ujenzilink.ujenzilink_backend.auth.utils.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@NullMarked
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final SignInService signInService;
    private final AdminAuthService adminAuthService;
    private final HandlerExceptionResolver resolver;

    public JWTFilter(JWTUtil jwtUtil,
                     SignInService signInService,
                     AdminAuthService adminAuthService,
                     @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.jwtUtil = jwtUtil;
        this.signInService = signInService;
        this.adminAuthService = adminAuthService;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            final String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                String username = jwtUtil.extractUserName(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String role = jwtUtil.extractRole(token);
                    UserDetails userDetails;

                    if ("ROLE_SUPER_ADMIN".equals(role) || "ROLE_ADMIN".equals(role)) {
                        userDetails = adminAuthService.loadUserByUsername(username);
                    } else {
                        userDetails = signInService.loadUserByUsername(username);
                    }

                    if (jwtUtil.isTokenValid(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException | MalformedJwtException | IllegalArgumentException e) {
            // This sends the exception to your GlobalExceptionHandler!
            resolver.resolveException(request, response, null, e);
        } catch (Exception e) {
            resolver.resolveException(request, response, null, e);
        }
    }
}

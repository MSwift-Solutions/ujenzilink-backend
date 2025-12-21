package com.ujenzilink.ujenzilink_backend.auth.filters;

import com.ujenzilink.ujenzilink_backend.auth.services.SignInService;
import com.ujenzilink.ujenzilink_backend.auth.utils.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final SignInService signInService;

    private String jwtToken = null;
    private String userName = null;

    public JWTFilter(JWTUtil jwtUtil, SignInService signInService) {
        this.jwtUtil = jwtUtil;
        this.signInService = signInService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException, java.io.IOException {
        final String header = request.getHeader("Authorization");

        if(header != null && header.startsWith("Bearer ")){
            jwtToken = header.substring(7);
            try{
                userName = jwtUtil.extractUserName(jwtToken);
            }catch (IllegalArgumentException e){
                System.out.println("Unable to get JWTToken");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\": \"Internal server error, Unable to get JWT Token.\", \"statusCode\": 500}");
                return;
            }catch (ExpiredJwtException e){
                System.out.println("Token Expired: " +e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\": \"Session expired, kindly login again.\", \"statusCode\": 401}");
                return;
            }catch (MalformedJwtException e){
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\": \"Malformed JWT, log in again.\", \"statusCode\": 401}");
                return;
            }
        }else {
            System.out.println("Disabled authorize endpoint accessed!");
        }

        if(userName != null && SecurityContextHolder.getContext().getAuthentication() == null){
            try {
                UserDetails userDetails = signInService.loadUserByUsername(userName);
                if(jwtUtil.isTokenValid(jwtToken, userDetails)){
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails,
                                    null,
                                    userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            } catch (UsernameNotFoundException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\": \"" + e.getMessage() + "\", \"statusCode\": 401}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    public String getCurrentUser(){
        return userName;
    }

    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().iterator().next().getAuthority();
    }

}
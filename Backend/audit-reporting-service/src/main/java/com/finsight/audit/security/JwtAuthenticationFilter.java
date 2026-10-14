package com.finsight.audit.security;

import com.finsight.common.constant.FinSightHeaders;
import com.finsight.common.context.CorrelationIdContext;
import com.finsight.common.security.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(FinSightHeaders.CORRELATION_ID);
        CorrelationIdContext.set(correlationId);

        try {
            String authHeader = request.getHeader(FinSightHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith(FinSightHeaders.BEARER_PREFIX)) {
                String token = authHeader.substring(FinSightHeaders.BEARER_PREFIX.length());
                if (jwtUtils.validateToken(token)) {
                    String username = jwtUtils.extractUsername(token);
                    List<String> roles = jwtUtils.extractRoles(token);

                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                            .map(SimpleGrantedAuthority::new)
                            .toList();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } else {
                String userId = request.getHeader(FinSightHeaders.USER_ID);
                String userRoles = request.getHeader(FinSightHeaders.USER_ROLES);
                String userEmail = request.getHeader(FinSightHeaders.USER_EMAIL);

                if (userId != null && userRoles != null) {
                    List<SimpleGrantedAuthority> authorities = Arrays.stream(userRoles.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                            .map(SimpleGrantedAuthority::new)
                            .toList();

                    String principal = userEmail != null ? userEmail : "user-" + userId;
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            CorrelationIdContext.clear();
        }
    }
}

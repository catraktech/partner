package com.catrak.exatip.partner.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.catrak.exatip.commonlib.dto.JsonResponseDTO;
import com.catrak.exatip.partner.util.JwtTokenCreator;
import com.google.gson.Gson;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenCreator jwtTokenCreator;

    public JwtAuthorizationFilter(JwtTokenCreator jwtTokenCreator) {
        super();
        this.jwtTokenCreator = jwtTokenCreator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        boolean isExceptionOccured = false;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtTokenCreator.extractUsername(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null
                        && Boolean.TRUE.equals(jwtTokenCreator.validateToken(token))) {
                    Authentication authToken = new UsernamePasswordAuthenticationToken(username, "");
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException
                    | SignatureException ex) {
                isExceptionOccured = true;
                setErrorResponse(HttpStatus.BAD_REQUEST, response, ex);
            }
        }
        if (authHeader == null || !isExceptionOccured) {
            filterChain.doFilter(request, response);
        }

    }

    public void setErrorResponse(HttpStatus status, HttpServletResponse response, Throwable ex) throws IOException {
        Gson g = new Gson();
        response.setStatus(status.value());
        response.setContentType("application/json");
        String exception = g.toJson(new JsonResponseDTO<>(ex.getMessage(), false, JsonResponseDTO.BAD_REQUEST));
        response.getWriter().write(exception);
    }

}

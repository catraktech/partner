package com.catrak.exatip.partner.filter;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Optional;

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
import com.catrak.exatip.commonlib.entity.PartnerInfo;
import com.catrak.exatip.partner.repository.PartnerInfoRepository;
import com.catrak.exatip.partner.util.JwtTokenValidator;
import com.google.gson.Gson;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenValidator jwtTokenValidator;

    private final PartnerInfoRepository partnerInfoRepository;

    public JwtAuthorizationFilter(JwtTokenValidator jwtTokenValidator, PartnerInfoRepository partnerInfoRepository) {
        super();
        this.jwtTokenValidator = jwtTokenValidator;
        this.partnerInfoRepository = partnerInfoRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtTokenValidator.extractUsername(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null
                        && Boolean.TRUE.equals(jwtTokenValidator.validateToken(token))) {
                    Optional<PartnerInfo> partnerOptional = partnerInfoRepository.findByUserName(username);
                    if (!partnerOptional.isPresent()) {
                        setErrorResponse(HttpStatus.BAD_REQUEST, response, new Exception("Invalid user"));
                        return;
                    }
                    if (partnerOptional.get().getExpirationDateTime()
                            .before(new Timestamp(System.currentTimeMillis()))) {
                        setErrorResponse(HttpStatus.BAD_REQUEST, response,
                                new Exception("api-key expires, please renew the api-key"));
                        return;
                    }
                    Authentication authToken = new UsernamePasswordAuthenticationToken(username, "");
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException
                    | SignatureException ex) {
                setErrorResponse(HttpStatus.BAD_REQUEST, response, ex);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    public void setErrorResponse(HttpStatus status, HttpServletResponse response, Throwable ex) throws IOException {
        Gson g = new Gson();
        response.setStatus(status.value());
        response.setContentType("application/json");
        String exception = g.toJson(new JsonResponseDTO<>(ex.getMessage(), false, JsonResponseDTO.BAD_REQUEST));
        response.getWriter().write(exception);
    }

}

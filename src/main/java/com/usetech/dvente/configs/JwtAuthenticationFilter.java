package com.usetech.dvente.configs;

import com.usetech.dvente.services.auth.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.warn("Token expiré : {}");
            sendErrorResponse(response, 401, "token_expired", "Le token JWT a expiré", e.getClaims().getExpiration().toInstant().toString());
            return;

        } catch (io.jsonwebtoken.MalformedJwtException e) {
            logger.warn("Token invalide : {}");
            sendErrorResponse(response, 400, "token_invalid", "Le token JWT est invalide", null);
            return;

        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            logger.warn("Token non supporté : {}");
            sendErrorResponse(response, 400, "token_unsupported", "Le token JWT n'est pas supporté", null);
            return;

        } catch (io.jsonwebtoken.security.SignatureException e) {
            logger.warn("Signature invalide : {}");
            sendErrorResponse(response, 400, "token_invalid_signature", "La signature du token JWT est invalide", null);
            return;

        } catch (Exception e) {
            logger.error("Erreur authentification JWT : {}");
            sendErrorResponse(response, 500, "internal_error", "Erreur interne lors de l'authentification JWT", null);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String error, String message, String expiredAt) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");

        String json = String.format(
                "{\"error\":\"%s\",\"message\":\"%s\"%s}",
                error,
                message,
                expiredAt != null ? String.format(",\"expiredAt\":\"%s\"", expiredAt) : ""
        );

        response.getWriter().write(json);
    }
}

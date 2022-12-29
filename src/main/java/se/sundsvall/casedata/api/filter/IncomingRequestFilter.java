package se.sundsvall.casedata.api.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

import static java.util.Objects.isNull;
import static se.sundsvall.casedata.service.util.Constants.AD_USER_HEADER_KEY;
import static se.sundsvall.casedata.service.util.Constants.UNKNOWN;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

@Component
public class IncomingRequestFilter extends OncePerRequestFilter {

    // WSO2-subscriber
    @Getter
    private String subscriber;

    // AD-user
    @Getter
    private String adUser;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        // Extract sub from x-jwt-assertion header
        extractSubsciber(request);
        // Extract AD-user from ad-user header
        extractAdUser(request);

        filterChain.doFilter(request, response);
    }

    private void extractAdUser(HttpServletRequest request) {
        var headerValue = request.getHeader(AD_USER_HEADER_KEY);

        if (isNull(headerValue) || headerValue.isBlank()) {
            adUser = UNKNOWN;
        } else {
            adUser = headerValue;
        }
    }

    private void extractSubsciber(HttpServletRequest request) throws JsonProcessingException {
        var jwtHeader = request.getHeader(X_JWT_ASSERTION_HEADER_KEY);

        if (isNull(jwtHeader) || jwtHeader.isBlank()) {
            subscriber = UNKNOWN;
        } else {
            String[] jwtParts = jwtHeader.split("\\.");
            String jwtPayload = new String(Base64.getUrlDecoder().decode(jwtParts[1]));
            subscriber = new ObjectMapper().readTree(jwtPayload).findValue("sub").asText();
        }
    }
}

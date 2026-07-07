package com.fts.twin.handler;

import com.fts.twin.util.ResponseConstant;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Returns JSON 401 Unauthorized response for missing or invalid authentication.
 */
@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException authException) throws IOException, ServletException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json");

        Map<String, Object> errorAttributes = new LinkedHashMap<>();
        errorAttributes.put("status", "error");
        errorAttributes.put("message", ResponseConstant.MSG_TOKEN_INVALID);
        errorAttributes.put("data", null);

        res.getWriter().write(new ObjectMapper().writeValueAsString(errorAttributes));
        res.getWriter().flush();
        res.getWriter().close();
    }
}

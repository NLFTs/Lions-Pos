package com.dak.spravel.handler;

import com.dak.spravel.util.ResponseConstant;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Returns JSON 403 Forbidden response for missing authorities.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex)
            throws IOException, ServletException {
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        res.setContentType("application/json");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "error");
        body.put("message", ResponseConstant.MSG_FORBIDDEN);
        body.put("data", null);

        res.getWriter().write(new ObjectMapper().writeValueAsString(body));
        res.getWriter().flush();
        res.getWriter().close();
    }
}

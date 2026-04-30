package com.dak.spravel.middleware;

import com.dak.spravel.model.system.LogHttp;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.system.LogHttpRepository;
import com.dak.spravel.util.ConfigConstant;
import com.dak.spravel.util.StringUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.*;

/**
 * Servlet filter for HTTP request/response logging with sensitive data masking.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HttpLogFilter implements Filter {

    private final LogHttpRepository logHttpRepository;
    private final UserRepository userRepository;

    private static final List<String> REQUEST_IGNORE = List.of("ignore-sample");
    private static final List<String> RESPONSE_IGNORE = List.of("ignore-sample");

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        var req = (HttpServletRequest) request;
        var res = (HttpServletResponse) response;

        long startProcess = System.currentTimeMillis();
        log.info("◉—————————————————————————————————————————————————————————————————◉");
        log.info("⚪ Process Started, 🌐 {} {}", req.getMethod(), req.getRequestURI());
        log.info("[s] Controller");

        try {
            ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(req);
            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(res);
            chain.doFilter(requestWrapper, responseWrapper);

            LogHttp logInfo = createLogRecord(req, requestWrapper, responseWrapper);
            logHttpRepository.save(logInfo);
            responseWrapper.copyBodyToResponse();

            long endProcess = System.currentTimeMillis();
            long interval = endProcess - startProcess;
            log.info("[e] Controller");
            log.info("🟢 Process Completed, 🕛 the process took {} ms", interval);
        } catch (Exception e) {
            long endProcess = System.currentTimeMillis();
            long interval = endProcess - startProcess;
            log.error("[e] eController");
            log.error("🔴 Process Completed, 🕛 the process took {} ms", interval);
            if (!response.isCommitted()) {
                chain.doFilter(request, response);
            }
        }
    }

    private LogHttp createLogRecord(
            HttpServletRequest req,
            ContentCachingRequestWrapper requestWrapper,
            ContentCachingResponseWrapper responseWrapper) {
        LogHttp logInfo = new LogHttp();
        String uri = req.getRequestURI().toLowerCase();

        logInfo.setType(ConfigConstant.HTTP_LOG_INCOMING);
        logInfo.setServiceName(ConfigConstant.SERVICE_NAME);
        logInfo.setRequestAt(new Date());
        logInfo.setResponseAt(new Date());
        logInfo.setMethod(requestWrapper.getMethod());
        logInfo.setUrl(req.getRequestURI());
        logInfo.setUserAgent(req.getHeader("User-Agent"));

        setUserInfo(logInfo);
        processHeaders(req, logInfo);

        if (shouldProcessRequestBody(uri, req)) {
            processRequestBody(requestWrapper, logInfo);
        }
        if (shouldProcessResponseBody(uri, responseWrapper)) {
            processResponseBody(responseWrapper, logInfo);
        }
        return logInfo;
    }

    private void setUserInfo(LogHttp logInfo) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            String username = auth.getName();
            userRepository.findByUsername(username).ifPresent(user -> {
                logInfo.setUserId(user.getId());
                logInfo.setUserFullname(user.getFullname() == null ? "" : user.getFullname());
            });
        }
    }

    private void processHeaders(HttpServletRequest req, LogHttp logInfo) {
        JSONObject headersJson = new JSONObject();
        Enumeration<String> headerNames = req.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = req.getHeader(headerName);
            if ("X-CUSTOM-DESCRIPTION".equalsIgnoreCase(headerName)) {
                logInfo.setDescription(headerValue);
            }
            if ("authorization".equalsIgnoreCase(headerName)) {
                headerValue = "*****";
            }
            headersJson.put(headerName, headerValue);
        }
        logInfo.setHeader(headersJson.toString());
    }

    private boolean shouldProcessRequestBody(String uri, HttpServletRequest req) {
        return REQUEST_IGNORE.stream().noneMatch(uri::contains) &&
                "application/json".equals(req.getContentType());
    }

    private void processRequestBody(ContentCachingRequestWrapper requestWrapper, LogHttp logInfo) {
        String requestBody = new String(requestWrapper.getContentAsByteArray());
        if (!requestBody.isEmpty()) {
            try {
                logInfo.setRequest(StringUtil.maskingJson(new JSONObject(requestBody)).toString());
            } catch (Exception e) {
                log.debug("Failed to process request body", e);
            }
        }
    }

    private boolean shouldProcessResponseBody(String uri, HttpServletResponse res) {
        return RESPONSE_IGNORE.stream().noneMatch(uri::contains) &&
                "application/json".equals(res.getContentType());
    }

    private void processResponseBody(ContentCachingResponseWrapper responseWrapper, LogHttp logInfo) {
        String responseBody = new String(responseWrapper.getContentAsByteArray());
        if (!responseBody.isEmpty()) {
            try {
                logInfo.setResponse(StringUtil.maskingJson(new JSONObject(responseBody)).toString());
            } catch (Exception e) {
                log.debug("Failed to process response body", e);
            }
        }
    }
}

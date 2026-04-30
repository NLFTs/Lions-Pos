package com.dak.spravel.controller.main;

import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.util.ResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Root controller handling hello/version, app info, Scalar docs redirect,
 * and Vue SPA routing.
 */
@Slf4j
@Controller
public class MainController {
    @Value("${app.name:#{null}}")
    private String appName;

    @Value("${app.version:#{null}}")
    private String appVersion;

    @Value("${app.build.image.tag:#{null}}")
    private String imageTag;

    @Value("${spring.jpa.show-sql:false}")
    private boolean showSql;

    /**
     * GET /
     * Say hello and return app version.
     */
    @GetMapping("/")
    @ResponseBody
    public ResponseEntity<ResData<Map<String, Object>>> hello() {
        log.info("version {}", appVersion);
        return ResponseBuilder.ok(Map.of("version", appVersion != null ? appVersion : "unknown"));
    }

    /**
     * GET /api/v1/info
     * Return app metadata and status.
     */
    @GetMapping("/api/v1/info")
    @ResponseBody
    public ResponseEntity<ResData<Map<String, Object>>> info() {
        return ResponseBuilder.ok(Map.of(
                "service", appName != null ? appName : "spravel",
                "version", appVersion != null ? appVersion : "unknown",
                "imageTag", imageTag != null ? imageTag : "not-set"
        ));
    }

    /**
     * GET /docs
     * Redirect to documentation (Scalar UI).
     */
    @GetMapping("/docs")
    public String redirectToScalar() {
        return "redirect:/scalar-ui/index.html";
    }

    /**
     * GET /_/**
     * Forward to SPA index (deep routing).
     */
    @GetMapping("/_/{path:[^\\.]*}/**")
    public String spaDeep(Model model) {
        return spa(model);
    }

    /**
     * GET /_/{path}
     * Forward to SPA index (shallow routing).
     */
    @GetMapping("/_/{path:[^\\.]*}")
    public String spaShallow(Model model) {
        return spa(model);
    }

    /**
     * GET /_/
     * Forward to SPA index (root).
     */
    @GetMapping("/_/")
    public String spaRoot(Model model) {
        return spa(model);
    }

    private String spa(Model model) {
        model.addAttribute("devMode", showSql);
        return "pages/index";
    }
}

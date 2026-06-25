package com.dak.spravel.controller.system;

import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.NotificationResponse;
import com.dak.spravel.service.system.NotificationService;
import com.dak.spravel.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ResData<Page<NotificationResponse>>> index(
            @RequestParam(defaultValue = "false") Boolean isDraft,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("[GET] /api/v1/notifications isDraft={} page={} size={}", isDraft, page, size);
        return ResponseBuilder.ok(notificationService.findAll(isDraft, page, size));
    }

    @PostMapping("/{id}/draft")
    public ResponseEntity<ResData<NotificationResponse>> markAsDraft(@PathVariable Long id) {
        log.info("[POST] /api/v1/notifications/{}/draft — START", id);
        try {
            NotificationResponse result = notificationService.markAsDraft(id);
            log.info("[POST] /api/v1/notifications/{}/draft — SUCCESS isDraft={}", id, result.getIsDraft());
            return ResponseBuilder.ok(result);
        } catch (Exception e) {
            log.error("[POST] /api/v1/notifications/{}/draft — ERROR: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResData<String>> delete(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/notifications/{}", id);
        notificationService.delete(id);
        return ResponseBuilder.ok("Notification deleted successfully");
    }

    @PostMapping("/bulk-draft")
    public ResponseEntity<ResData<String>> bulkMarkAsDraft(@RequestBody List<Long> ids) {
        log.info("[POST] /api/v1/notifications/bulk-draft size={}", ids.size());
        notificationService.bulkMarkAsDraft(ids);
        return ResponseBuilder.ok("Notifications marked as draft successfully");
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<ResData<String>> bulkDelete(@RequestBody List<Long> ids) {
        log.info("[POST] /api/v1/notifications/bulk-delete size={}", ids.size());
        notificationService.bulkDelete(ids);
        return ResponseBuilder.ok("Notifications deleted successfully");
    }
}

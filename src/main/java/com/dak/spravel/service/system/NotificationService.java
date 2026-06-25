package com.dak.spravel.service.system;

import com.dak.spravel.dto.response.NotificationResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.system.Notification;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.system.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    private void checkAccess(Notification notification, User currentUser) {
        // Super Admin (partner == null) bisa akses semua notifikasi
        if (currentUser.getPartner() == null) return;

        // Partner user hanya bisa akses notifikasi milik partnernya
        // Notifikasi dengan partner == null adalah milik system/super admin — tidak bisa diakses partner user
        if (notification.getPartner() == null) {
            throw new RuntimeException("Akses Ditolak: Notifikasi ini milik sistem.");
        }
        if (!notification.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Notifikasi bukan milik partner Anda.");
        }
    }

    @Transactional
    public Page<NotificationResponse> findAll(Boolean isDraft, int page, int size) {
        User currentUser = getAuthenticatedUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Notification> notifications;
        if (currentUser.getPartner() == null) {
            // Super Admin can see everything
            notifications = notificationRepository.findByIsDraft(isDraft, pageable);
        } else {
            // Scoped to partner
            notifications = notificationRepository.findByPartnerIdAndIsDraft(
                    currentUser.getPartner().getId(), isDraft, pageable);
        }

        // If viewing the inbox page, mark all unseen active notifications for this partner as seen
        if (!isDraft) {
            List<Notification> unseenList;
            if (currentUser.getPartner() == null) {
                unseenList = notificationRepository.findByIsDraftAndIsSeen(false, false);
            } else {
                unseenList = notificationRepository.findByPartnerIdAndIsDraftAndIsSeen(
                        currentUser.getPartner().getId(), false, false);
            }
            if (!unseenList.isEmpty()) {
                for (Notification n : unseenList) {
                    n.setIsSeen(true);
                }
                notificationRepository.saveAll(unseenList);
            }
        }

        return notifications.map(this::mapToResponse);
    }

    @Transactional
    public NotificationResponse markAsDraft(Long id) {
        User currentUser = getAuthenticatedUser();
        Notification notification = notificationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));

        checkAccess(notification, currentUser);
        notification.setIsDraft(true);
        Notification saved = notificationRepository.save(notification);
        log.info("[markAsDraft] id={} isDraft={} OK", saved.getId(), saved.getIsDraft());
        return mapToResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        Notification notification = notificationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));

        checkAccess(notification, currentUser);
        notificationRepository.delete(notification);
        log.info("[delete] id={} deleted OK", id);
    }

    @Transactional
    public void bulkMarkAsDraft(List<Long> ids) {
        User currentUser = getAuthenticatedUser();
        List<Notification> notifications = notificationRepository.findAllById(ids);
        for (Notification n : notifications) {
            checkAccess(n, currentUser);
            n.setIsDraft(true);
        }
        notificationRepository.saveAll(notifications);
        log.info("[bulkMarkAsDraft] {} items saved as draft", notifications.size());
    }

    @Transactional
    public void bulkDelete(List<Long> ids) {
        User currentUser = getAuthenticatedUser();
        List<Notification> notifications = notificationRepository.findAllById(ids);
        for (Notification n : notifications) {
            checkAccess(n, currentUser);
        }
        notificationRepository.deleteAll(notifications);
        log.info("[bulkDelete] {} items deleted", notifications.size());
    }

    @Transactional
    public void createNotification(Partners partner, String name, String description, User createdBy) {
        try {
            Notification notification = new Notification();
            notification.setPartner(partner);
            notification.setName(name);
            notification.setDescription(description);
            notification.setCreatedBy(createdBy);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setIsDraft(false);
            notification.setIsSeen(false);
            notification.setQuantity(1);
            notificationRepository.save(notification);
            log.info("Notifikasi baru berhasil dibuat: {} - {}", name, description);
        } catch (Exception e) {
            log.error("Gagal membuat notifikasi: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void createOrUpdateProductNotification(Partners partner, String productName, User createdBy) {
        try {
            java.util.Optional<Notification> existingOpt;
            if (partner == null) {
                existingOpt = notificationRepository.findFirstByPartnerIsNullAndNameAndIsDraftAndIsSeenOrderByCreatedAtDesc(
                        "Product", false, false);
            } else {
                existingOpt = notificationRepository.findFirstByPartnerIdAndNameAndIsDraftAndIsSeenOrderByCreatedAtDesc(
                        partner.getId(), "Product", false, false);
            }

            if (existingOpt.isPresent()) {
                Notification existing = existingOpt.get();
                int newQty = existing.getQuantity() + 1;
                existing.setQuantity(newQty);
                
                String desc = existing.getDescription();
                String productList = "";
                int colonIdx = desc.indexOf(": ");
                if (colonIdx != -1) {
                    productList = desc.substring(colonIdx + 2);
                } else {
                    productList = productName;
                }
                
                existing.setDescription("Menambahkan " + newQty + " product baru: " + productList + ", " + productName);
                existing.setCreatedAt(LocalDateTime.now());
                notificationRepository.save(existing);
                log.info("Notifikasi Product berhasil digabungkan (jumlah: {}): {}", newQty, existing.getDescription());
            } else {
                Notification notification = new Notification();
                notification.setPartner(partner);
                notification.setName("Product");
                notification.setDescription("Menambahkan 1 product baru: " + productName);
                notification.setCreatedBy(createdBy);
                notification.setCreatedAt(LocalDateTime.now());
                notification.setIsDraft(false);
                notification.setIsSeen(false);
                notification.setQuantity(1);
                notificationRepository.save(notification);
                log.info("Notifikasi Product baru dibuat: {}", notification.getDescription());
            }
        } catch (Exception e) {
            log.error("Gagal membuat/menggabungkan notifikasi Product: {}", e.getMessage(), e);
        }
    }

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse res = new NotificationResponse();
        res.setId(notification.getId());
        if (notification.getPartner() != null) {
            res.setPartnerId(notification.getPartner().getId());
            res.setPartnerName(notification.getPartner().getName());
        }
        res.setName(notification.getName());
        res.setDescription(notification.getDescription());
        if (notification.getCreatedBy() != null) {
            res.setCreatedByName(notification.getCreatedBy().getFullname() != null ? 
                    notification.getCreatedBy().getFullname() : notification.getCreatedBy().getUsername());
        } else {
            res.setCreatedByName("System");
        }
        res.setCreatedAt(notification.getCreatedAt());
        res.setIsDraft(notification.getIsDraft());
        res.setIsSeen(notification.getIsSeen());
        res.setQuantity(notification.getQuantity());
        return res;
    }
}

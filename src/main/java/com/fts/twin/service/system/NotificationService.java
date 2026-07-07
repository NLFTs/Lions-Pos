package com.fts.twin.service.system;

import com.fts.twin.dto.response.NotificationResponse;
import com.fts.twin.handler.ResourceNotFoundException;
import com.fts.twin.model.auth.User;
import com.fts.twin.model.common.Partners;
import com.fts.twin.model.system.Notification;
import com.fts.twin.repository.auth.UserRepository;
import com.fts.twin.repository.system.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
            // Cari notifikasi Product yang masih aktif (belum di-draft/delete)
            // Tidak peduli isSeen — tetap merge selama belum di-draft
            Optional<Notification> existingOpt;
            if (partner == null) {
                existingOpt = notificationRepository
                        .findFirstByPartnerIsNullAndNameAndIsDraftOrderByCreatedAtDesc("Product", false);
            } else {
                existingOpt = notificationRepository
                        .findFirstByPartnerIdAndNameAndIsDraftOrderByCreatedAtDesc(partner.getId(), "Product", false);
            }

            if (existingOpt.isPresent()) {
                // Merge ke notifikasi yang sudah ada
                Notification existing = existingOpt.get();
                int newQty = existing.getQuantity() + 1;
                existing.setQuantity(newQty);

                // Ambil daftar produk dari deskripsi sebelumnya
                String desc = existing.getDescription();
                String productList;
                int colonIdx = desc.indexOf(": ");
                if (colonIdx != -1) {
                    productList = desc.substring(colonIdx + 2) + ", " + productName;
                } else {
                    productList = productName;
                }

                existing.setDescription("Menambahkan " + newQty + " product baru: " + productList);
                existing.setCreatedAt(LocalDateTime.now());
                // Reset isSeen=false agar badge bell kembali aktif saat ada produk baru
                existing.setIsSeen(false);
                notificationRepository.save(existing);
                log.info("[ProductNotif] Merged qty={} desc={}", newQty, existing.getDescription());
            } else {
                // Buat notifikasi baru
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
                log.info("[ProductNotif] Created new for: {}", productName);
            }
        } catch (Exception e) {
            log.error("[ProductNotif] Gagal: {}", e.getMessage(), e);
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

package com.fts.twin.repository.system;

import com.fts.twin.model.system.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // ── Paginated queries with JOIN FETCH to avoid LazyInitializationException ──

    @Query(value = "SELECT n FROM Notification n LEFT JOIN FETCH n.partner LEFT JOIN FETCH n.createdBy WHERE n.isDraft = :isDraft",
           countQuery = "SELECT COUNT(n) FROM Notification n WHERE n.isDraft = :isDraft")
    Page<Notification> findByIsDraft(@Param("isDraft") Boolean isDraft, Pageable pageable);

    @Query(value = "SELECT n FROM Notification n LEFT JOIN FETCH n.partner LEFT JOIN FETCH n.createdBy WHERE n.partner.id = :partnerId AND n.isDraft = :isDraft",
           countQuery = "SELECT COUNT(n) FROM Notification n WHERE n.partner.id = :partnerId AND n.isDraft = :isDraft")
    Page<Notification> findByPartnerIdAndIsDraft(@Param("partnerId") Long partnerId, @Param("isDraft") Boolean isDraft, Pageable pageable);

    // ── List queries for unseen marking ──────────────────────────────────────

    List<Notification> findByIsDraftAndIsSeen(Boolean isDraft, Boolean isSeen);
    List<Notification> findByPartnerIdAndIsDraftAndIsSeen(Long partnerId, Boolean isDraft, Boolean isSeen);

    // ── findById with eager fetch (for markAsDraft / delete) ─────────────────

    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.partner LEFT JOIN FETCH n.createdBy WHERE n.id = :id")
    Optional<Notification> findByIdWithDetails(@Param("id") Long id);

    // ── Deduplication for product notifications ───────────────────────────────
    // Merge berdasarkan isDraft=false saja — tidak peduli isSeen,
    // sehingga notifikasi tetap tergabung meski user sudah buka inbox.

    Optional<Notification> findFirstByPartnerIdAndNameAndIsDraftOrderByCreatedAtDesc(
            Long partnerId, String name, Boolean isDraft);

    Optional<Notification> findFirstByPartnerIsNullAndNameAndIsDraftOrderByCreatedAtDesc(
            String name, Boolean isDraft);
}

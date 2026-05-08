package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.StockOpnameItemDTO;
import com.dak.spravel.dto.request.inventory.StockOpnameRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
<<<<<<< HEAD
=======
import com.dak.spravel.model.auth.User;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.StockOpname;
import com.dak.spravel.model.inventory.StockOpnameItem;
<<<<<<< HEAD
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.common.PartnerRepository;
=======
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import com.dak.spravel.repository.inventory.StockOpnameItemRepository;
import com.dak.spravel.repository.inventory.StockOpnameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
<<<<<<< HEAD
=======
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockOpnameService {

    private final StockOpnameRepository stockOpnameRepository;
    private final StockOpnameItemRepository stockOpnameItemRepository;
<<<<<<< HEAD
    private final PartnerRepository partnersRepository;
    private final ProductRepository productRepository;

    // GET ALL
    public List<StockOpname> findAll() {
        return stockOpnameRepository.findByDeletedAtIsNull();
=======
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));

        if (isAdmin(user)) {
            throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola Stock Opname.");
        }

        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));
    }

    private StockOpname getValidatedOpname(Long id, User currentUser) {
        StockOpname opname = stockOpnameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockOpname", id));

        if (currentUser.getPartner() == null ||
                !opname.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Stock opname bukan milik partner Anda.");
        }

        return opname;
    }

    // GET ALL
    public List<StockOpname> findAll() {
        User currentUser = getAuthenticatedUser();
        return stockOpnameRepository.findByPartnerIdAndDeletedAtIsNull(currentUser.getPartner().getId());
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
    }

    // GET ALL PAGINATED
    public Page<StockOpname> findAll(int page, int size) {
<<<<<<< HEAD
        return stockOpnameRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
=======
        User currentUser = getAuthenticatedUser();
        return stockOpnameRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
    }

    // GET BY ID
    public StockOpname findById(Long id) {
<<<<<<< HEAD
        return stockOpnameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockOpname", id));
=======
        User currentUser = getAuthenticatedUser();
        return getValidatedOpname(id, currentUser);
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
    }

    // GET ITEMS BY OPNAME ID
    public List<StockOpnameItem> findItemsByOpnameId(Long opnameId) {
<<<<<<< HEAD
=======
        User currentUser = getAuthenticatedUser();
        getValidatedOpname(opnameId, currentUser);
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
        return stockOpnameItemRepository.findByStockOpnameId(opnameId);
    }

    // CREATE
    @Transactional
    public StockOpname create(StockOpnameRequestDTO request) {
<<<<<<< HEAD
        Partners partner = partnersRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner", request.getPartnerId()));
=======
        User currentUser = getAuthenticatedUser();

        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
        }
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635

        StockOpname opname = new StockOpname();
        opname.setPartner(partner);
        opname.setLocation(request.getLocation());
        opname.setLocationId(request.getLocationId());
        opname.setDate(request.getDate());
        opname.setNotes(request.getNotes());
        opname.setStatus(StockOpname.Status.DRAFT);

        StockOpname saved = stockOpnameRepository.save(opname);

<<<<<<< HEAD
        // Save items
=======
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<StockOpnameItem> items = new ArrayList<>();
            for (StockOpnameItemDTO itemDTO : request.getItems()) {
                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", itemDTO.getProductId()));

<<<<<<< HEAD
=======
                if (!product.getPartner().getId().equals(partner.getId())) {
                    throw new RuntimeException("Akses Ditolak: Product bukan milik partner Anda.");
                }

>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
                StockOpnameItem item = new StockOpnameItem();
                item.setStockOpname(saved);
                item.setProduct(product);
                item.setQtySystem(itemDTO.getQtySystem() != null ? itemDTO.getQtySystem() : BigDecimal.ZERO);
                item.setQtyPhysical(itemDTO.getQtyPhysical() != null ? itemDTO.getQtyPhysical() : BigDecimal.ZERO);
                item.setQtyDifference(item.getQtyPhysical().subtract(item.getQtySystem()));
                item.setNotes(itemDTO.getNotes());
                items.add(item);
            }
            stockOpnameItemRepository.saveAll(items);
        }

        return saved;
    }

    // UPDATE STATUS
    @Transactional
    public StockOpname updateStatus(Long id, String status) {
<<<<<<< HEAD
        StockOpname opname = stockOpnameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockOpname", id));
=======
        User currentUser = getAuthenticatedUser();
        StockOpname opname = getValidatedOpname(id, currentUser);
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635

        StockOpname.Status newStatus = StockOpname.Status.valueOf(status.toUpperCase());
        opname.setStatus(newStatus);

        if (newStatus == StockOpname.Status.RIEVIEWED) {
            opname.setReviewedAt(LocalDateTime.now());
        } else if (newStatus == StockOpname.Status.APPROVED) {
            opname.setApprovedAt(LocalDateTime.now());
        }

        return stockOpnameRepository.save(opname);
    }

    // SOFT DELETE
    public void delete(Long id) {
<<<<<<< HEAD
        StockOpname opname = stockOpnameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockOpname", id));

=======
        User currentUser = getAuthenticatedUser();
        StockOpname opname = getValidatedOpname(id, currentUser);
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
        opname.setDeletedAt(LocalDateTime.now());
        stockOpnameRepository.save(opname);
    }
}
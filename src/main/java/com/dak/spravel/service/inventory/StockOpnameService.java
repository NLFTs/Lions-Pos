package com.dak.spravel.service.inventory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dak.spravel.dto.request.inventory.StockOpnameItemDTO;
import com.dak.spravel.dto.request.inventory.StockOpnameRequestDTO;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.dto.response.inventoryresponse.StockOpnameResponse;
import com.dak.spravel.dto.response.inventoryresponse.StockOpnameResponse.StockOpnameItemResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.StockBalance;
import com.dak.spravel.model.inventory.StockOpname;
import com.dak.spravel.model.inventory.StockOpnameItem;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.inventory.StockBalanceRepository;
import com.dak.spravel.repository.inventory.StockOpnameItemRepository;
import com.dak.spravel.repository.inventory.StockOpnameRepository;
import com.dak.spravel.service.inventory.StockMutationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockOpnameService {

    private final StockOpnameRepository stockOpnameRepository;
    private final StockOpnameItemRepository stockOpnameItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final StockMutationService stockMutationService;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();
        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("owner") ||
                        role.getSlug().equalsIgnoreCase("employee"));

        if (!isAuthorized) {
            throw new RuntimeException("Akses Ditolak: Hanya Owner atau Employee yang diizinkan.");
        }
        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));
    }

    private boolean isAdminPartnerAndEmployee(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("employee") || 
                        role.getSlug().equals("owner"));
    }

    // 💡 HELPER BARU: Deteksi apakah user adalah Employee murni
    private boolean isEmployee(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("employee"));
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

    private StockOpnameResponse mapToResponse(StockOpname opname) {
        PartnerSimpleDto partnerDto = null;
        if (opname.getPartner() != null) {
            partnerDto = new PartnerSimpleDto();
            partnerDto.setId(opname.getPartner().getId());
            partnerDto.setName(opname.getPartner().getName());
        }

        List<StockOpnameResponse.StockOpnameItemResponse> itemResponses =
                stockOpnameItemRepository.findByStockOpnameId(opname.getId())
                        .stream()
                        .map(this::mapItemToResponse)
                        .collect(Collectors.toList());

        return StockOpnameResponse.builder()
                .id(opname.getId())
                .partner(partnerDto)
                .location(opname.getLocation())
                .locationId(opname.getLocationId())
                .date(opname.getDate())
                .status(opname.getStatus())
                .notes(opname.getNotes())
                .reviewedAt(opname.getReviewedAt())
                .approvedAt(opname.getApprovedAt())
                .createdAt(opname.getCreatedAt())
                .updatedAt(opname.getUpdatedAt())
                .deletedAt(opname.getDeletedAt())
                .createdBy(mapUserToSimpleDto(opname.getCreatedBy()))
                .updatedBy(mapUserToSimpleDto(opname.getUpdatedBy()))
                .deletedBy(mapUserToSimpleDto(opname.getDeletedBy()))
                .reviewedBy(mapUserToSimpleDto(opname.getReviewedBy()))
                .approvedBy(mapUserToSimpleDto(opname.getApprovedBy()))
                .items(itemResponses)
                .build();
    }

    private StockOpnameResponse.StockOpnameItemResponse mapItemToResponse(StockOpnameItem item) {
        StockOpnameResponse.ProductSimpleDto productDto = null;

        if (item.getProduct() != null) {
            productDto = new StockOpnameResponse.ProductSimpleDto();
            productDto.setId(item.getProduct().getId());
            productDto.setName(item.getProduct().getName());
            productDto.setSku(item.getProduct().getSku());
        }

        StockOpnameResponse.StockOpnameItemResponse response = new StockOpnameResponse.StockOpnameItemResponse();
        response.setId(item.getId());
        response.setProduct(productDto);
        response.setQtySystem(item.getQtySystem());
        response.setQtyPhysical(item.getQtyPhysical());
        response.setQtyDifference(item.getQtyDifference());
        response.setNotes(item.getNotes());
        response.setCountedBy(mapUserToSimpleDto(item.getCountedBy()));
        response.setCountedAt(item.getCountedAt());

        return response;
    }

    private UserSimpleDto mapUserToSimpleDto(User user) {
        if (user == null) return null;
        UserSimpleDto dto = new UserSimpleDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }

    public List<StockOpnameResponse> findAllOpName() {
        User currentUser = getAuthenticatedUser();
        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Hanya Super Admin yang diperbolehkan.");
        }
        List<StockOpname> allStockOpnames = stockOpnameRepository.findByDeletedAtIsNull();
        return allStockOpnames.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // RIWAYAT OPNAME (Bisa dibaca oleh Employee)
    public List<StockOpnameResponse> findAll() {
        User currentUser = getAuthenticatedUser();

        if (isAdminPartnerAndEmployee(currentUser)) {
            if (currentUser.getPartner() == null) {
                throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
            }
            List<StockOpname> opnames = stockOpnameRepository.findByPartnerIdAndDeletedAtIsNull(currentUser.getPartner().getId());
            return opnames.stream().map(this::mapToResponse).collect(Collectors.toList());
        }

        List<StockOpname> allStockOpnames = stockOpnameRepository.findByDeletedAtIsNull();
        return allStockOpnames.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public Page<StockOpnameResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (isAdminPartnerAndEmployee(currentUser)) {
            if (currentUser.getPartner() == null) {
                throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
            }
            Page<StockOpname> opnames = stockOpnameRepository.findByPartnerIdAndDeletedAtIsNull(currentUser.getPartner().getId(), pageRequest);
            return opnames.map(this::mapToResponse);
        }

        Page<StockOpname> opnames = stockOpnameRepository.findByDeletedAtIsNull(pageRequest);
        return opnames.map(this::mapToResponse);
    }

    public StockOpnameResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        return mapToResponse(getValidatedOpname(id, currentUser));
    }

    public List<StockOpnameItemResponse> findItemsByOpnameId(Long opnameId) {
        User currentUser = getAuthenticatedUser();
        getValidatedOpname(opnameId, currentUser);

        List<StockOpnameItem> items = stockOpnameItemRepository.findByStockOpnameId(opnameId);
        return items.stream().map(this::mapItemToResponse).collect(Collectors.toList());
    }

    // =============================================================
    // CREATE SESI OPNAME — ❌ BLOKIR JIKA EMPLOYEE
    // =============================================================
    @Transactional
    public StockOpnameResponse create(StockOpnameRequestDTO request) {
        User currentUser = getAuthenticatedUser();

        // 🔥 VALIDASI: Employee dilarang membuat sesi opname baru
        if (isEmployee(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Employee tidak diizinkan membuat sesi opname.");
        }

        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
        }

        StockOpname opname = new StockOpname();
        opname.setPartner(partner);
        opname.setLocation(request.getLocationType());
        opname.setLocationId(request.getLocationId());
        opname.setDate(request.getDate());
        opname.setNotes(request.getNotes());
        opname.setStatus(StockOpname.Status.DRAFT);

        StockOpname saved = stockOpnameRepository.save(opname);

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<StockOpnameItem> items = new ArrayList<>();
            for (StockOpnameItemDTO itemDTO : request.getItems()) {
                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", itemDTO.getProductId()));

                if (!product.getPartner().getId().equals(partner.getId())) {
                    throw new RuntimeException("Akses Ditolak: Product bukan milik partner Anda.");
                }

                long qtySystem = stockBalanceRepository
                        .findByProductIdAndLocationTypeAndLocationId(
                                product.getId(),
                                request.getLocationType().toUpperCase(),
                                request.getLocationId())
                        .map(sb -> sb.getQty() != null ? sb.getQty() : 0L)
                        .orElse(0L);

                StockOpnameItem item = new StockOpnameItem();
                item.setStockOpname(saved);
                item.setProduct(product);
                item.setQtySystem(qtySystem);

                long qtyPhysical = itemDTO.getQtyPhysical() != null ? itemDTO.getQtyPhysical() : 0L;
                item.setQtyPhysical(qtyPhysical);
                
                long qtyDifference = qtyPhysical - qtySystem;
                item.setQtyDifference(qtyDifference);

                item.setNotes(itemDTO.getNotes());
                items.add(item);
            }
            stockOpnameItemRepository.saveAll(items);
        }
        return mapToResponse(saved);
    }

    // =============================================================
    // INPUT HITUNGAN FISIK DI LAPANGAN —  DIIZINKAN UNTUK EMPLOYEE
    // =============================================================
    @Transactional
    public StockOpnameResponse inputQtyPhysical(Long opnameId, List<StockOpnameItemDTO> itemsInput) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        StockOpname opname = getValidatedOpname(opnameId, currentUser);

        // Validasi: Hanya bisa input jika status sesi opname masih DRAFT
        if (opname.getStatus() != StockOpname.Status.DRAFT) {
            throw new RuntimeException("Akses Ditolak: Tidak bisa mengisi hitungan fisik pada sesi opname yang sudah diproses.");
        }

        for (StockOpnameItemDTO itemDTO : itemsInput) {
            StockOpnameItem item = stockOpnameItemRepository.findByStockOpnameId(opnameId)
                    .stream()
                    .filter(i -> i.getProduct().getId().equals(itemDTO.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Produk tidak ditemukan dalam daftar sesi opname ini"));

            long qtyPhysical = itemDTO.getQtyPhysical() != null ? itemDTO.getQtyPhysical() : 0L;
            item.setQtyPhysical(qtyPhysical);
            
            // Hitung selisih otomatis
            long qtySystem = item.getQtySystem() != null ? item.getQtySystem() : 0L;
            item.setQtyDifference(qtyPhysical - qtySystem);
            item.setCountedBy(currentUser);
            item.setCountedAt(LocalDateTime.now());
            if (itemDTO.getNotes() != null) {
                item.setNotes(itemDTO.getNotes());
            }

            stockOpnameItemRepository.save(item);
        }

        return mapToResponse(opname);
    }

    // =============================================================
    // MANAGEMENT / UPDATE STATUS (APPROVE/REVIEW/ADJUSTED) — ❌ BLOKIR JIKA EMPLOYEE
    // =============================================================
    @Transactional
    public StockOpname updateStatus(Long id, String status) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        // 🔥 VALIDASI: Employee tidak boleh menyetujui (Approve) atau mengelola eksekusi opname
        if (isEmployee(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Employee tidak memiliki hak untuk memproses status manajemen Opname.");
        }

        StockOpname stockOpname = getValidatedOpname(id, currentUser);

        String statusUpper = status.toUpperCase();
        StockOpname.Status newStatus = StockOpname.Status.valueOf(statusUpper);

        // Validasi transisi status yang diizinkan
        if (newStatus == StockOpname.Status.ADJUSTED && stockOpname.getStatus() != StockOpname.Status.APPROVED) {
            throw new RuntimeException("Gagal: Opname harus berstatus APPROVED sebelum bisa di-ADJUSTED.");
        }

        // Set status baru
        stockOpname.setStatus(newStatus);

        if (newStatus == StockOpname.Status.REVIEWED) {
            stockOpname.setReviewedAt(LocalDateTime.now());
            stockOpname.setReviewedBy(currentUser);
        }
        else if (newStatus == StockOpname.Status.APPROVED) {
            // BUG FIX #2a: APPROVED hanya mencatat siapa yang approve & kapan.
            // Tidak ada perubahan stok di sini — itu dilakukan saat ADJUSTED.
            stockOpname.setApprovedAt(LocalDateTime.now());
            stockOpname.setApprovedBy(currentUser);

            // Hitung ulang qty_difference untuk memastikan data akurat
            List<StockOpnameItem> stockOpnameItems = stockOpnameItemRepository
                    .findByStockOpnameId(stockOpname.getId());

            for (StockOpnameItem stockOpnameItem : stockOpnameItems) {
                Long qtySystem = stockOpnameItem.getQtySystem() != null ? stockOpnameItem.getQtySystem() : 0L;
                Long qtyPhysical = stockOpnameItem.getQtyPhysical() != null ? stockOpnameItem.getQtyPhysical() : 0L;
                Long qtyDifference = qtyPhysical - qtySystem;
                stockOpnameItem.setQtyDifference(qtyDifference);

                if (qtyDifference < 0) {
                    stockOpnameItem.setNotes("STOK MINUS " + qtyDifference);
                } else if (qtyDifference > 0) {
                    stockOpnameItem.setNotes("STOK PLUS " + qtyDifference);
                } else {
                    stockOpnameItem.setNotes("STOK SESUAI");
                }
                stockOpnameItemRepository.save(stockOpnameItem);
            }
        }
        else if (newStatus == StockOpname.Status.ADJUSTED) {
            // BUG FIX #2b: ADJUSTED adalah status yang benar-benar menerapkan koreksi stok
            List<StockOpnameItem> stockOpnameItems = stockOpnameItemRepository
                    .findByStockOpnameId(stockOpname.getId());

            for (StockOpnameItem stockOpnameItem : stockOpnameItems) {
                Long qtyDifference = stockOpnameItem.getQtyDifference() != null
                        ? stockOpnameItem.getQtyDifference() : 0L;

                // Hanya proses item yang ada selisih
                if (qtyDifference == 0) continue;

                if (stockOpnameItem.getProduct() != null) {
                    // Update stock balance dengan menambahkan selisih (bukan set ke qtyPhysical)
                    StockBalance stockBalance = stockBalanceRepository
                            .findByProductIdAndLocationTypeAndLocationId(
                                    stockOpnameItem.getProduct().getId(),
                                    stockOpname.getLocation(),
                                    stockOpname.getLocationId()
                            ).orElse(new StockBalance());

                    if (stockBalance.getId() == null) {
                        stockBalance.setProduct(stockOpnameItem.getProduct());
                        stockBalance.setLocationType(stockOpname.getLocation());
                        stockBalance.setLocationId(stockOpname.getLocationId());
                        stockBalance.setCreatedBy(currentUser);
                    }

                    long currentQty = stockBalance.getQty() != null ? stockBalance.getQty() : 0L;
                    stockBalance.setQty(currentQty + qtyDifference);
                    stockBalance.setUpdatedBy(currentUser);
                    stockBalanceRepository.save(stockBalance);

                    // Catat StockMutation type=ADJUSTMENT
                    stockMutationService.recordMutation(
                            stockOpnameItem.getProduct(),
                            stockOpname.getPartner(),
                            "ADJUSTMENT",
                            stockOpname.getLocation(), stockOpname.getLocationId(),
                            stockOpname.getLocation(), stockOpname.getLocationId(),
                            Math.abs(qtyDifference),
                            "stock_opname", stockOpname.getId(),
                            "Opname #" + stockOpname.getId() + " adjustment: "
                                    + (qtyDifference > 0 ? "+" : "") + qtyDifference,
                            currentUser
                    );
                }
            }
        }

        stockOpname.setUpdatedAt(LocalDateTime.now());
        stockOpname.setUpdatedBy(currentUser);

        return stockOpnameRepository.save(stockOpname);
    }

    // =============================================================
    // DELETE SESI OPNAME — ❌ BLOKIR JIKA EMPLOYEE
    // =============================================================
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();

        // 🔥 VALIDASI: Employee dilarang menghapus sesi opname
        if (isEmployee(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Employee tidak diizinkan untuk menghapus data opname.");
        }

        StockOpname opname = getValidatedOpname(id, currentUser);
        opname.setDeletedAt(LocalDateTime.now());
        stockOpnameRepository.save(opname);
    }
}
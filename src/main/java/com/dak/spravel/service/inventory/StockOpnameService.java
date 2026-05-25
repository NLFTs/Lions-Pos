package com.dak.spravel.service.inventory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    // ─── 🔒 PUSAT VALIDASI AUTH & PERMISSION (MURNI DINAMIS) ───────────────────

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    // 🔥 KUNCI DINAMIS: Check permission dinamis dari database tanpa kaku nge-lock nama role
    private void checkPermission(User user, String permissionSlug) {
        // 👑 Raja Super Admin (partner null) bypass seluruh jenis gate permission
        if (user.getPartner() == null) {
            return;
        }

        boolean hasPerm = user.getRoles().stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(perm -> perm.getSlug().equalsIgnoreCase(permissionSlug));

        if (!hasPerm) {
            throw new RuntimeException("Akses Ditolak: Anda tidak memiliki hak akses '" + permissionSlug + "'!");
        }
    }

    private void checkSuperAdminOnly(User user) {
        if (user.getPartner() != null) {
            throw new RuntimeException("Akses Ditolak: Fitur ini khusus Super Admin Global.");
        }
    }

    // ─── 🛡️ MULTI-TENANT GUARD (ANTI NULL POINTER UNTUK SUPER ADMIN) ───────────

    private StockOpname getValidatedOpname(Long id, User currentUser) {
        StockOpname opname = stockOpnameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockOpname", id));

        // 👑 Super Admin global bebas bypass pengecekan tenant id
        if (currentUser.getPartner() == null) {
            return opname;
        }

        if (opname.getPartner() == null || !opname.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Data Stock Opname bukan milik partner Anda");
        }

        return opname;
    }

    // ─── 🚀 MAIN METHODSCORE (SUDAH DISERAGAMKAN POLANYA) ──────────────────────

    // KHUSUS SUPER ADMIN GLOBAL

    public List<StockOpnameResponse> findAllAdmin() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_opname.index"); // 💡 Saring via permission index
        checkSuperAdminOnly(currentUser);

        return stockOpnameRepository.findByDeletedAtIsNull()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<StockOpnameResponse> findPageAdmin(int page, int size) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_opname.index"); // 💡 Saring via permission index
        checkSuperAdminOnly(currentUser);

        return stockOpnameRepository.findByDeletedAtIsNull(
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToResponse);
    }

    // OPERASIONAL TENANT / PARTNER (BERBASIS PERMISSION SLUG)

    public List<StockOpnameResponse> findAll() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_opname.index"); // 💡 Saring via permission index

        // 👑 Handling Super Admin: Lihat semua data opname aktif global
        if (currentUser.getPartner() == null) {
            return stockOpnameRepository.findByDeletedAtIsNull()
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        // 🏢 Handling Tenant: Hanya ambil data opname milik perusahaannya sendiri
        return stockOpnameRepository
                .findByPartnerIdAndDeletedAtIsNull(currentUser.getPartner().getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public StockOpnameResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_opname.show");
        return mapToResponse(getValidatedOpname(id, currentUser));
    }

    public List<StockOpnameItemResponse> findItemsByOpnameId(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_opname.show");
        getValidatedOpname(id, currentUser);

        return stockOpnameItemRepository.findByStockOpnameId(id)
                .stream()
                .map(this::mapItemToResponse)
                .toList();
    }

    public Page<StockOpnameResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_opname.index");
    
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    
        // 👑 LOGIC IF-ELSE MULTI-TENANT
        if (currentUser.getPartner() == null) {
            return stockOpnameRepository.findAll(pageable)
                    .map(this::mapToResponse);
        } else {
            return stockOpnameRepository.findByPartnerIdAndDeletedAtIsNull(
                        currentUser.getPartner().getId(), 
                        pageable
                    )
                    .map(this::mapToResponse);
        }
    }

    // FORMULASI PEMBUATAN DRAFT DOKUMEN OPNAME
    @Transactional
    public StockOpnameResponse create(StockOpnameRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_opname.store"); // 💡 Siapapun boleh buat asal diberi izin Owner via UI
        
        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("Akses Ditolak: Super Admin Global tidak diperbolehkan membuat dokumen opname langsung.");
        }

        StockOpname opname = new StockOpname();
        opname.setPartner(partner);
        opname.setLocation(request.getLocationType().toUpperCase());
        opname.setLocationId(request.getLocationId());
        opname.setDate(request.getDate());
        opname.setNotes(request.getNotes());
        opname.setStatus(StockOpname.Status.DRAFT);
        opname.setCreatedBy(currentUser);
        opname.setCreatedAt(LocalDateTime.now());

        StockOpname saved = stockOpnameRepository.save(opname);

        if (request.getItems() != null) {
            List<StockOpnameItem> items = new ArrayList<>();

            for (StockOpnameItemDTO dto : request.getItems()) {
                Product product = productRepository.findById(dto.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", dto.getProductId()));

                if (product.getPartner() == null || !product.getPartner().getId().equals(partner.getId())) {
                    throw new RuntimeException("Akses Ditolak: Product '" + product.getName() + "' bukan milik partner Anda");
                }

                // Ambil snapshot kuantitas sistem saat ini secara real-time
                long systemQty = stockBalanceRepository
                        .findByProductIdAndLocationTypeAndLocationId(
                                product.getId(),
                                request.getLocationType().toUpperCase(),
                                request.getLocationId()
                        )
                        .map(sb -> sb.getQty() == null ? 0L : sb.getQty())
                        .orElse(0L);

                StockOpnameItem item = new StockOpnameItem();
                item.setStockOpname(saved);
                item.setProduct(product);
                item.setQtySystem(systemQty);

                long physical = dto.getQtyPhysical() == null ? 0L : dto.getQtyPhysical();
                item.setQtyPhysical(physical);
                item.setQtyDifference(physical - systemQty);
                item.setNotes(dto.getNotes());

                items.add(item);
            }

            stockOpnameItemRepository.saveAll(items);
        }

        return mapToResponse(saved);
    }

    // INPUT HASIL PERHITUNGAN FISIK LAPANGAN
    @Transactional
    public StockOpnameResponse inputQtyPhysical(Long opnameId, List<StockOpnameItemDTO> itemsInput) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_opname.update");
        
        StockOpname opname = getValidatedOpname(opnameId, currentUser);

        if (opname.getStatus() != StockOpname.Status.DRAFT) {
            throw new RuntimeException("Gagal: Dokumen opname ini sudah diproses dan dikunci.");
        }

        for (StockOpnameItemDTO dto : itemsInput) {
            StockOpnameItem item = stockOpnameItemRepository.findByStockOpnameId(opnameId)
                    .stream()
                    .filter(i -> i.getProduct().getId().equals(dto.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Item produk tidak ditemukan di dokumen opname ini."));

            long physical = dto.getQtyPhysical() == null ? 0L : dto.getQtyPhysical();
            long system = item.getQtySystem() == null ? 0L : item.getQtySystem();

            item.setQtyPhysical(physical);
            item.setQtyDifference(physical - system);
            item.setCountedBy(currentUser);
            item.setCountedAt(LocalDateTime.now());
            item.setNotes(dto.getNotes());

            stockOpnameItemRepository.save(item);
        }

        return mapToResponse(opname);
    }

    // SINKRONISASI WORKFLOW STATUS DOKUMEN
    @Transactional
    public StockOpname updateStatus(Long id, String status) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_opname.update");
        
        StockOpname opname = getValidatedOpname(id, currentUser);
        StockOpname.Status newStatus = StockOpname.Status.valueOf(status.toUpperCase());

        opname.setStatus(newStatus);

        if (newStatus == StockOpname.Status.REVIEWED) {
            opname.setReviewedAt(LocalDateTime.now());
            opname.setReviewedBy(currentUser);
        }

        if (newStatus == StockOpname.Status.APPROVED) {
            opname.setApprovedAt(LocalDateTime.now());
            opname.setApprovedBy(currentUser);
        }

        // ⚙️ EKSEKUSI PENYESUAIAN STOK JIKA STATUS BERUBAH MENJADI ADJUSTED
        if (newStatus == StockOpname.Status.ADJUSTED) {
            applyAdjustment(opname, currentUser);
        }

        opname.setUpdatedAt(LocalDateTime.now());
        opname.setUpdatedBy(currentUser);

        return stockOpnameRepository.save(opname);
    }

    // PROSES EKSEKUSI UPDATE STOK BALANCE & INPUT AUDIT LOG MUTASI
    private void applyAdjustment(StockOpname opname, User user) {
        List<StockOpnameItem> items = stockOpnameItemRepository.findByStockOpnameId(opname.getId());

        for (StockOpnameItem item : items) {
            long diff = item.getQtyDifference() == null ? 0L : item.getQtyDifference();
            if (diff == 0) continue; // Jika selisih nol, lewati (stok klop aman jaya)

            StockBalance balance = stockBalanceRepository
                    .findByProductIdAndLocationTypeAndLocationId(
                            item.getProduct().getId(),
                            opname.getLocation().toUpperCase(),
                            opname.getLocationId()
                    )
                    .orElse(new StockBalance());

            if (balance.getId() == null) {
                balance.setProduct(item.getProduct());
                balance.setLocationType(opname.getLocation().toUpperCase());
                balance.setLocationId(opname.getLocationId());
                balance.setCreatedBy(user);
                balance.setCreatedAt(LocalDateTime.now());
            }

            long current = balance.getQty() == null ? 0L : balance.getQty();
            balance.setQty(current + diff);
            balance.setUpdatedBy(user);
            balance.setUpdatedAt(LocalDateTime.now());

            stockBalanceRepository.save(balance);

            // Suntik otomatis ke rekaman audit histori mutasi barang Spravel
            stockMutationService.recordMutation(
                    item.getProduct(),
                    opname.getPartner(),
                    "ADJUSTMENT",
                    opname.getLocation().toUpperCase(),
                    opname.getLocationId(),
                    opname.getLocation().toUpperCase(),
                    opname.getLocationId(),
                    Math.abs(diff),
                    "STOCK_OPNAME",
                    opname.getId(),
                    "Adjustment Opname #" + opname.getId() + " - Notes: " + item.getNotes(),
                    user
            );
        }
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_opname.delete"); // 💡 Sikat via permission delete
        
        StockOpname opname = getValidatedOpname(id, currentUser);

        opname.setDeletedAt(LocalDateTime.now());
        opname.setDeletedBy(currentUser);

        stockOpnameRepository.save(opname);
    }

    // ─── 🔄 UTILS MAPPERS SECTION ───────────────────────────────────────────

    private StockOpnameResponse mapToResponse(StockOpname opname) {
        PartnerSimpleDto partnerDto = null;
        if (opname.getPartner() != null) {
            partnerDto = new PartnerSimpleDto();
            partnerDto.setId(opname.getPartner().getId());
            partnerDto.setName(opname.getPartner().getName());
        }

        List<StockOpnameItemResponse> itemResponses =
                stockOpnameItemRepository.findByStockOpnameId(opname.getId())
                        .stream()
                        .map(this::mapItemToResponse)
                        .toList();

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

    private StockOpnameItemResponse mapItemToResponse(StockOpnameItem item) {
        StockOpnameResponse.ProductSimpleDto productDto = null;

        if (item.getProduct() != null) {
            productDto = new StockOpnameResponse.ProductSimpleDto();
            productDto.setId(item.getProduct().getId());
            productDto.setName(item.getProduct().getName());
            productDto.setSku(item.getProduct().getSku());
        }

        StockOpnameItemResponse response = new StockOpnameItemResponse();
        response.setId(item.getId());
        response.setProduct(productDto);
        response.setQtySystem(item.getQtySystem());
        response.setQtyPhysical(item.getQtyPhysical());
        response.setQtyDifference(item.getQtyDifference());
        response.setNotes(item.getNotes());
        response.setCountedAt(item.getCountedAt());
        response.setCountedBy(mapUserToSimpleDto(item.getCountedBy()));

        return response;
    }

    private UserSimpleDto mapUserToSimpleDto(User user) {
        if (user == null) return null;
        UserSimpleDto dto = new UserSimpleDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }
}
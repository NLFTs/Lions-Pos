package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.StockOpnameItemDTO;
import com.dak.spravel.dto.request.inventory.StockOpnameRequestDTO;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.dto.response.inventoryresponse.StockOpnameResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.StockOpname;
import com.dak.spravel.model.inventory.StockOpnameItem;
import com.dak.spravel.model.inventory.StockBalance; // Import Model StockBalance
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.inventory.StockOpnameItemRepository;
import com.dak.spravel.repository.inventory.StockOpnameRepository;
import com.dak.spravel.repository.inventory.StockBalanceRepository; // Import Repository StockBalance
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockOpnameService {

    private final StockOpnameRepository stockOpnameRepository;
    private final StockOpnameItemRepository stockOpnameItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StockBalanceRepository stockBalanceRepository; // Ditambahkan untuk sinkronisasi stok

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

    private User getAuthenticatedSuperAdmin() {
        User user = getAuthenticatedUser();
        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase("SUPER_ADMIN"));
        if (!isSuperAdmin) throw new RuntimeException("Akses ditolak: Anda bukan Super Admin");
        return user;
    }

    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();
        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase("ADMIN_PARTNER") ||
                        role.getName().equalsIgnoreCase("EMPLOYEE"));

        boolean isStaff = !user.getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase("SUPER_ADMIN"));

        if (!isAuthorized || !isStaff) {
            throw new RuntimeException("Akses Ditolak: Hanya Admin Partner atau Employee yang diizinkan.");
        }
        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));
    }

    private boolean isAdminPartnerAndEmployee(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("employee") || role.getSlug().equals("admin-partners"));
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
        if (isAdminPartnerAndEmployee(currentUser)) {
            throw new RuntimeException("Akses Di Tolak: Admin Partner Dan Employee Tidak Di Perbolehkan Melihat Semua StockOpname");
        }
        List<StockOpname> allStockOpnames = stockOpnameRepository.findAll();
        return allStockOpnames.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<StockOpname> findAll() {
        User currentUser = getAuthenticatedUser();
        return stockOpnameRepository.findByPartnerIdAndDeletedAtIsNull(currentUser.getPartner().getId());
    }

    public Page<StockOpname> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        return stockOpnameRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public StockOpname findById(Long id) {
        User currentUser = getAuthenticatedUser();
        return getValidatedOpname(id, currentUser);
    }

    public List<StockOpnameItem> findItemsByOpnameId(Long opnameId) {
        User currentUser = getAuthenticatedUser();
        getValidatedOpname(opnameId, currentUser);
        return stockOpnameItemRepository.findByStockOpnameId(opnameId);
    }

    @Transactional
    public StockOpname create(StockOpnameRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
        }

        StockOpname opname = new StockOpname();
        opname.setPartner(partner);
        opname.setLocation(request.getLocation());
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

    @Transactional
    public StockOpname updateStatus(Long id, String status) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        StockOpname stockOpname = getValidatedOpname(id, currentUser);

        StockOpname.Status newStatus = StockOpname.Status.valueOf(status.toUpperCase());
        stockOpname.setStatus(newStatus);

        if (newStatus == StockOpname.Status.RIEVIEWED || status.equalsIgnoreCase("RIEVIEWED")) {
            stockOpname.setReviewedAt(LocalDateTime.now());
            stockOpname.setReviewedBy(currentUser);
        }
        else if (newStatus == StockOpname.Status.APPROVED) {
            stockOpname.setApprovedAt(LocalDateTime.now());
            stockOpname.setApprovedBy(currentUser);

            List<StockOpnameItem> stockOpnameItems = stockOpnameItemRepository
                    .findByStockOpnameId(stockOpname.getId());

            for (StockOpnameItem stockOpnameItem : stockOpnameItems) {
                Long qtySystem = stockOpnameItem.getQtySystem() != null ? stockOpnameItem.getQtySystem().longValue() : 0L;
                Long qtyPhysical = stockOpnameItem.getQtyPhysical() != null ? stockOpnameItem.getQtyPhysical().longValue() : 0L;

                Long qtyDifference = qtyPhysical - qtySystem;
                stockOpnameItem.setQtyDifference(BigDecimal.valueOf(qtyDifference));

                if (qtyDifference < 0) {
                    stockOpnameItem.setNotes("STOK MINUS " + qtyDifference);
                } else if (qtyDifference > 0) {
                    stockOpnameItem.setNotes("STOK PLUS " + qtyDifference);
                } else {
                    stockOpnameItem.setNotes("STOK SESUAI");
                }

                stockOpnameItemRepository.save(stockOpnameItem);

                if (stockOpnameItem.getProduct() != null) {
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

                    stockBalance.setQty(qtyPhysical);
                    stockBalance.setUpdatedBy(currentUser);

                    stockBalanceRepository.save(stockBalance);
                }
            }
        }

        stockOpname.setUpdatedAt(LocalDateTime.now());
        stockOpname.setUpdatedBy(currentUser);

        return stockOpnameRepository.save(stockOpname);
    }

    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        StockOpname opname = getValidatedOpname(id, currentUser);
        opname.setDeletedAt(LocalDateTime.now());
        stockOpnameRepository.save(opname);
    }
}
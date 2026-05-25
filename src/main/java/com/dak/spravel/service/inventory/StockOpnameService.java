package com.dak.spravel.service.inventory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private User getAuthenticatedSuperAdmin() {
        User user = getAuthenticatedUser();

        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getSlug().equalsIgnoreCase("super_admin"));

        if (!isSuperAdmin) {
            throw new RuntimeException("Akses ditolak: hanya Super Admin");
        }

        return user;
    }

    private User getAuthenticatedOwner() {
        User user = getAuthenticatedUser();

        boolean isOwner = user.getRoles().stream()
                .anyMatch(r -> r.getSlug().equalsIgnoreCase("owner"));

        boolean isNotAdmin = user.getRoles().stream()
                .noneMatch(r -> r.getSlug().equalsIgnoreCase("admin"));

        if (!isOwner || !isNotAdmin) {
            throw new RuntimeException("Akses Ditolak: Hanya Owner yang diizinkan");
        }

        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(r -> r.getSlug().equalsIgnoreCase("admin")
                        || r.getSlug().equalsIgnoreCase("super_admin"));
    }

    private boolean isOwner(User user) {
        return user.getRoles().stream()
                .anyMatch(r -> r.getSlug().equalsIgnoreCase("owner"));
    }

    private StockOpname getValidatedOpname(Long id, User user) {
        StockOpname opname = stockOpnameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockOpname", id));

        if (user.getPartner() == null ||
                !opname.getPartner().getId().equals(user.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: bukan milik partner Anda");
        }

        return opname;
    }


    public List<StockOpnameResponse> findAllAdmin() {
        getAuthenticatedSuperAdmin();

        return stockOpnameRepository.findByDeletedAtIsNull()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<StockOpnameResponse> findPageAdmin(int page, int size) {
        getAuthenticatedSuperAdmin();

        return stockOpnameRepository.findByDeletedAtIsNull(
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToResponse);
    }

    // =====================================================
    // OWNER / USER
    // =====================================================

    public List<StockOpnameResponse> findAll() {
        User user = getAuthenticatedUser();

        if (isOwner(user)) {
            if (user.getPartner() == null) {
                throw new RuntimeException("Partner tidak ditemukan");
            }

            return stockOpnameRepository
                    .findByPartnerIdAndDeletedAtIsNull(user.getPartner().getId())
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        return stockOpnameRepository.findByDeletedAtIsNull()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public StockOpnameResponse findById(Long id) {
        User user = getAuthenticatedUser();
        return mapToResponse(getValidatedOpname(id, user));
    }

    public List<StockOpnameItemResponse> findItemsByOpnameId(Long id) {
        User user = getAuthenticatedUser();
        getValidatedOpname(id, user);

        return stockOpnameItemRepository.findByStockOpnameId(id)
                .stream()
                .map(this::mapItemToResponse)
                .toList();
    }

    @Transactional
    public StockOpnameResponse create(StockOpnameRequestDTO request) {
        User user = getAuthenticatedOwner();
        Partners partner = user.getPartner();

        if (partner == null) {
            throw new RuntimeException("Partner tidak ditemukan");
        }

        StockOpname opname = new StockOpname();
        opname.setPartner(partner);
        opname.setLocation(request.getLocationType());
        opname.setLocationId(request.getLocationId());
        opname.setDate(request.getDate());
        opname.setNotes(request.getNotes());
        opname.setStatus(StockOpname.Status.DRAFT);
        opname.setCreatedBy(user);

        StockOpname saved = stockOpnameRepository.save(opname);

        if (request.getItems() != null) {
            List<StockOpnameItem> items = new ArrayList<>();

            for (StockOpnameItemDTO dto : request.getItems()) {

                Product product = productRepository.findById(dto.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", dto.getProductId()));

                if (!product.getPartner().getId().equals(partner.getId())) {
                    throw new RuntimeException("Product bukan milik partner Anda");
                }

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

    // =====================================================
    // INPUT PHYSICAL
    // =====================================================

    @Transactional
    public StockOpnameResponse inputQtyPhysical(Long opnameId, List<StockOpnameItemDTO> itemsInput) {
        User user = getAuthenticatedOwner();
        StockOpname opname = getValidatedOpname(opnameId, user);

        if (opname.getStatus() != StockOpname.Status.DRAFT) {
            throw new RuntimeException("Opname sudah diproses");
        }

        for (StockOpnameItemDTO dto : itemsInput) {

            StockOpnameItem item = stockOpnameItemRepository.findByStockOpnameId(opnameId)
                    .stream()
                    .filter(i -> i.getProduct().getId().equals(dto.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Item tidak ditemukan"));

            long physical = dto.getQtyPhysical() == null ? 0L : dto.getQtyPhysical();
            long system = item.getQtySystem() == null ? 0L : item.getQtySystem();

            item.setQtyPhysical(physical);
            item.setQtyDifference(physical - system);
            item.setCountedBy(user);
            item.setCountedAt(LocalDateTime.now());
            item.setNotes(dto.getNotes());

            stockOpnameItemRepository.save(item);
        }

        return mapToResponse(opname);
    }

    // =====================================================
    // STATUS FLOW
    // =====================================================

    @Transactional
    public StockOpname updateStatus(Long id, String status) {
        User user = getAuthenticatedOwner();
        StockOpname opname = getValidatedOpname(id, user);

        StockOpname.Status newStatus =
                StockOpname.Status.valueOf(status.toUpperCase());

        opname.setStatus(newStatus);

        if (newStatus == StockOpname.Status.REVIEWED) {
            opname.setReviewedAt(LocalDateTime.now());
            opname.setReviewedBy(user);
        }

        if (newStatus == StockOpname.Status.APPROVED) {
            opname.setApprovedAt(LocalDateTime.now());
            opname.setApprovedBy(user);
        }

        if (newStatus == StockOpname.Status.ADJUSTED) {
            applyAdjustment(opname, user);
        }

        opname.setUpdatedAt(LocalDateTime.now());
        opname.setUpdatedBy(user);

        return stockOpnameRepository.save(opname);
    }

    private void applyAdjustment(StockOpname opname, User user) {

        List<StockOpnameItem> items =
                stockOpnameItemRepository.findByStockOpnameId(opname.getId());

        for (StockOpnameItem item : items) {

            long diff = item.getQtyDifference() == null ? 0L : item.getQtyDifference();
            if (diff == 0) continue;

            StockBalance balance = stockBalanceRepository
                    .findByProductIdAndLocationTypeAndLocationId(
                            item.getProduct().getId(),
                            opname.getLocation(),
                            opname.getLocationId()
                    )
                    .orElse(new StockBalance());

            if (balance.getId() == null) {
                balance.setProduct(item.getProduct());
                balance.setLocationType(opname.getLocation());
                balance.setLocationId(opname.getLocationId());
                balance.setCreatedBy(user);
            }

            long current = balance.getQty() == null ? 0L : balance.getQty();
            balance.setQty(current + diff);
            balance.setUpdatedBy(user);

            stockBalanceRepository.save(balance);

            stockMutationService.recordMutation(
                    item.getProduct(),
                    opname.getPartner(),
                    "ADJUSTMENT",
                    opname.getLocation(),
                    opname.getLocationId(),
                    opname.getLocation(),
                    opname.getLocationId(),
                    Math.abs(diff),
                    "stock_opname",
                    opname.getId(),
                    "Adjustment #" + opname.getId(),
                    user
            );
        }
    }

    public void delete(Long id) {
        User user = getAuthenticatedOwner();
        StockOpname opname = getValidatedOpname(id, user);

        opname.setDeletedAt(LocalDateTime.now());
        opname.setDeletedBy(user);

        stockOpnameRepository.save(opname);
    }


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
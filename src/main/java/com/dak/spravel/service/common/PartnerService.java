package com.dak.spravel.service.common;

import java.util.List;

// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dak.spravel.dto.request.partner.CreatePartnerRequest;
import com.dak.spravel.dto.request.partner.GetPartnerByPlan;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.util.AuditHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PartnerService {
    // private final Logger log = LogManager.getLogger(PartnerService.class);
    private final PartnerRepository partnerRepository;

    public List<Partners> getAllPartners() {
        return partnerRepository.findAll();
    }

    public Partners createPartner(CreatePartnerRequest request) {
        if (partnerRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Partner with name " + request.getName() + " already exists");
        }

        Partners partner = new Partners();
        partner.setName(request.getName());
        partner.setPlan(request.getPlan());

        AuditHelper.setCreated(partner);
       
        String generateSlug = request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-");
        partner.setSlug(generateSlug);
        
        return partnerRepository.save(partner);
    }

    public List<Partners> getPartnersByPlan(GetPartnerByPlan request) {
        return partnerRepository.findByPlan(request.getPlan());
    }

    @Transactional
    public Partners softDeletePartner(Long id) {
        Partners partner = partnerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Partner with id " + id + " not found"));
        partner.setIsActive(false);

        AuditHelper.setUpdated(partner);
        return partnerRepository.save(partner);
    }
    
}

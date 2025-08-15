package com.saasstarterkit.organisation.controller;

import com.saasstarterkit.organisation.domain.Organization;
import com.saasstarterkit.organisation.domain.OrganizationStatus;
import com.saasstarterkit.organisation.domain.OrganizationTier;
import com.saasstarterkit.organisation.dto.*;
import com.saasstarterkit.organisation.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/organizations")
@CrossOrigin(origins = "*")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Organisation Service is running!");
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getOrganization(@PathVariable UUID id) {
        Organization org = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(new OrganizationResponse(org));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<OrganizationResponse> getOrganizationBySlug(@PathVariable String slug) {
        Organization org = organizationService.getOrganizationBySlug(slug);
        return ResponseEntity.ok(new OrganizationResponse(org));
    }

    @PostMapping
    public ResponseEntity<OrganizationResponse> createOrganization(@Valid @RequestBody OrganizationCreateRequest request) {
        Organization org = organizationService.createOrganization(request, UUID.randomUUID());
        return ResponseEntity.ok(new OrganizationResponse(org));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponse> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody OrganizationUpdateRequest request) {
        // TODO: Implement update logic in service
        Organization org = organizationService.getOrganizationById(id);
        // Update fields based on request
        return ResponseEntity.ok(new OrganizationResponse(org));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrganizationResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam OrganizationStatus status,
            @RequestParam(required = false) String reason) {
        Organization org = organizationService.updateOrganizationStatus(id, status, reason, UUID.randomUUID());
        return ResponseEntity.ok(new OrganizationResponse(org));
    }

    @PutMapping("/{id}/tier")
    public ResponseEntity<OrganizationResponse> updateTier(
            @PathVariable UUID id,
            @RequestParam OrganizationTier tier) {
        Organization org = organizationService.changeOrganizationTier(id, tier, UUID.randomUUID());
        return ResponseEntity.ok(new OrganizationResponse(org));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrganizationResponse>> getOrganizationsByStatus(@PathVariable OrganizationStatus status) {
        List<Organization> orgs = organizationService.getOrganizationsByStatus(status);
        List<OrganizationResponse> responses = orgs.stream()
            .map(OrganizationResponse::new)
            .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/tier/{tier}")
    public ResponseEntity<List<OrganizationResponse>> getOrganizationsByTier(@PathVariable OrganizationTier tier) {
        List<Organization> orgs = organizationService.getOrganizationsByTier(tier);
        List<OrganizationResponse> responses = orgs.stream()
            .map(OrganizationResponse::new)
            .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/attention-needed")
    public ResponseEntity<List<OrganizationResponse>> getOrganizationsNeedingAttention() {
        List<Organization> orgs = organizationService.getOrganizationsNeedingAttention();
        List<OrganizationResponse> responses = orgs.stream()
            .map(OrganizationResponse::new)
            .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<OrganizationResponse>> searchOrganizations(
            @RequestParam String query,
            Pageable pageable) {
        // TODO: Implement search in service
        return ResponseEntity.ok(Page.empty(pageable));
    }
}



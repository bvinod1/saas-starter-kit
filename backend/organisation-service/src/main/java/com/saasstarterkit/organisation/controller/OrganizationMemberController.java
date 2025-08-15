package com.saasstarterkit.organisation.controller;

import com.saasstarterkit.organisation.domain.OrganizationMember;
import com.saasstarterkit.organisation.dto.MemberAddRequest;
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
@RequestMapping("/organizations/{organizationId}/members")
@CrossOrigin(origins = "*")
public class OrganizationMemberController {

    @Autowired
    private OrganizationService organizationService;

    @GetMapping
    public ResponseEntity<List<OrganizationMember>> getOrganizationMembers(
            @PathVariable UUID organizationId,
            Pageable pageable) {
        // TODO: Implement member retrieval from service
        return ResponseEntity.ok(List.of());
    }

    @PostMapping
    public ResponseEntity<OrganizationMember> addMember(
            @PathVariable UUID organizationId,
            @Valid @RequestBody MemberAddRequest request) {
        OrganizationMember member = organizationService.addMember(organizationId, request, UUID.randomUUID());
        return ResponseEntity.ok(member);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<OrganizationMember> getMember(
            @PathVariable UUID organizationId,
            @PathVariable UUID memberId) {
        // TODO: Implement member retrieval from service
        return ResponseEntity.ok(new OrganizationMember());
    }

    @PutMapping("/{memberId}/role")
    public ResponseEntity<OrganizationMember> updateMemberRole(
            @PathVariable UUID organizationId,
            @PathVariable UUID memberId,
            @RequestParam String role) {
        // TODO: Implement role update in service
        return ResponseEntity.ok(new OrganizationMember());
    }

    @PutMapping("/{memberId}/status")
    public ResponseEntity<OrganizationMember> updateMemberStatus(
            @PathVariable UUID organizationId,
            @PathVariable UUID memberId,
            @RequestParam String status) {
        // TODO: Implement status update in service
        return ResponseEntity.ok(new OrganizationMember());
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID organizationId,
            @PathVariable UUID memberId) {
        // TODO: Implement member removal in service
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<OrganizationMember>> searchMembers(
            @PathVariable UUID organizationId,
            @RequestParam String query,
            Pageable pageable) {
        // TODO: Implement member search in service
        return ResponseEntity.ok(Page.empty(pageable));
    }
}

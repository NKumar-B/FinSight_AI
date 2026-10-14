package com.finsight.auth.controller;

import com.finsight.auth.dto.request.UpdateRolesRequest;
import com.finsight.auth.dto.response.UserResponse;
import com.finsight.auth.service.UserService;
import com.finsight.common.dto.ApiResponse;
import com.finsight.common.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing user accounts, roles, and status")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'AUDITOR')")
    @Operation(summary = "List users with pagination and search", description = "Returns a paginated list of users.")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<UserResponse> response = userService.listUsers(search, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response, "Users retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'AUDITOR')")
    @Operation(summary = "Get user details by ID", description = "Retrieves details for a specific user.")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "User retrieved successfully"));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user roles", description = "Assigns specified roles to a user account (Admin only).")
    public ResponseEntity<ApiResponse<UserResponse>> updateRoles(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRolesRequest request) {
        UserResponse response = userService.updateRoles(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "User roles updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate or deactivate user account", description = "Enables or disables a user account (Admin only).")
    public ResponseEntity<ApiResponse<UserResponse>> setStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        UserResponse response = userService.setUserActiveStatus(id, active);
        return ResponseEntity.ok(ApiResponse.ok(response, "User active status updated successfully"));
    }
}

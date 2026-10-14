package com.finsight.auth.service;

import com.finsight.auth.dto.request.UpdateRolesRequest;
import com.finsight.auth.dto.response.UserResponse;
import com.finsight.common.dto.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {
    PagedResponse<UserResponse> listUsers(String search, Pageable pageable);
    UserResponse getUserById(Long id);
    UserResponse updateRoles(Long id, UpdateRolesRequest request);
    UserResponse setUserActiveStatus(Long id, boolean active);
}

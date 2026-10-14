package com.finsight.auth.service.impl;

import com.finsight.auth.dto.request.UpdateRolesRequest;
import com.finsight.auth.dto.response.UserResponse;
import com.finsight.auth.entity.Role;
import com.finsight.auth.entity.User;
import com.finsight.auth.mapper.UserMapper;
import com.finsight.auth.repository.RoleRepository;
import com.finsight.auth.repository.UserRepository;
import com.finsight.auth.service.UserService;
import com.finsight.common.dto.PagedResponse;
import com.finsight.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> listUsers(String search, Pageable pageable) {
        Page<User> page;
        if (search != null && !search.trim().isEmpty()) {
            page = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(search.trim(), search.trim(), pageable);
        } else {
            page = userRepository.findAll(pageable);
        }

        List<UserResponse> content = page.getContent().stream()
                .map(userMapper::toResponse)
                .toList();

        return PagedResponse.<UserResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateRoles(Long id, UpdateRolesRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            String normalized = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
            Role role = roleRepository.findByName(normalized)
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name(normalized)
                            .description("Role " + normalized)
                            .build()));
            roles.add(role);
        }

        user.setRoles(roles);
        User updated = userRepository.save(user);
        log.info("[UserService] Updated roles for userId={}: {}", id, request.getRoles());
        return userMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public UserResponse setUserActiveStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setActive(active);
        User updated = userRepository.save(user);
        log.info("[UserService] Set active status for userId={} to {}", id, active);
        return userMapper.toResponse(updated);
    }
}

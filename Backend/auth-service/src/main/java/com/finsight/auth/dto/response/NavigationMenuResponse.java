package com.finsight.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationMenuResponse {

    private String username;
    private String fullName;
    private Set<String> roles;
    @Builder.Default
    private List<MenuItemDto> menuItems = new ArrayList<>();
    @Builder.Default
    private List<String> permissions = new ArrayList<>();
}

package com.finsight.auth.service;

import com.finsight.auth.dto.response.NavigationMenuResponse;

public interface RoleMenuService {

    NavigationMenuResponse getMenuForUser(String username);
}

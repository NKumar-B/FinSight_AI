package com.finsight.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDto {

    private String id;
    private String title;
    private String icon;
    private String path;
    private String badge;
    @Builder.Default
    private List<String> allowedRoles = new ArrayList<>();
    @Builder.Default
    private List<MenuItemDto> children = new ArrayList<>();
}

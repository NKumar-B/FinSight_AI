package com.finsight.auth.service.impl;

import com.finsight.auth.dto.response.MenuItemDto;
import com.finsight.auth.dto.response.NavigationMenuResponse;
import com.finsight.auth.entity.Role;
import com.finsight.auth.entity.User;
import com.finsight.auth.repository.UserRepository;
import com.finsight.auth.service.RoleMenuService;
import com.finsight.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleMenuServiceImpl implements RoleMenuService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public NavigationMenuResponse getMenuForUser(String usernameOrEmail) {
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new ResourceNotFoundException("User", "username/email", usernameOrEmail));

        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        boolean isAdmin = roleNames.contains("ROLE_ADMIN");
        boolean isAnalyst = roleNames.contains("ROLE_FINANCIAL_ANALYST");
        boolean isCompliance = roleNames.contains("ROLE_COMPLIANCE_OFFICER");
        boolean isAuditor = roleNames.contains("ROLE_AUDITOR");

        List<MenuItemDto> menuItems = new ArrayList<>();
        List<String> permissions = new ArrayList<>();

        // 1. Dashboard / Overview (Universal)
        menuItems.add(MenuItemDto.builder()
                .id("dashboard")
                .title("Executive Dashboard")
                .icon("chart-line")
                .path("/dashboard")
                .allowedRoles(List.of("ROLE_USER", "ROLE_FINANCIAL_ANALYST", "ROLE_COMPLIANCE_OFFICER", "ROLE_AUDITOR", "ROLE_ADMIN"))
                .build());
        permissions.add("DASHBOARD_VIEW");

        // 2. Financial Operations (Analyst & Admin)
        if (isAdmin || isAnalyst) {
            menuItems.add(MenuItemDto.builder()
                    .id("financial-operations")
                    .title("Financial Operations")
                    .icon("bank")
                    .path("/finance")
                    .allowedRoles(List.of("ROLE_FINANCIAL_ANALYST", "ROLE_ADMIN"))
                    .children(List.of(
                            MenuItemDto.builder().id("accounts").title("Chart of Accounts").icon("credit-card").path("/finance/accounts").build(),
                            MenuItemDto.builder().id("ingestion").title("CSV / Ingestion Hub").icon("upload").path("/finance/ingestion").build(),
                            MenuItemDto.builder().id("reconciliation").title("Deterministic Matching").icon("balance-scale").path("/finance/reconciliation").build(),
                            MenuItemDto.builder().id("invoices").title("Invoices & AR Aging").icon("file-invoice-dollar").path("/finance/invoices").build(),
                            MenuItemDto.builder().id("cash-flow").title("Cash Flow Runway Forecast").icon("wave-square").path("/finance/cash-flow").build()
                    ))
                    .build());

            permissions.addAll(List.of("ACCOUNTS_MANAGE", "TRANSACTIONS_INGEST", "RECONCILIATION_EXECUTE", "INVOICES_MANAGE", "CASHFLOW_FORECAST_VIEW"));
        }

        // 3. Risk & Compliance (Compliance Officer, Auditor & Admin)
        if (isAdmin || isCompliance || isAuditor) {
            List<MenuItemDto> complianceChildren = new ArrayList<>();
            complianceChildren.add(MenuItemDto.builder().id("fraud-alerts").title("AML & Fraud Alerts").icon("shield-exclamation").path("/compliance/fraud").build());
            complianceChildren.add(MenuItemDto.builder().id("audit-trail").title("Immutable Audit Trail").icon("history").path("/compliance/audit-trail").build());
            complianceChildren.add(MenuItemDto.builder().id("sox-reports").title("SOX Compliance Reports").icon("file-contract").path("/compliance/reports").build());

            if (isAdmin || isCompliance) {
                complianceChildren.add(MenuItemDto.builder().id("human-gate").title("AI Human Review Gates").icon("user-shield").path("/compliance/ai-review").badge("GATED").build());
                permissions.addAll(List.of("FRAUD_SURVEILLANCE", "HUMAN_GATE_APPROVE", "AUDIT_VIEW", "COMPLIANCE_REPORTS_GENERATE"));
            } else {
                permissions.addAll(List.of("AUDIT_VIEW", "COMPLIANCE_REPORTS_VIEW"));
            }

            menuItems.add(MenuItemDto.builder()
                    .id("compliance-center")
                    .title("Risk & Compliance")
                    .icon("shield-alt")
                    .path("/compliance")
                    .allowedRoles(List.of("ROLE_COMPLIANCE_OFFICER", "ROLE_AUDITOR", "ROLE_ADMIN"))
                    .children(complianceChildren)
                    .build());
        }

        // 4. Autonomous AI Intelligence (Analyst, Compliance, Admin)
        if (isAdmin || isAnalyst || isCompliance) {
            menuItems.add(MenuItemDto.builder()
                    .id("ai-intelligence")
                    .title("AI Intelligence Hub")
                    .icon("robot")
                    .path("/ai")
                    .allowedRoles(List.of("ROLE_FINANCIAL_ANALYST", "ROLE_COMPLIANCE_OFFICER", "ROLE_ADMIN"))
                    .children(List.of(
                            MenuItemDto.builder().id("ai-agents").title("The 9 Specialist Agents").icon("brain").path("/ai/agents").build(),
                            MenuItemDto.builder().id("ai-tools").title("Microservice Tool Registry").icon("tools").path("/ai/tools").build(),
                            MenuItemDto.builder().id("ai-execute").title("Interactive AI Prompt Studio").icon("terminal").path("/ai/execute").build()
                    ))
                    .build());
            permissions.add("AI_ORCHESTRATION_EXECUTE");
        }

        // 5. System Administration (Admin Only)
        if (isAdmin) {
            menuItems.add(MenuItemDto.builder()
                    .id("admin-settings")
                    .title("Administration")
                    .icon("cog")
                    .path("/admin")
                    .allowedRoles(List.of("ROLE_ADMIN"))
                    .children(List.of(
                            MenuItemDto.builder().id("user-management").title("User Management & RBAC").icon("users-cog").path("/admin/users").build(),
                            MenuItemDto.builder().id("service-registry").title("Eureka Service Registry").icon("server").path("/admin/eureka").build(),
                            MenuItemDto.builder().id("gateway-routing").title("Gateway Security Policies").icon("network-wired").path("/admin/gateway").build()
                    ))
                    .build());
            permissions.addAll(List.of("USER_ADMIN", "ROLES_MANAGE", "SYSTEM_CONFIG", "GATEWAY_ADMIN"));
        }

        return NavigationMenuResponse.builder()
                .username(user.getUsername())
                .fullName(user.getFullName())
                .roles(roleNames)
                .menuItems(menuItems)
                .permissions(permissions)
                .build();
    }
}

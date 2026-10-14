import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AppShell } from '@/components/layout/AppShell';
import { ProtectedRoute } from '@/features/auth/components/ProtectedRoute';
import { LoginPage } from '@/features/auth/pages/LoginPage';
import { UnauthorizedPage, NotFoundPage } from '@/features/auth/pages/UnauthorizedPage';

import { DashboardPage } from '@/features/dashboard/pages/DashboardPage';
import { FinancialDataPage } from '@/features/financial-data/pages/FinancialDataPage';
import { ReconciliationPage } from '@/features/reconciliation/pages/ReconciliationPage';
import { ExpensesPage } from '@/features/expenses/pages/ExpensesPage';
import { InvoicesReceivablesPage } from '@/features/invoices-receivables/pages/InvoicesReceivablesPage';
import { CashFlowPage } from '@/features/cash-flow/pages/CashFlowPage';
import { FraudMonitoringPage } from '@/features/fraud-monitoring/pages/FraudMonitoringPage';
import { InvestigationsPage } from '@/features/investigations/pages/InvestigationsPage';
import { AgentActivityPage } from '@/features/agent-activity/pages/AgentActivityPage';
import { AiCopilotPage } from '@/features/ai-copilot/pages/AiCopilotPage';
import { ReportsPage } from '@/features/reports/pages/ReportsPage';
import { AuditHistoryPage } from '@/features/audit-history/pages/AuditHistoryPage';
import { UserManagementPage } from '@/features/user-management/pages/UserManagementPage';

export const AppRoutes: React.FC = () => {
  return (
    <Routes>
      {/* Public Route */}
      <Route path="/login" element={<LoginPage />} />

      {/* Protected Routes wrapped in AppShell */}
      <Route element={<ProtectedRoute />}>
        <Route element={<AppShell />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/financial-data" element={<FinancialDataPage />} />
          <Route path="/reconciliation" element={<ReconciliationPage />} />
          <Route path="/expenses" element={<ExpensesPage />} />
          <Route path="/invoices" element={<InvoicesReceivablesPage />} />
          <Route path="/cash-flow" element={<CashFlowPage />} />
          <Route path="/fraud" element={<FraudMonitoringPage />} />
          <Route path="/investigations" element={<InvestigationsPage />} />
          <Route path="/agents" element={<AgentActivityPage />} />
          <Route path="/copilot" element={<AiCopilotPage />} />
          <Route path="/reports" element={<ReportsPage />} />
          <Route path="/audit" element={<AuditHistoryPage />} />

          {/* Admin Protected Route */}
          <Route element={<ProtectedRoute requiredRole="ROLE_ADMIN" />}>
            <Route path="/users" element={<UserManagementPage />} />
          </Route>
        </Route>
      </Route>

      {/* Error Routes */}
      <Route path="/unauthorized" element={<UnauthorizedPage />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};

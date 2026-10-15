import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { ProtectedRoute } from './ProtectedRoute';
import { AuthProvider } from '../hooks/useAuth';

describe('ProtectedRoute Navigation & Guard Logic', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  const renderWithRouter = (initialRoute: string) =>
    render(
      <MemoryRouter initialEntries={[initialRoute]}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<div>LOGIN_PAGE_CONTENT</div>} />
            <Route path="/unauthorized" element={<div>UNAUTHORIZED_PAGE_CONTENT</div>} />
            <Route element={<ProtectedRoute />}>
              <Route path="/dashboard" element={<div>DASHBOARD_PAGE_CONTENT</div>} />
            </Route>
            <Route element={<ProtectedRoute requiredRole="ROLE_ADMIN" />}>
              <Route path="/admin-only" element={<div>ADMIN_ONLY_CONTENT</div>} />
            </Route>
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );

  it('redirects unauthenticated users to /login', () => {
    renderWithRouter('/dashboard');
    expect(screen.getByText('LOGIN_PAGE_CONTENT')).toBeInTheDocument();
    expect(screen.queryByText('DASHBOARD_PAGE_CONTENT')).not.toBeInTheDocument();
  });

  it('allows access to /dashboard when valid session exists in localStorage (no redirect loop)', () => {
    localStorage.setItem('finsight_jwt', 'mock_jwt_token_123');
    localStorage.setItem(
      'finsight_user',
      JSON.stringify({
        id: 1,
        username: 'admin',
        roles: ['ROLE_ADMIN'],
        email: 'admin@finsight.local',
        fullName: 'Platform Administrator',
        enabled: true,
      })
    );

    renderWithRouter('/dashboard');
    expect(screen.getByText('DASHBOARD_PAGE_CONTENT')).toBeInTheDocument();
    expect(screen.queryByText('LOGIN_PAGE_CONTENT')).not.toBeInTheDocument();
  });

  it('allows access to role-protected routes for authorized roles', () => {
    localStorage.setItem('finsight_jwt', 'mock_jwt_token_123');
    localStorage.setItem(
      'finsight_user',
      JSON.stringify({
        id: 1,
        username: 'admin',
        roles: ['ROLE_ADMIN'],
        email: 'admin@finsight.local',
        fullName: 'Platform Administrator',
        enabled: true,
      })
    );

    renderWithRouter('/admin-only');
    expect(screen.getByText('ADMIN_ONLY_CONTENT')).toBeInTheDocument();
  });

  it('redirects to /unauthorized when user lacks required role', () => {
    localStorage.setItem('finsight_jwt', 'mock_jwt_token_123');
    localStorage.setItem(
      'finsight_user',
      JSON.stringify({
        id: 2,
        username: 'analyst_john',
        roles: ['ROLE_FINANCIAL_ANALYST'],
        email: 'analyst@finsight.local',
        fullName: 'John Doe',
        enabled: true,
      })
    );

    renderWithRouter('/admin-only');
    expect(screen.getByText('UNAUTHORIZED_PAGE_CONTENT')).toBeInTheDocument();
    expect(screen.queryByText('ADMIN_ONLY_CONTENT')).not.toBeInTheDocument();
  });
});

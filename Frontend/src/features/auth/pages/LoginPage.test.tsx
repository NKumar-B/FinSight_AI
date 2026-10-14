import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { LoginPage } from './LoginPage';
import { AuthProvider } from '../hooks/useAuth';
import { ThemeProvider } from '@/lib/theme';
import { I18nProvider } from '@/lib/i18n';

describe('LoginPage Component', () => {
  const renderLoginPage = () =>
    render(
      <BrowserRouter>
        <ThemeProvider>
          <I18nProvider>
            <AuthProvider>
              <LoginPage />
            </AuthProvider>
          </I18nProvider>
        </ThemeProvider>
      </BrowserRouter>
    );

  it('renders enterprise login form and title', () => {
    renderLoginPage();

    expect(screen.getByText('Sign in to your workspace')).toBeInTheDocument();
    expect(screen.getByLabelText(/enterprise username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in to finsight ai/i })).toBeInTheDocument();
  });

  it('renders register switch and one-click evaluator personas', () => {
    renderLoginPage();

    expect(screen.getByText(/don't have an account\? register with us/i)).toBeInTheDocument();
    expect(screen.getByText('Admin')).toBeInTheDocument();
    expect(screen.getByText('Analyst')).toBeInTheDocument();
  });
});

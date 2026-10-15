import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export interface ProtectedRouteProps {
  requiredRole?: string;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ requiredRole }) => {
  const { isAuthenticated, hasRole } = useAuth();

  // Evaluate both in-memory React state and synchronous localStorage fallback to eliminate navigation race conditions
  const hasValidSession = Boolean(
    isAuthenticated ||
    (localStorage.getItem('finsight_jwt') && localStorage.getItem('finsight_user'))
  );

  if (!hasValidSession) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && !hasRole(requiredRole)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
};

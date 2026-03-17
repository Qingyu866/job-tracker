import React, { useEffect, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useUserStore } from '@/store/userStore';
import { Spinner } from '@/components/common';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { isLoggedIn } = useUserStore();
  const location = useLocation();

  if (!isLoggedIn) {
    sessionStorage.setItem('redirect_after_login', location.pathname);
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}

interface PublicRouteProps {
  children: React.ReactNode;
}

export function PublicRoute({ children }: PublicRouteProps) {
  const { isLoggedIn } = useUserStore();

  if (isLoggedIn) {
    const redirectPath = sessionStorage.getItem('redirect_after_login');
    sessionStorage.removeItem('redirect_after_login');
    return <Navigate to={redirectPath || '/'} replace />;
  }

  return <>{children}</>;
}

export function AuthLoadingWrapper({ children }: { children: React.ReactNode }) {
  const { initAuth } = useUserStore();
  const [initialized, setInitialized] = useState(false);

  useEffect(() => {
    const init = async () => {
      await initAuth();
      setInitialized(true);
    };
    init();
  }, [initAuth]);

  if (!initialized) {
    return (
      <div className="min-h-screen bg-paper-50 flex items-center justify-center">
        <Spinner size="xl" />
      </div>
    );
  }

  return <>{children}</>;
}

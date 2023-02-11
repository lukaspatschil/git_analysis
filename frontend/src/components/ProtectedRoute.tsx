import { ReactNode, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../stores/useAuthStore';

export const LOGIN_PROVIDER_KEY = 'loginProvider';

export default function ProtectedRoute({children}: {children: ReactNode}) {
  const { token } = useAuthStore();
  const navigate = useNavigate();

  useEffect(() => {
    if (!token) {
      const loginProvider = localStorage.getItem(LOGIN_PROVIDER_KEY);
      if (loginProvider) {
        window.location.href = `${import.meta.env.VITE_BASE_API_URL}oauth2/authorization/${loginProvider}`;
      } else {
        navigate('/');
      }
    }
  }, [token]);

  return <>{children}</>;
}
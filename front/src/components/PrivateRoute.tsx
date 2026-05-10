import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import type { ReactNode } from 'react';

interface Props {
  children: ReactNode;
  requiredBusinessId?: number;
}

export default function PrivateRoute({ children, requiredBusinessId }: Props) {
  const { user } = useAuth();

  if (!user) return <Navigate to="/login" replace />;

  const isAdmin = user.roles.includes('ADMIN') || user.roles.includes('BUSINESS_OWNER');
  if (!isAdmin) return <Navigate to="/login" replace />;

  if (requiredBusinessId !== undefined && user.businessId !== requiredBusinessId) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}

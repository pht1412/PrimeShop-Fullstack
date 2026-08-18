import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import api from '../api/api';

interface AdminRouteProps {
  children: React.ReactNode;
}

const AdminRoute: React.FC<AdminRouteProps> = ({ children }) => {
  const [isAdmin, setIsAdmin] = useState<boolean | null>(null);
  const [loading, setLoading] = useState(true);
  const [role, setRole] = useState<string | null>(null);

  useEffect(() => {
    const checkAdminRole = async () => {
      try {
        const response = await api.get('/auth/check-role');   
        const roles = response.data;
        setRole(roles);
        const hasAdmin = roles.some((role: string) => role === "ROLE_ADMIN");
        setIsAdmin(hasAdmin);
      } catch (error) {
        console.error("Error checking admin role:", error);
        setIsAdmin(false);
      } finally {
        setLoading(false);
      }
    };

    checkAdminRole();
  }, [isAdmin]);

  if (loading) {
    return <div>Loading...</div>;
  }

  if (!isAdmin) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};

export default AdminRoute; 
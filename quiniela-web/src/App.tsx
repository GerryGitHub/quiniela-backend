import { useEffect } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import { useStore } from './store/useStore';

export default function App() {
  const navigate = useNavigate();
  const { isAuthenticated, fetchPerfil, logout } = useStore();

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token && !isAuthenticated) {
      fetchPerfil()
        .then(() => {
          // Token valid, user authenticated
        })
        .catch((err) => {
          console.log('Error fetching perfil:', err);
          // Don't logout immediately, let user try to use the app
        });
    }
  }, []);

  return <Outlet />;
}
import { useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import { useStore } from './store/useStore';

export default function App() {
  const { fetchPerfil, usuario } = useStore();

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token && !usuario) {
      fetchPerfil();
    }
  }, []);

  return (
    <Outlet />
  );
}
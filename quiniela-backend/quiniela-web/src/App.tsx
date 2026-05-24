import { useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import { useStore } from './store/useStore';
import { ApiToggle } from './services/apiToggle';

export default function App() {
  const { fetchPerfil, logout, usuario } = useStore();

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token && !usuario) {
      console.log('Token found, fetching perfil...');
      fetchPerfil()
        .then(() => console.log('Perfil fetched successfully'))
        .catch((err) => {
          console.error('Error fetching perfil:', err);
        });
    }
  }, []);

  return (
    <>
      <ApiToggle />
      <Outlet />
    </>
  );
}
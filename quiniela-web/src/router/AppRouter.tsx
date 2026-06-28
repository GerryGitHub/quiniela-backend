import { createBrowserRouter, Navigate } from 'react-router-dom';
import App from '../App';
import Login from '../pages/Login';
import Dashboard from '../pages/Dashboard';
import Grupos from '../pages/Grupos';
import Resultados from '../pages/Resultados';
import Users from '../pages/Users';
import UserDetail from '../pages/UserDetail';
import AdminQuinielas from '../pages/AdminQuinielas';
import EstadisticasEquipos from '../pages/EstadisticasEquipos';
import Eliminatorias from '../pages/Eliminatorias';

const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      {
        index: true,
        element: <Navigate to="/login" replace />,
      },
      {
        path: 'login',
        element: <Login />,
      },
      {
        path: 'dashboard',
        element: <Dashboard />,
      },
      {
        path: 'grupos',
        element: <Grupos />,
      },
      {
        path: 'resultados',
        element: <Resultados />,
      },
      {
        path: 'admin/users',
        element: <Users />,
      },
      {
        path: 'admin/users/:id',
        element: <UserDetail />,
      },
      {
        path: 'admin/quinielas',
        element: <AdminQuinielas />,
      },
      {
        path: 'admin/estadisticas',
        element: <EstadisticasEquipos />,
      },
      {
        path: 'eliminatorias',
        element: <Eliminatorias />,
      },
    ],
  },
  {
    path: '*',
    element: <Navigate to="/login" replace />,
  },
]);

export default router;
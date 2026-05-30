import { createBrowserRouter, Navigate } from 'react-router-dom';
import App from '../App';
import Login from '../pages/Login';
import Register from '../pages/Register';
import Dashboard from '../pages/Dashboard';
import QuinielaDetalle from '../pages/QuinielaDetalle';
import Pronosticos from '../pages/Pronosticos';
import Grupos from '../pages/Grupos';
import Resultados from '../pages/Resultados';
import Users from '../pages/Users';
import AdminQuinielas from '../pages/AdminQuinielas';

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
        path: 'register',
        element: <Register />,
      },
      {
        path: 'dashboard',
        element: <Dashboard />,
      },
      {
        path: 'quiniela/:id',
        element: <QuinielaDetalle />,
      },
      {
        path: 'pronosticos/:quinielaId',
        element: <Pronosticos />,
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
        path: 'admin/quinielas',
        element: <AdminQuinielas />,
      },
    ],
  },
  {
    path: '*',
    element: <Navigate to="/login" replace />,
  },
]);

export default router;
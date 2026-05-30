import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useStore } from '../store/useStore';
import api from '../services/api';
import Spinner from '../components/Spinner';
import './Users.css';

export default function UserDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { usuario, logout } = useStore();
  const [user, setUser] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (usuario && usuario.rol !== 'ADMIN') {
      navigate('/dashboard');
      return;
    }
    if (id) {
      api.get(`/admin/users/${id}`)
        .then(res => setUser(res.data))
        .catch(() => navigate('/admin/users'))
        .finally(() => setLoading(false));
    }
  }, [id]);

  if (loading) return (
    <div className="users-page">
      <header className="header">
        <h1>Detalle de Usuario</h1>
        <div className="header-actions">
          <Link to="/dashboard" className="btn-grupos">Dashboard</Link>
          <Link to="/admin/users" className="btn-grupos">Usuarios</Link>
          <div className="header-right">
            <span className="user-info">{usuario?.nombre}</span>
            <button onClick={logout} className="btn-logout">Cerrar Sesión</button>
          </div>
        </div>
      </header>
      <main className="main-content"><div className="loading-container"><Spinner /></div></main>
    </div>
  );

  if (!user) return null;

  return (
    <div className="users-page">
      <header className="header">
        <h1>Detalle de Usuario</h1>
        <div className="header-actions">
          <Link to="/dashboard" className="btn-grupos">Dashboard</Link>
          <Link to="/admin/users" className="btn-grupos">Usuarios</Link>
          <div className="header-right">
            <span className="user-info">{usuario?.nombre}</span>
            <button onClick={logout} className="btn-logout">Cerrar Sesión</button>
          </div>
        </div>
      </header>

      <main className="main-content">
        <div className="detail-card">
          <div className="detail-header">
            <h2>{user.nombre}</h2>
            <span className={user.verificado ? 'badge-verified' : 'badge-unverified'}>
              {user.verificado ? 'Verificado' : 'No verificado'}
            </span>
          </div>
          <div className="detail-grid">
            <div className="detail-field">
              <span className="detail-label">Correo</span>
              <span className="detail-value">{user.email}</span>
            </div>
            <div className="detail-field">
              <span className="detail-label">Fecha de registro</span>
              <span className="detail-value">
                {user.fechaRegistro ? new Date(user.fechaRegistro).toLocaleDateString('es-MX', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' }) : '-'}
              </span>
            </div>
            <div className="detail-field">
              <span className="detail-label">Quinielas</span>
              <span className="detail-value">{user.cantidadQuinielas}</span>
            </div>
          </div>

          <h3 className="detail-subtitle">Quinielas</h3>
          {user.quinielas.length === 0 ? (
            <div className="empty-state"><p>Este usuario no pertenece a ninguna quiniela</p></div>
          ) : (
            <div className="detail-quinielas">
              {user.quinielas.map((q: any) => (
                <div key={q.id} className="detail-quiniela-row">
                  <span className="detail-quiniela-nombre">{q.nombre}</span>
                  <span className="detail-quiniela-admin">Creada por {q.administrador}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

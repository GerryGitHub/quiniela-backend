import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useStore } from '../store/useStore';
import api from '../services/api';
import Spinner from '../components/Spinner';
import './Users.css';

export default function Users() {
  const navigate = useNavigate();
  const { usuario, logout } = useStore();
  const [users, setUsers] = useState<any[]>([]);
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState<string>('todos');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (usuario && usuario.rol !== 'ADMIN') {
      navigate('/dashboard');
      return;
    }
    fetchUsers();
  }, [search, filter]);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const params: any = {};
      if (search.trim()) params.search = search.trim();
      if (filter === 'verificados') params.verificado = true;
      else if (filter === 'noverificados') params.verificado = false;

      const res = await api.get('/admin/users', { params });
      setUsers(res.data);
    } catch (err) {
      console.log('Error cargando usuarios:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="users-page">
      <header className="header">
        <h1>Gestión de Usuarios</h1>
        <div className="header-actions">
          <Link to="/dashboard" className="btn-grupos">Dashboard</Link>
          <div className="header-right">
            <span className="user-info">{usuario?.nombre}</span>
            <button onClick={logout} className="btn-logout">Cerrar Sesión</button>
          </div>
        </div>
      </header>

      <main className="main-content">
        <div className="users-toolbar">
          <input
            type="text"
            placeholder="Buscar por nombre o correo..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="search-input"
          />
          <select value={filter} onChange={(e) => setFilter(e.target.value)} className="filter-select">
            <option value="todos">Todos</option>
            <option value="verificados">Verificados</option>
            <option value="noverificados">No verificados</option>
          </select>
        </div>

        {loading ? (
          <div className="loading-container"><Spinner /></div>
        ) : users.length === 0 ? (
          <div className="empty-state">
            <p>No se encontraron usuarios</p>
          </div>
        ) : (
          <div className="table-wrapper">
            <table className="users-table">
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Correo</th>
                  <th>Verificado</th>
                  <th>Fecha Registro</th>
                  <th>Quinielas</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u: any) => (
                  <tr key={u.id}>
                    <td><Link to={`/admin/users/${u.id}`} className="user-link">{u.nombre}</Link></td>
                    <td>{u.email}</td>
                    <td>{u.verificado ? <span className="badge-verified">Sí</span> : <span className="badge-unverified">No</span>}</td>
                    <td>{u.fechaRegistro ? new Date(u.fechaRegistro).toLocaleDateString('es-MX', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }) : '-'}</td>
                    <td>{u.quinielas}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>
    </div>
  );
}

import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useStore } from '../store/useStore';
import api from '../services/api';
import Spinner from '../components/Spinner';
import './Users.css';

export default function AdminQuinielas() {
  const navigate = useNavigate();
  const { usuario, logout } = useStore();
  const [quinielas, setQuinielas] = useState<any[]>([]);
  const [search, setSearch] = useState('');
  const [sort, setSort] = useState('');
  const [order, setOrder] = useState('desc');
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState('');

  useEffect(() => {
    if (usuario && usuario.rol !== 'ADMIN') {
      navigate('/dashboard');
      return;
    }
    fetchQuinielas();
  }, [search, sort, order]);

  const fetchQuinielas = async () => {
    setLoading(true);
    setFetchError('');
    try {
      const params: any = {};
      if (search.trim()) params.search = search.trim();
      if (sort) params.sort = sort;
      if (order) params.order = order;

      const res = await api.get('/admin/quinielas', { params });
      setQuinielas(res.data);
    } catch (err: any) {
      const msg = err?.response?.status === 401 || err?.response?.status === 403
        ? 'Tu sesión expiró. Inicia sesión nuevamente.'
        : 'Error al cargar quinielas. Intenta más tarde.';
      setFetchError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleSort = (column: string) => {
    if (sort === column) {
      setOrder(order === 'asc' ? 'desc' : 'asc');
    } else {
      setSort(column);
      setOrder('asc');
    }
  };

  const sortArrow = (column: string) => {
    if (sort !== column) return '';
    return order === 'asc' ? ' ▲' : ' ▼';
  };

  return (
    <div className="users-page">
      <header className="header">
        <Link to="/dashboard" className="btn-back">← Volver</Link>
        <h1>Gestión de Quinielas</h1>
        <div className="header-right" style={{ marginLeft: 'auto' }}>
          <span className="user-info">{usuario?.nombre}</span>
          <button onClick={logout} className="btn-logout">Cerrar Sesión</button>
        </div>
      </header>

      <main className="main-content">
        <div className="users-toolbar">
          <input
            type="text"
            placeholder="Buscar por nombre o creador..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="search-input"
          />
        </div>

        {loading ? (
          <div className="loading-container"><Spinner /></div>
        ) : fetchError ? (
          <div className="error-section">{fetchError}</div>
        ) : quinielas.length === 0 ? (
          <div className="empty-state">
            <p>No se encontraron quinielas</p>
          </div>
        ) : (
          <div className="table-wrapper">
            <table className="users-table">
              <thead>
                <tr>
                  <th onClick={() => handleSort('nombre')} className="sortable">Nombre{sortArrow('nombre')}</th>
                  <th onClick={() => handleSort('creador')} className="sortable">Creador{sortArrow('creador')}</th>
                  <th>Participantes</th>
                  <th>Estado</th>
                  <th onClick={() => handleSort('fecha')} className="sortable">Fecha{sortArrow('fecha')}</th>
                </tr>
              </thead>
              <tbody>
                {quinielas.map((q: any) => (
                  <tr key={q.id}>
                    <td>{q.nombre}</td>
                    <td>{q.creador}</td>
                    <td>{q.participantes}</td>
                    <td><span className={`estado-label estado-${(q.estado || 'activa').toLowerCase()}`}>{q.estado || 'ACTIVA'}</span></td>
                    <td>{q.createdAt ? new Date(q.createdAt).toLocaleDateString('es-MX', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }) : '-'}</td>
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

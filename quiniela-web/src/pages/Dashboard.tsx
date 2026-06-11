import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useStore } from '../store/useStore';
import { disconnectWebSocket } from '../services/websocket';
import api from '../services/api';
import Spinner from '../components/Spinner';
import './Dashboard.css';

export default function Dashboard() {
  const navigate = useNavigate();
  const { usuario, fetchPerfil, logout } = useStore();
  const [metrics, setMetrics] = useState<any>(null);
  const [metricsLoading, setMetricsLoading] = useState(true);
  const [metricsError, setMetricsError] = useState('');
  const [activity, setActivity] = useState<any>(null);
  const [activityLoading, setActivityLoading] = useState(true);
  const [activityError, setActivityError] = useState('');
  const [system, setSystem] = useState<any>(null);
  const [systemLoading, setSystemLoading] = useState(true);
  const [systemError, setSystemError] = useState('');

  useEffect(() => {
    fetchPerfil();
  }, []);

  useEffect(() => {
    if (usuario) {
      if (usuario.rol !== 'ADMIN') {
        navigate('/login', { replace: true });
        return;
      }
      setMetricsLoading(true);
      api.get('/admin/dashboard')
        .then(res => { setMetrics(res.data); setMetricsError(''); })
        .catch(() => setMetricsError('Error al cargar métricas'))
        .finally(() => setMetricsLoading(false));

      setActivityLoading(true);
      api.get('/admin/activity')
        .then(res => { setActivity(res.data); setActivityError(''); })
        .catch(() => setActivityError('Error al cargar actividad'))
        .finally(() => setActivityLoading(false));

      setSystemLoading(true);
      api.get('/admin/system')
        .then(res => { setSystem(res.data); setSystemError(''); })
        .catch(() => setSystemError('Error al cargar estado del sistema'))
        .finally(() => setSystemLoading(false));
    }
  }, [usuario]);

  const logoutAndRedirect = () => {
    logout();
    navigate('/login', { replace: true });
  };

  if (!usuario) return <div className="loading-container"><Spinner /></div>;

  return (
    <div className="dashboard">
      <header className="header">
        <h1>Panel de Administración</h1>
        <div className="header-actions">
          <Link to="/grupos" className="btn-grupos">Grupos FIFA</Link>
          <Link to="/resultados" className="btn-grupos">Resultados</Link>
          <Link to="/admin/users" className="btn-grupos">Usuarios</Link>
          <Link to="/admin/quinielas" className="btn-grupos">Quinielas</Link>
          <div className="header-right">
            <span className="user-info">{usuario?.nombre}</span>
            <button onClick={logoutAndRedirect} className="btn-logout">Cerrar Sesión</button>
          </div>
        </div>
      </header>

      <main className="main-content">
        <div className="admin-dashboard">
          <h2>Bienvenido, Administrador</h2>
          <p className="admin-desc">Monitorea la plataforma QGol.</p>
          
          <div className="metrics-grid">
            {metricsLoading ? (
              <div className="loading-section"><Spinner /></div>
            ) : metricsError ? (
              <div className="error-section">{metricsError}</div>
            ) : (
              <>
                <div className="metric-card">
                  <span className="metric-icon">👤</span>
                  <span className="metric-value">{metrics.usuarios}</span>
                  <span className="metric-label">Usuarios registrados</span>
                </div>
                <div className="metric-card">
                  <span className="metric-icon">✅</span>
                  <span className="metric-value">{metrics.usuariosVerificados}</span>
                  <span className="metric-label">Usuarios verificados</span>
                </div>
                <div className="metric-card">
                  <span className="metric-icon">🏆</span>
                  <span className="metric-value">{metrics.quinielas}</span>
                  <span className="metric-label">Quinielas creadas</span>
                </div>
                <div className="metric-card">
                  <span className="metric-icon">⚽</span>
                  <span className="metric-value">{metrics.pronosticos}</span>
                  <span className="metric-label">Pronósticos enviados</span>
                </div>
                <div className="metric-card">
                  <span className="metric-icon">🔴</span>
                  <span className="metric-value">{metrics.partidosLive}</span>
                  <span className="metric-label">Partidos en vivo</span>
                </div>
              </>
            )}
          </div>

          <div className="activity-section">
            <h3>Actividad Reciente</h3>
            {activityLoading ? (
              <div className="loading-section"><Spinner /></div>
            ) : activityError ? (
              <div className="error-section">{activityError}</div>
            ) : (
              <div className="activity-grid">
                <div className="activity-column">
                  <h4>Últimos usuarios</h4>
                  {activity.usuarios.length > 0 ? (
                    <ul>{activity.usuarios.map((u: any) => (
                      <li key={u.id}>{u.nombre}</li>
                    ))}</ul>
                  ) : <p className="activity-empty">Sin usuarios</p>}
                </div>
                <div className="activity-column">
                  <h4>Últimas quinielas</h4>
                  {activity.quinielas.length > 0 ? (
                    <ul>{activity.quinielas.map((q: any) => (
                      <li key={q.id}>{q.nombre} <span className="activity-admin">({q.administrador})</span></li>
                    ))}</ul>
                  ) : <p className="activity-empty">Sin quinielas</p>}
                </div>
                <div className="activity-column">
                  <h4>Últimos partidos</h4>
                  {activity.partidos.length > 0 ? (
                    <ul>{activity.partidos.map((p: any) => (
                      <li key={p.id}>{p.local} vs {p.visitante} <span className="activity-estado">{p.estado}</span></li>
                    ))}</ul>
                  ) : <p className="activity-empty">Sin partidos</p>}
                </div>
              </div>
            )}
          </div>

          <div className="system-section">
            <h3>Estado del Sistema</h3>
            {systemLoading ? (
              <div className="loading-section"><Spinner /></div>
            ) : systemError ? (
              <div className="error-section">{systemError}</div>
            ) : (
              <div className="system-grid">
                <div className="system-item">
                  <span className={`system-dot ${system.api === 'ONLINE' ? 'dot-online' : 'dot-offline'}`}></span>
                  <span className="system-label">API</span>
                  <span className={`system-status ${system.api === 'ONLINE' ? 'text-online' : 'text-offline'}`}>{system.api}</span>
                </div>
                <div className="system-item">
                  <span className={`system-dot ${system.database === 'ONLINE' ? 'dot-online' : 'dot-offline'}`}></span>
                  <span className="system-label">Base de datos</span>
                  <span className={`system-status ${system.database === 'ONLINE' ? 'text-online' : 'text-offline'}`}>{system.database}</span>
                </div>
                <div className="system-item">
                  <span className="system-label" style={{ gridColumn: 'span 2' }}>Última actualización</span>
                  <span className="system-value">
                    {system.ultimaActualizacion
                      ? new Date(system.ultimaActualizacion).toLocaleString('es-MX')
                      : '—'}
                  </span>
                </div>
              </div>
            )}
          </div>

          <div className="admin-cards">
            <Link to="/grupos" className="admin-card">
              <div className="admin-icon">🌐</div>
              <h3>Grupos FIFA</h3>
              <p>Ver tablas de posiciones y resultados de grupos</p>
            </Link>
            
            <Link to="/resultados" className="admin-card">
              <div className="admin-icon">⚽</div>
              <h3>Resultados</h3>
              <p>Actualizar marcadores de partidos</p>
            </Link>
            <Link to="/admin/users" className="admin-card">
              <div className="admin-icon">👥</div>
              <h3>Usuarios</h3>
              <p>Gestionar usuarios registrados</p>
            </Link>
            <Link to="/admin/quinielas" className="admin-card">
              <div className="admin-icon">📋</div>
              <h3>Quinielas</h3>
              <p>Visualizar quinielas existentes</p>
            </Link>
          </div>
        </div>
      </main>
    </div>
  );
}
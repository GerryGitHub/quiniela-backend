import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useStore } from '../store/useStore';
import { connectWebSocket, disconnectWebSocket } from '../services/websocket';
import api from '../services/api';
import Spinner from '../components/Spinner';
import './Dashboard.css';

export default function Dashboard() {
  const navigate = useNavigate();
  const { usuario, quinielas, fetchQuinielas, fetchPerfil, logout, loading, crearQuiniela, unirseQuiniela } = useStore();
  const [showCrear, setShowCrear] = useState(false);
  const [showUnirse, setShowUnirse] = useState(false);
  const [nombre, setNombre] = useState('');
  const [codigo, setCodigo] = useState('');
  const [error, setError] = useState('');
  const [partidosEnVivo, setPartidosEnVivo] = useState<any[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [metrics, setMetrics] = useState<any>(null);
  const [metricsLoading, setMetricsLoading] = useState(false);
  const [metricsError, setMetricsError] = useState('');
  const [activity, setActivity] = useState<any>(null);
  const [activityLoading, setActivityLoading] = useState(false);
  const [activityError, setActivityError] = useState('');
  const [system, setSystem] = useState<any>(null);
  const [systemLoading, setSystemLoading] = useState(false);
  const [systemError, setSystemError] = useState('');

  useEffect(() => {
    fetchPerfil();
    fetchQuinielas();
    
    api.get('/api/resultados/en-vivo')
      .then(res => {
        if (res.data && Array.isArray(res.data)) {
          setPartidosEnVivo(res.data);
        }
      })
      .catch(() => {});

    connectWebSocket((partido) => {
      setPartidosEnVivo(prev => {
        const index = prev.findIndex(p => p.id === partido.id);
        if (index >= 0) {
          const updated = [...prev];
          updated[index] = partido;
          return updated;
        }
        return [...prev, partido];
      });
    });

    return () => {
      disconnectWebSocket();
    };
  }, []);

  useEffect(() => {
    if (usuario?.rol === 'ADMIN') {
      setMetricsLoading(true);
      api.get('/admin/dashboard')
        .then(res => { setMetrics(res.data); setMetricsError(''); })
        .catch(err => setMetricsError('Error al cargar métricas'))
        .finally(() => setMetricsLoading(false));

      setActivityLoading(true);
      api.get('/admin/activity')
        .then(res => { setActivity(res.data); setActivityError(''); })
        .catch(err => setActivityError('Error al cargar actividad'))
        .finally(() => setActivityLoading(false));

      setSystemLoading(true);
      api.get('/admin/system')
        .then(res => { setSystem(res.data); setSystemError(''); })
        .catch(err => setSystemError('Error al cargar estado del sistema'))
        .finally(() => setSystemLoading(false));
    }
  }, [usuario]);

  const isAdmin = usuario?.rol === 'ADMIN';

  const getErrorMessage = (err: any): string => {
    if (!err.response) return 'Error de conexión. Intenta más tarde.';
    const status = err.response.status;
    const data = err.response.data;
    if (status === 401 || status === 403) return 'Tu sesión expiró. Por favor inicia sesión.';
    if (status === 400) return data?.error || 'Solicitud inválida';
    if (status === 404) return data?.error || 'Recurso no encontrado';
    if (status >= 500) return 'Error del servidor. Intenta más tarde.';
    return data?.error || 'Ocurrió un error';
  };

  const handleCrear = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const codigoGenerado = Math.random().toString(36).substring(2, 10).toUpperCase();
      await crearQuiniela(nombre, codigoGenerado);
      setShowCrear(false);
      setNombre('');
    } catch (err: any) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const handleUnirse = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await unirseQuiniela(codigo);
      setShowUnirse(false);
      setCodigo('');
    } catch (err: any) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  if (isAdmin) {
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
              <button onClick={logout} className="btn-logout">Cerrar Sesión</button>
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

  return (
    <div className="dashboard">
      <header className="header">
        <h1>Quiniela</h1>
        <div className="header-actions">
          <Link to="/grupos" className="btn-grupos">Grupos FIFA</Link>
          {usuario?.rol === 'ADMIN' && (
            <Link to="/resultados" className="btn-grupos">Resultados</Link>
          )}
          <div className="header-right">
            <span className="user-info">{usuario?.nombre}</span>
            <button onClick={logout} className="btn-logout">Cerrar Sesión</button>
          </div>
        </div>
      </header>

      <main className="main-content">
        <div className="actions-bar">
          <h2>Mis Quinielas</h2>
          <div className="buttons-row">
            <button onClick={() => setShowCrear(true)} className="btn-primary">
              + Crear Quiniela
            </button>
            <button onClick={() => setShowUnirse(true)} className="btn-secondary">
              Unirse con Código
            </button>
          </div>
        </div>

        {quinielas.length === 0 ? (
          <div className="empty-state">
            <p>No tienes quinielas aún</p>
            <p>Crea una o únete a una</p>
          </div>
        ) : (
          <div className="quinielas-grid">
            {quinielas.map((q: any) => (
              <div key={q.id} className="quiniela-card">
                <h3>{q.nombre}</h3>
                <p className="codigo">Código: {q.codigoInvitacion}</p>
                <p className="puntos">Puntos: {q.puntosTotales}</p>
                <div className="card-actions">
                  <Link to={`/quiniela/${q.id}`} className="btn-small">
                    Ver Detalle
                  </Link>
                  <Link to={`/pronosticos/${q.id}`} className="btn-small">
                    Pronósticos
                  </Link>
                </div>
              </div>
            ))}
</div>
        )}

      {partidosEnVivo.length > 0 && (
        <div className="partidos-en-vivo">
            <h3>🔴 Partidos en Vivo</h3>
            <div className="partidos-grid">
              {partidosEnVivo.filter(p => p.estado === 'EN_CURSO').map(partido => (
                <div key={partido.id} className="partido-card live">
                  <span className="equipo">{partido.equipoLocal}</span>
                  <span className="marcador">{partido.golesLocalReal} - {partido.golesVisitanteReal}</span>
                  <span className="equipo">{partido.equipoVisitante}</span>
                </div>
              ))}
            </div>
          </div>
        )}
      </main>

      {showCrear && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>Crear Quiniela</h3>
            {error && <div className="error-message">{error}</div>}
            <form onSubmit={handleCrear}>
              <div className="form-group">
                <label>Nombre de la Quiniela</label>
                <input
                  type="text"
                  value={nombre}
                  onChange={(e) => setNombre(e.target.value)}
                  required
                />
              </div>
              <div className="modal-buttons">
                <button type="button" onClick={() => setShowCrear(false)} className="btn-cancel" disabled={submitting}>
                  Cancelar
                </button>
                <button type="submit" className="btn-primary" disabled={submitting}>
                  {submitting ? <Spinner /> : 'Crear'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showUnirse && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>Unirse a Quiniela</h3>
            {error && <div className="error-message">{error}</div>}
            <form onSubmit={handleUnirse}>
              <div className="form-group">
                <label>Código de Invitación</label>
                <input
                  type="text"
                  value={codigo}
                  onChange={(e) => setCodigo(e.target.value)}
                  required
                />
              </div>
              <div className="modal-buttons">
                <button type="button" onClick={() => setShowUnirse(false)} className="btn-cancel" disabled={submitting}>
                  Cancelar
                </button>
                <button type="submit" className="btn-primary" disabled={submitting}>
                  {submitting ? <Spinner /> : 'Unirse'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useStore } from '../store/useStore';
import { connectWebSocket, disconnectWebSocket } from '../services/websocket';
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

  useEffect(() => {
    fetchPerfil();
    fetchQuinielas();
    
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

  const isAdmin = usuario?.rol === 'ADMIN';

  const handleCrear = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const codigoGenerado = Math.random().toString(36).substring(2, 10).toUpperCase();
      await crearQuiniela(nombre, codigoGenerado);
      setShowCrear(false);
      setNombre('');
    } catch (err: any) {
      setError(err.response?.data?.error || 'Error al crear quiniela');
    }
  };

  const handleUnirse = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await unirseQuiniela(codigo);
      setShowUnirse(false);
      setCodigo('');
    } catch (err: any) {
      setError(err.response?.data?.error || 'Error al unirse');
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
            <div className="header-right">
              <span className="user-info">{usuario?.nombre}</span>
              <button onClick={logout} className="btn-logout">Cerrar Sesión</button>
            </div>
          </div>
        </header>

        <main className="main-content">
          <div className="admin-dashboard">
            <h2>Bienvenido, Administrador</h2>
            <p className="admin-desc">Gestiona los resultados de los partidos y monitorea el mundial.</p>
            
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
                <button type="button" onClick={() => setShowCrear(false)} className="btn-cancel">
                  Cancelar
                </button>
                <button type="submit" className="btn-primary">
                  Crear
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
                <button type="button" onClick={() => setShowUnirse(false)} className="btn-cancel">
                  Cancelar
                </button>
                <button type="submit" className="btn-primary">
                  Unirse
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
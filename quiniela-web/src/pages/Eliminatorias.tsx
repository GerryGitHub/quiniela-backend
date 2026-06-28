import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { eliminatoriasService } from '../services/api';
import { useStore } from '../store/useStore';
import './Eliminatorias.css';

interface SlotOrigen {
  tipo: string;
  grupos?: string | null;
  partidoOrigen?: string | null;
  esGanador?: boolean | null;
}

interface BracketSlot {
  codigo: string;
  ronda: string;
  orden: number;
  equipoLocal: string | null;
  equipoVisitante: string | null;
  localSlot: SlotOrigen | null;
  visitanteSlot: SlotOrigen | null;
  resuelto: boolean;
}

interface BracketPreview {
  rondas: Record<string, BracketSlot[]>;
  gruposActivos: boolean;
}

interface StatusData {
  faseGruposActiva: boolean;
  quinielaGrupos: number | null;
  rondaActual: string | null;
  bracket: BracketPreview | null;
}

const RONDA_LABELS: Record<string, string> = {
  R32: 'Dieciseisavos', R16: 'Octavos', QF: 'Cuartos',
  SF: 'Semifinales', '3RD': 'Tercer Lugar', FINAL: 'Final',
};

const RONDA_ORDER = ['R32', 'R16', 'QF', 'SF', '3RD', 'FINAL'];

function displayName(name: string | null) {
  if (!name) return null;
  const m = name.match(/^([WL])(\d+)$/);
  if (m) return m[1] === 'W' ? `Ganador P${m[2]}` : `Perdedor P${m[2]}`;
  return name;
}

export default function Eliminatorias() {
  const { usuario, fetchPerfil } = useStore();
  const [status, setStatus] = useState<StatusData | null>(null);
  const [preview, setPreview] = useState<BracketPreview | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [creating, setCreating] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [createError, setCreateError] = useState('');
  const [createSuccess, setCreateSuccess] = useState('');

  useEffect(() => {
    fetchPerfil();
  }, []);

  useEffect(() => {
    if (!usuario) {
      setLoading(false);
      return;
    }
    if (usuario.rol !== 'ADMIN') {
      setLoading(false);
      return;
    }

    Promise.all([
      eliminatoriasService.getStatus().catch(() => null),
      eliminatoriasService.getPreview().catch(() => null),
    ])
      .then(([statusData, previewData]) => {
        setStatus(statusData);
        setPreview(previewData);
      })
      .catch(() => setError('Error al cargar eliminatorias'))
      .finally(() => setLoading(false));
  }, [usuario]);

  const handleCrear = async () => {
    if (!status?.quinielaGrupos) return;
    setCreating(true);
    setCreateError('');
    setCreateSuccess('');

    try {
      const rondaActual = status.rondaActual || 'R32';
      const nombreQuiniela = `Eliminatorias - ${RONDA_LABELS[rondaActual] || rondaActual}`;
      const result = await eliminatoriasService.crear({
        nombreQuiniela,
        quinielaGruposId: status.quinielaGrupos,
      });
      setCreateSuccess(`Quiniela "${result.nombre}" creada con ${result.partidosCreados} partidos`);
      setShowConfirm(false);
      // Refresh status
      const newStatus = await eliminatoriasService.getStatus();
      setStatus(newStatus);
    } catch (err: any) {
      setCreateError(err?.response?.data?.body?.message || 'Error al crear eliminatorias');
    } finally {
      setCreating(false);
    }
  };

  if (!usuario) return <div className="loading">Cargando...</div>;
  if (usuario.rol !== 'ADMIN') {
    return (
      <div className="eliminatorias-page">
        <header className="header">
          <Link to="/dashboard" className="btn-back">← Volver</Link>
          <h1>Eliminatorias</h1>
        </header>
        <div className="content">
          <p className="no-access">Acceso solo para administradores</p>
        </div>
      </div>
    );
  }

  if (loading) return <div className="loading">Cargando...</div>;

  const rondasDisponibles = RONDA_ORDER.filter(
    (r) => preview?.rondas[r] && preview.rondas[r].length > 0
  );

  return (
    <div className="eliminatorias-page">
      <header className="header">
        <Link to="/dashboard" className="btn-back">← Volver</Link>
        <h1>Eliminatorias</h1>
      </header>

      <div className="content">
        {error && <div className="error-msg">{error}</div>}
        {createSuccess && <div className="success-msg">{createSuccess}</div>}

        <div className="status-card">
          <h3>Estado</h3>
          <div className="status-grid">
            <div className="status-item">
              <span className="status-label">Fase de grupos</span>
              <span className={`status-value ${status?.faseGruposActiva ? 'activo' : 'inactivo'}`}>
                {status?.faseGruposActiva ? 'Activa' : 'Finalizada'}
              </span>
            </div>
            <div className="status-item">
              <span className="status-label">Ronda actual</span>
              <span className="status-value">{RONDA_LABELS[status?.rondaActual || ''] || status?.rondaActual || '—'}</span>
            </div>
            <div className="status-item">
              <span className="status-label">Quiniela de grupos</span>
              <span className="status-value">{status?.quinielaGrupos ? `#${status.quinielaGrupos}` : '—'}</span>
            </div>
          </div>

          {status && !status.rondaActual && status.quinielaGrupos && (
            <button
              className="btn-crear"
              onClick={() => setShowConfirm(true)}
              disabled={creating}
            >
              {creating ? 'Generando...' : 'Generar Eliminatorias'}
            </button>
          )}
        </div>

        {preview?.gruposActivos && (
          <div className="info-banner">
            Así quedarían las eliminatorias si hoy terminara la fase de grupos
          </div>
        )}

        {rondasDisponibles.length === 0 && (
          <p className="empty-msg">
            Los cruces se mostrarán cuando haya resultados en la fase de grupos
          </p>
        )}

        {rondasDisponibles.map((ronda) => (
          <div key={ronda} className="ronda-section">
            <h3 className="ronda-title">{RONDA_LABELS[ronda] || ronda}</h3>
            <div className="partidos-list">
              {[...(preview?.rondas[ronda] ?? [])]
                .sort((a, b) => a.orden - b.orden)
                .map((p) => (
                  <div key={p.codigo} className={`partido-card ${p.resuelto ? '' : 'no-resuelto'}`}>
                    <span className="partido-codigo">{p.codigo}</span>
                    <span className="equipo">{displayName(p.equipoLocal) || 'Por definir'}</span>
                    <span className="vs">vs</span>
                    <span className="equipo">{displayName(p.equipoVisitante) || 'Por definir'}</span>
                  </div>
                ))}
            </div>
          </div>
        ))}
      </div>

      {showConfirm && (
        <div className="modal-overlay" onClick={() => setShowConfirm(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>Generar Eliminatorias</h3>
            <p>Se cerrará la quiniela de grupos y se creará una nueva quiniela con los partidos de la siguiente ronda.</p>
            <p className="modal-ronda">
              Ronda a generar: <strong>{RONDA_LABELS[status?.rondaActual || 'R32'] || status?.rondaActual || 'R32'}</strong>
            </p>
            {createError && <div className="error-msg">{createError}</div>}
            <div className="modal-actions">
              <button className="btn-cancel" onClick={() => setShowConfirm(false)}>Cancelar</button>
              <button className="btn-confirm" onClick={handleCrear} disabled={creating}>
                {creating ? 'Generando...' : 'Generar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

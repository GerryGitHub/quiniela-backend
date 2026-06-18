import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useStore } from '../store/useStore';
import api from '../services/api';
import Spinner from '../components/Spinner';

interface EquipoEst {
  equipoId: number;
  nombre: string;
  grupo: string | null;
  rankingFifa: number | null;
  puntosFairPlay: number;
}

export default function EstadisticasEquipos() {
  const navigate = useNavigate();
  const { usuario, fetchPerfil, logout } = useStore();
  const [equipos, setEquipos] = useState<EquipoEst[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [editValues, setEditValues] = useState<Record<number, { rankingFifa: string; puntosFairPlay: string }>>({});
  const [saving, setSaving] = useState<Record<number, boolean>>({});
  const [successMsg, setSuccessMsg] = useState('');

  useEffect(() => {
    fetchPerfil();
  }, []);

  useEffect(() => {
    if (usuario) {
      if (usuario.rol !== 'ADMIN') {
        navigate('/login', { replace: true });
        return;
      }
      loadEquipos();
    }
  }, [usuario]);

  const loadEquipos = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.get('/admin/equipos-estadisticas');
      const data = res.data as EquipoEst[];
      setEquipos(data);
      const vals: Record<number, { rankingFifa: string; puntosFairPlay: string }> = {};
      data.forEach((e: EquipoEst) => {
        vals[e.equipoId] = {
          rankingFifa: e.rankingFifa?.toString() ?? '',
          puntosFairPlay: e.puntosFairPlay.toString(),
        };
      });
      setEditValues(vals);
    } catch {
      setError('Error al cargar equipos');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async (equipoId: number) => {
    const vals = editValues[equipoId];
    if (!vals) return;
    setSaving((prev) => ({ ...prev, [equipoId]: true }));
    setSuccessMsg('');
    try {
      await api.put(`/admin/equipos/${equipoId}/estadisticas`, {
        rankingFifa: vals.rankingFifa ? parseInt(vals.rankingFifa, 10) : null,
        puntosFairPlay: vals.puntosFairPlay ? parseInt(vals.puntosFairPlay, 10) : 0,
      });
      setSuccessMsg('Estadísticas guardadas');
      setEquipos((prev) =>
        prev.map((eq) =>
          eq.equipoId === equipoId
            ? {
                ...eq,
                rankingFifa: vals.rankingFifa ? parseInt(vals.rankingFifa, 10) : null,
                puntosFairPlay: vals.puntosFairPlay ? parseInt(vals.puntosFairPlay, 10) : 0,
              }
            : eq,
        ),
      );
    } catch {
      setError('Error al guardar estadísticas');
    } finally {
      setSaving((prev) => ({ ...prev, [equipoId]: false }));
    }
  };

  const logoutAndRedirect = () => {
    logout();
    navigate('/login', { replace: true });
  };

  if (!usuario) return <div className="loading-container"><Spinner /></div>;

  const sorted = [...equipos].sort((a, b) => {
    if (a.grupo && b.grupo) {
      const g = a.grupo.localeCompare(b.grupo);
      if (g !== 0) return g;
    } else if (a.grupo) return -1;
    else if (b.grupo) return 1;
    return a.nombre.localeCompare(b.nombre);
  });

  return (
    <div className="dashboard">
      <header className="header">
        <h1>Estadísticas de Equipos</h1>
        <div className="header-actions">
          <Link to="/dashboard" className="btn-grupos">Dashboard</Link>
          <div className="header-right">
            <span className="user-info">{usuario?.nombre}</span>
            <button onClick={logoutAndRedirect} className="btn-logout">Cerrar Sesión</button>
          </div>
        </div>
      </header>
      <main className="main-content">
        <div className="admin-dashboard">
          {successMsg && <div className="success-message">{successMsg}</div>}
          {error && <div className="error-message">{error}</div>}
          {loading ? (
            <div className="loading-section"><Spinner /></div>
          ) : (
            <div className="table-container">
              <table className="estadisticas-table">
                <thead>
                  <tr>
                    <th>Equipo</th>
                    <th>Grupo</th>
                    <th>Ranking FIFA</th>
                    <th>Puntos Fair Play</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {sorted.map((eq) => (
                    <tr key={eq.equipoId}>
                      <td className="td-equipo">{eq.nombre}</td>
                      <td className="td-grupo">{eq.grupo || '-'}</td>
                      <td>
                        <input
                          type="number"
                          className="est-input"
                          value={editValues[eq.equipoId]?.rankingFifa ?? ''}
                          onChange={(e) =>
                            setEditValues((prev) => ({
                              ...prev,
                              [eq.equipoId]: { ...prev[eq.equipoId], rankingFifa: e.target.value },
                            }))
                          }
                          min={0}
                          max={3000}
                        />
                      </td>
                      <td>
                        <input
                          type="number"
                          className="est-input"
                          value={editValues[eq.equipoId]?.puntosFairPlay ?? 0}
                          onChange={(e) =>
                            setEditValues((prev) => ({
                              ...prev,
                              [eq.equipoId]: { ...prev[eq.equipoId], puntosFairPlay: e.target.value },
                            }))
                          }
                          min={-50}
                          max={50}
                        />
                      </td>
                      <td>
                        <button
                          className="btn-save"
                          onClick={() => handleSave(eq.equipoId)}
                          disabled={saving[eq.equipoId]}
                        >
                          {saving[eq.equipoId] ? 'Guardando...' : 'Guardar'}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

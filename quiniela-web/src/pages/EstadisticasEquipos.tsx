import { useEffect, useState, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useStore } from '../store/useStore';
import api from '../services/api';
import Spinner from '../components/Spinner';
import './Dashboard.css';

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
  const [dirty, setDirty] = useState<Set<number>>(new Set());
  const [savingAll, setSavingAll] = useState(false);
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
      setDirty(new Set());
    } catch {
      setError('Error al cargar equipos');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (equipoId: number, field: 'rankingFifa' | 'puntosFairPlay', value: string) => {
    setEditValues((prev) => ({ ...prev, [equipoId]: { ...prev[equipoId], [field]: value } }));
    setDirty((prev) => new Set(prev).add(equipoId));
  };

  const handleSaveAll = async () => {
    if (dirty.size === 0) return;
    setSavingAll(true);
    setSuccessMsg('');
    setError('');
    let saved = 0;
    for (const equipoId of dirty) {
      const vals = editValues[equipoId];
      if (!vals) continue;
      try {
        await api.put(`/admin/equipos/${equipoId}/estadisticas`, {
          rankingFifa: vals.rankingFifa ? parseInt(vals.rankingFifa, 10) : null,
          puntosFairPlay: vals.puntosFairPlay ? parseInt(vals.puntosFairPlay, 10) : 0,
        });
        saved++;
      } catch {
        setError(`Error al guardar ${equipos.find((e) => e.equipoId === equipoId)?.nombre || equipoId}`);
      }
    }
    if (saved > 0) {
      setSuccessMsg(`${saved}/${dirty.size} equipos guardados`);
      loadEquipos();
    } else {
      setDirty(new Set());
    }
    setSavingAll(false);
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
        <Link to="/dashboard" className="btn-back">← Volver</Link>
        <h1>Estadísticas de Equipos</h1>
        <div className="header-right" style={{ marginLeft: 'auto' }}>
          <span className="user-info">{usuario?.nombre}</span>
          <button onClick={logoutAndRedirect} className="btn-logout">Cerrar Sesión</button>
        </div>
      </header>
      <main className="main-content">
        <div className="admin-dashboard">
          {successMsg && <div className="success-message">{successMsg}</div>}
          {error && <div className="error-message">{error}</div>}
          {loading ? (
            <div className="loading-section"><Spinner /></div>
          ) : (
            <>
              <div className="table-container">
                <table className="estadisticas-table">
                  <thead>
                    <tr>
                      <th>Equipo</th>
                      <th>Grupo</th>
                      <th>Ranking FIFA</th>
                      <th>Puntos Fair Play</th>
                    </tr>
                  </thead>
                  <tbody>
                    {sorted.map((eq) => (
                      <tr key={eq.equipoId} className={dirty.has(eq.equipoId) ? 'row-dirty' : ''}>
                        <td className="td-equipo">{eq.nombre}</td>
                        <td className="td-grupo">{eq.grupo || '-'}</td>
                        <td>
                          <input
                            type="number"
                            className="est-input"
                            value={editValues[eq.equipoId]?.rankingFifa ?? ''}
                            onChange={(e) => handleChange(eq.equipoId, 'rankingFifa', e.target.value)}
                            min={0} max={3000}
                          />
                        </td>
                        <td>
                          <input
                            type="number"
                            className="est-input"
                            value={editValues[eq.equipoId]?.puntosFairPlay ?? 0}
                            onChange={(e) => handleChange(eq.equipoId, 'puntosFairPlay', e.target.value)}
                            min={-50} max={50}
                          />
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div className="save-all-bar">
                <span className="dirty-count">{dirty.size} equipo(s) modificado(s)</span>
                <button className="btn-save-all" onClick={handleSaveAll} disabled={dirty.size === 0 || savingAll}>
                  {savingAll ? 'Guardando...' : `Guardar Todo (${dirty.size})`}
                </button>
              </div>
            </>
          )}
        </div>
      </main>
    </div>
  );
}

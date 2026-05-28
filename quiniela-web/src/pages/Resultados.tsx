import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import Spinner from '../components/Spinner';
import './Resultados.css';

interface Partido {
  id: number;
  equipoLocal: string;
  equipoVisitante: string;
  fechaHora: string;
  grupo?: string;
  grupoId?: number;
  equipoLocalId?: number;
  equipoVisitanteId?: number;
  golesLocalReal: number | null;
  golesVisitanteReal: number | null;
  estado: string;
}

interface ResultadoUpdate {
  golesLocal: number;
  golesVisitante: number;
}

export default function Resultados() {
  const [partidos, setPartidos] = useState<Partido[]>([]);
  const [loading, setLoading] = useState(true);
  const [editando, setEditando] = useState<number | null>(null);
  const [resultado, setResultado] = useState<ResultadoUpdate>({ golesLocal: 0, golesVisitante: 0 });
  const [mensaje, setMensaje] = useState('');
  const [submitting, setSubmitting] = useState<number | null>(null);

  const filtrarPartidosDelDia = (data: Partido[]) => {
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const manana = new Date(hoy);
    manana.setDate(manana.getDate() + 1);
    
    return data.filter((p: Partido) => {
      const fechaPartido = new Date(p.fechaHora);
      return fechaPartido >= hoy && fechaPartido < manana;
    });
  };

  useEffect(() => {
    api.get('/api/resultados/partidos')
      .then(res => {
        setPartidos(filtrarPartidosDelDia(res.data));
        setLoading(false);
      })
      .catch(err => {
        console.error(err);
        setLoading(false);
      });
  }, []);

  const handleActualizar = async (partidoId: number) => {
    setSubmitting(partidoId);
    try {
      const res = await api.patch(`/api/resultados/${partidoId}`, resultado);
      
      if (res.status === 200) {
        setMensaje('¡Resultado actualizado!');
        setEditando(null);
        setTimeout(() => setMensaje(''), 3000);
        api.get('/api/resultados/partidos')
          .then(res => setPartidos(filtrarPartidosDelDia(res.data)));
      } else {
        setMensaje('Error al actualizar');
      }
    } catch (err: any) {
      setMensaje(err.response?.data?.error || 'Error al actualizar');
    } finally {
      setSubmitting(null);
    }
  };

const handleFinalizar = async (partidoId: number) => {
    setSubmitting(partidoId);
    try {
      const res = await api.patch(`/api/resultados/${partidoId}/finalizar`);
      
      if (res.status === 200) {
        setMensaje('¡Partido finalizado! Puntos calculados.');
        setTimeout(() => setMensaje(''), 3000);
        api.get('/api/resultados/partidos')
          .then(res => setPartidos(filtrarPartidosDelDia(res.data)));
      } else {
        setMensaje('Error al finalizar partido');
      }
    } catch (err: any) {
      setMensaje(err.response?.data?.error || 'Error al finalizar partido');
    } finally {
      setSubmitting(null);
    }
  };

  const puedeIngresarResultado = (fechaHora: string, estado: string): boolean => {
    if (estado === 'FINALIZADO' || estado === 'POR_COMENZAR' || estado === 'PENDIENTE') return false;
    const partidoTime = new Date(fechaHora).getTime();
    const ahora = Date.now();
    const treintaMinutos = 30 * 60 * 1000;
    return partidoTime <= ahora + treintaMinutos;
  };

  const partidosOrdenados = [...partidos].sort((a, b) => 
    new Date(a.fechaHora).getTime() - new Date(b.fechaHora).getTime()
  );

  const iniciarEdicion = (partido: Partido) => {
    setEditando(partido.id);
    setResultado({
      golesLocal: partido.golesLocalReal || 0,
      golesVisitante: partido.golesVisitanteReal || 0
    });
  };

  if (loading) return <div className="loading">Cargando...</div>;

  return (
    <div className="resultados-page">
      <header className="header">
        <Link to="/dashboard" className="btn-back">← Volver</Link>
        <h1>Resultados - Partidos</h1>
      </header>

      <div className="content">
        {mensaje && <div className="mensaje">{mensaje}</div>}

        <div className="intro">
          <p>Administra los resultados de los partidos. Los resultados se sincronizarán automáticamente o puedes ingresarlos manualmente.</p>
        </div>

        <div className="partidos-list">
          {partidosOrdenados.length === 0 ? (
            <p className="no-partidos">No hay partidos</p>
          ) : (
            partidosOrdenados.map(partido => {
              const puedeEditar = puedeIngresarResultado(partido.fechaHora, partido.estado);
              return (
                <div key={partido.id} className={`partido-card ${partido.golesLocalReal !== null ? 'completado' : ''} ${!puedeEditar ? 'pendiente' : ''}`}>
                  <div className="partido-header">
                    <span className="grupo-badge">{partido.grupo ? `Grupo ${partido.grupo}` : 'Partido'}</span>
                    <span className="fecha">
                      {new Date(partido.fechaHora).toLocaleString('es-MX', {
                        day: 'numeric',
                        month: 'short',
                        hour: '2-digit',
                        minute: '2-digit'
                      })}
                    </span>
                  </div>

                  <div className="equipos">
                    <span className="equipo">{partido.equipoLocal}</span>
                    
                    {editando === partido.id ? (
                      <div className="marcador-input">
                        <input
                          type="number"
                          min="0"
                          value={resultado.golesLocal}
                          onChange={e => setResultado({ ...resultado, golesLocal: parseInt(e.target.value) || 0 })}
                        />
                        <span>-</span>
                        <input
                          type="number"
                          min="0"
                          value={resultado.golesVisitante}
                          onChange={e => setResultado({ ...resultado, golesVisitante: parseInt(e.target.value) || 0 })}
                        />
                      </div>
                    ) : (
                      <span className={`marcador ${partido.golesLocalReal !== null ? 'resultado' : ''}`}>
                        {partido.golesLocalReal !== null 
                          ? `${partido.golesLocalReal} - ${partido.golesVisitanteReal}` 
                          : puedeEditar ? 'Sin resultado' : 'Partido no iniciado'}
                      </span>
                    )}
                    
                    <span className="equipo">{partido.equipoVisitante}</span>
                  </div>

                  <div className="acciones">
                      {editando === partido.id ? (
                      <>
                        <button 
                          className="btn-guardar"
                          onClick={() => handleActualizar(partido.id)}
                          disabled={submitting === partido.id}
                        >
                          {submitting === partido.id ? <Spinner size={14} /> : 'Guardar'}
                        </button>
                        <button 
                          className="btn-cancelar"
                          onClick={() => setEditando(null)}
                        >
                          Cancelar
                        </button>
                      </>
                    ) : (
                      <>
                        <button 
                          className="btn-editar"
                          onClick={() => iniciarEdicion(partido)}
                          disabled={!puedeEditar}
                        >
                          {partido.golesLocalReal !== null ? 'Modificar Resultado' : 'Ingresar Resultado'}
                        </button>
                        {partido.golesLocalReal !== null && partido.golesVisitanteReal !== null && partido.estado !== 'FINALIZADO' && (
                          <button 
                            className="btn-finalizar"
                            onClick={() => handleFinalizar(partido.id)}
                            disabled={submitting === partido.id}
                          >
                            {submitting === partido.id ? <Spinner size={14} /> : 'Finalizar Partido'}
                          </button>
                        )}
                      </>
                    )}
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
}
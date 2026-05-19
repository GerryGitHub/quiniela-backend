import { useEffect, useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useStore } from '../store/useStore';
import Spinner from '../components/Spinner';
import './Pronosticos.css';

interface PronosticoLocal {
  idPartido: number;
  golesLocalPredicho: number;
  golesVisitantePredicho: number;
}

interface PartidoAgrupado {
  fecha: string;
  partidos: any[];
}

const MINUTOS_BLOQUEO = 30; // Minutes before match to block

export default function Pronosticos() {
  const { quinielaId } = useParams<{ quinielaId: string }>();
  const navigate = useNavigate();
  const { partidos, fetchPartidos, guardarPronosticosBatch, misPronosticos, fetchMisPronosticos } = useStore();
  const [pronosticos, setPronosticos] = useState<PronosticoLocal[]>([]);
  const [guardando, setGuardando] = useState(false);
  const [mensaje, setMensaje] = useState('');

  useEffect(() => {
    fetchPartidos();
    if (quinielaId) {
      fetchMisPronosticos(parseInt(quinielaId));
    }
  }, [quinielaId]);

  useEffect(() => {
    if (partidos.length > 0) {
      const inicial = partidos.map((p: any) => {
        const existente = misPronosticos.find((mp: any) => mp.partido.id === p.id);
        return {
          idPartido: p.id,
          golesLocalPredicho: existente?.golesLocalPredicho ?? 0,
          golesVisitantePredicho: existente?.golesVisitantePredicho ?? 0,
        };
      });
      setPronosticos(inicial);
    }
  }, [partidos, misPronosticos]);

  const puedeModificar = (partido: any): boolean => {
    if (partido.estado !== 'PENDIENTE') return false;
    const fechaPartido = new Date(partido.fechaHora);
    const ahora = new Date();
    const minutosRestantes = (fechaPartido.getTime() - ahora.getTime()) / (1000 * 60);
    return minutosRestantes > MINUTOS_BLOQUEO;
  };

  const getBloqueoInfo = (partido: any): string => {
    if (partido.estado !== 'PENDIENTE') return 'Partido no disponible';
    const fechaPartido = new Date(partido.fechaHora);
    const ahora = new Date();
    const minutosRestantes = (fechaPartido.getTime() - ahora.getTime()) / (1000 * 60);
    if (minutosRestantes <= 0) return 'Partido started';
    if (minutosRestantes <= MINUTOS_BLOQUEO) return `Bloqueado (${Math.round(minutosRestantes)} min)`;
    return `Disponible (${Math.round(minutosRestantes)} min)`;
  };

  const partidosPorDia: PartidoAgrupado[] = useMemo(() => {
    const grouped: { [key: string]: any[] } = {};
    const partidosOrdenados = [...partidos].sort(
      (a, b) => new Date(a.fechaHora).getTime() - new Date(b.fechaHora).getTime()
    );
    
    partidosOrdenados.forEach((partido: any) => {
      const fecha = new Date(partido.fechaHora);
      const key = fecha.toLocaleDateString('es-MX', { 
        weekday: 'long', 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
      });
      if (!grouped[key]) grouped[key] = [];
      grouped[key].push(partido);
    });
    
    return Object.entries(grouped).map(([fecha, partidos]) => ({
      fecha,
      partidos
    }));
  }, [partidos]);

  const handlePronosticoChange = (idPartido: number, esLocal: boolean, valor: number) => {
    setPronosticos((prev) =>
      prev.map((p) =>
        p.idPartido === idPartido
          ? {
              ...p,
              ...(esLocal
                ? { golesLocalPredicho: valor }
                : { golesVisitantePredicho: valor }),
            }
          : p
      )
    );
  };

  const handleGuardar = async () => {
    if (!quinielaId) return;
    setGuardando(true);
    setMensaje('');
    try {
      await guardarPronosticosBatch(parseInt(quinielaId), pronosticos);
      setMensaje('¡Pronósticos guardados exitosamente!');
      setTimeout(() => setMensaje(''), 3000);
    } catch (error) {
      setMensaje('Error al guardar los pronósticos');
    }
    setGuardando(false);
  };

  const hayCambios = () => {
    return pronosticos.some((p: any) => {
      const existente = misPronosticos.find((mp: any) => mp.partido.id === p.idPartido);
      return (
        !existente ||
        existente.golesLocalPredicho !== p.golesLocalPredicho ||
        existente.golesVisitantePredicho !== p.golesVisitantePredicho
      );
    });
  };

  const hayPronosticosModificables = () => {
    return pronosticos.some((p: any) => {
      const partido = partidos.find((par: any) => par.id === p.idPartido);
      return partido && puedeModificar(partido);
    });
  };

  return (
    <div className="pronosticos-page">
      <header className="header">
        <button onClick={() => navigate('/dashboard')} className="btn-back">
          ← Volver
        </button>
        <h1>Mis Pronósticos</h1>
      </header>

      <div className="content">
        <div className="info-box">
          <p>Predice el marcador de cada partido. Puedes modificar tus pronósticos mientras el partido no haya comenzado (mínimo {MINUTOS_BLOQUEO} minutos antes).</p>
        </div>

        {mensaje && <div className="mensaje-exito">{mensaje}</div>}

        <div className="partidos-agrupados">
          {partidosPorDia.map((dia, index) => (
            <div key={index} className="dia-grupo">
              <h3 className="dia-titulo">{dia.fecha}</h3>
              <div className="partidos-grid">
                {dia.partidos.map((partido) => {
                  const pronostico = pronosticos.find((p) => p.idPartido === partido.id);
                  const puede = puedeModificar(partido);
                  const bloqueoInfo = getBloqueoInfo(partido);

                  return (
                    <div key={partido.id} className={`partido-card ${!puede ? 'disabled' : ''}`}>
                      <div className="partido-info">
                        <div className="equipos">
                          <span className="equipo">{partido.equipoLocal}</span>
                          <span className="vs">vs</span>
                          <span className="equipo">{partido.equipoVisitante}</span>
                        </div>
                        <div className="fecha">
                          {new Date(partido.fechaHora).toLocaleString('es-MX', {
                            hour: '2-digit',
                            minute: '2-digit',
                          })}
                        </div>
                        <span className={`estado estado-${partido.estado.toLowerCase()}`}>
                          {partido.estado}
                        </span>
                      </div>

                      <div className="marcador-input">
                        <div className="input-group">
                          <label>Local</label>
                          <input
                            type="number"
                            min="0"
                            value={pronostico?.golesLocalPredicho ?? 0}
                            onChange={(e) =>
                              handlePronosticoChange(partido.id, true, parseInt(e.target.value) || 0)
                            }
                            disabled={!puede}
                          />
                        </div>
                        <span className="separador">-</span>
                        <div className="input-group">
                          <label>Visitante</label>
                          <input
                            type="number"
                            min="0"
                            value={pronostico?.golesVisitantePredicho ?? 0}
                            onChange={(e) =>
                              handlePronosticoChange(partido.id, false, parseInt(e.target.value) || 0)
                            }
                            disabled={!puede}
                          />
                        </div>
                      </div>

                      {!puede && (
                        <div className="bloqueo-info">{bloqueoInfo}</div>
                      )}

                      {partido.golesLocalReal !== null && (
                        <div className="resultado-real">
                          Resultado: {partido.golesLocalReal} - {partido.golesVisitanteReal}
                          {misPronosticos.find((mp: any) => mp.partido.id === partido.id) && (
                            <span className="puntos-obtenidos">
                              {' '}(Puntos: {misPronosticos.find((mp: any) => mp.partido.id === partido.id)?.puntosObtenidos})
                            </span>
                          )}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          ))}
        </div>

        <button
          onClick={handleGuardar}
          disabled={guardando || !hayCambios() || !hayPronosticosModificables()}
          className="btn-guardar"
        >
          {guardando ? <Spinner size={16} /> : 'Guardar Pronósticos'}
        </button>
      </div>
    </div>
  );
}
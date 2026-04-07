import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useStore } from '../store/useStore';
import { Partido } from '../types';
import './Pronosticos.css';

interface PronosticoLocal {
  idPartido: number;
  golesLocalPredicho: number;
  golesVisitantePredicho: number;
}

export default function Pronosticos() {
  const { quinielaId } = useParams<{ quinielaId: string }>();
  const navigate = useNavigate();
  const { partidos, fetchPartidos, guardarPronosticosBatch, loading, misPronosticos, fetchMisPronosticos } = useStore();
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

  const partidosOrdenados = [...partidos].sort(
    (a, b) => new Date(a.fechaHora).getTime() - new Date(b.fechaHora).getTime()
  );

  const puedeModificar = (partido: Partido) => {
    return partido.estado === 'PENDIENTE' && new Date(partido.fechaHora) > new Date();
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
          <p>Predice el marcador de cada partido. Puedes modificar tus pronóstico mientras el partido no haya comenzado.</p>
        </div>

        {mensaje && <div className="mensaje-exito">{mensaje}</div>}

        <div className="partidos-grid">
          {partidosOrdenados.map((partido) => {
            const pronostico = pronosticos.find((p) => p.idPartido === partido.id);
            const puede = puedeModificar(partido);

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
                      day: 'numeric',
                      month: 'short',
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

                {partido.golesLocalReal !== null && (
                  <div className="resultado-real">
                    Resultado: {partido.golesLocalReal} - {partido.golesVisitanteReal}
                  </div>
                )}
              </div>
            );
          })}
        </div>

        <button
          onClick={handleGuardar}
          disabled={guardando || !hayCambios()}
          className="btn-guardar"
        >
          {guardando ? 'Guardando...' : 'Guardar Pronósticos'}
        </button>
      </div>
    </div>
  );
}
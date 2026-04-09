import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
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

  useEffect(() => {
    fetch('http://localhost:8080/api/resultados/partidos')
      .then(res => res.json())
      .then(data => {
        console.log('Todos:', data);
        setPartidos(data);
        setLoading(false);
      })
      .catch(err => {
        console.error(err);
        setLoading(false);
      });
  }, []);

  const handleActualizar = async (partidoId: number) => {
    try {
      const res = await fetch(`http://localhost:8080/api/resultados/${partidoId}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(resultado)
      });
      
      if (res.ok) {
        setMensaje('¡Resultado actualizado!');
        setEditando(null);
        setTimeout(() => setMensaje(''), 3000);
        fetch('http://localhost:8080/api/resultados/partidos')
          .then(res => res.json())
          .then(data => setPartidos(data));
      } else {
        setMensaje('Error al actualizar');
      }
    } catch (err) {
      setMensaje('Error al actualizar');
    }
  };

  const puedeIngresarResultado = (fechaHora: string): boolean => {
    const partidoTime = new Date(fechaHora).getTime();
    const ahora = Date.now();
    // Habilitar 30 minutos antes del partido
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
              const puedeEditar = puedeIngresarResultado(partido.fechaHora);
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
                        >
                          Guardar
                        </button>
                        <button 
                          className="btn-cancelar"
                          onClick={() => setEditando(null)}
                        >
                          Cancelar
                        </button>
                      </>
                    ) : (
                      <button 
                        className="btn-editar"
                        onClick={() => iniciarEdicion(partido)}
                        disabled={!puedeEditar}
                      >
                        {partido.golesLocalReal !== null ? 'Modificar Resultado' : 'Ingresar Resultado'}
                      </button>
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
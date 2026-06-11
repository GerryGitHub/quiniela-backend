import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { gruposService } from '../services/api';
import './Grupos.css';

interface Seleccion {
  id: number;
  nombre: string;
  pais: string;
  grupo: string;
  bandera: string | null;
  partidosJugados: number;
  partidosGanados: number;
  partidosEmpatados: number;
  partidosPerdidos: number;
  golesAFavor: number;
  golesEnContra: number;
  puntos: number;
  diferenciaGoles: number;
}

interface Partido {
  id: number;
  equipoLocal: string;
  equipoVisitante: string;
  fechaHora: string;
  grupo: string;
  golesLocalReal: number | null;
  golesVisitanteReal: number | null;
  estado: string;
}

interface Grupo {
  id: number;
  nombre: string;
  pais: string;
  selecciones: Seleccion[];
  partidos: Partido[];
}

export default function Grupos() {
  const [grupos, setGrupos] = useState<Grupo[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedGroup, setSelectedGroup] = useState<string | null>(null);

  useEffect(() => {
    gruposService.getGrupos()
      .then(data => {
        setGrupos(data.grupos);
        setLoading(false);
      })
      .catch(err => {
        console.error(err);
        setLoading(false);
      });
  }, []);

  const grupoActual = selectedGroup 
    ? grupos.find(g => g.nombre === selectedGroup) 
    : null;

  if (loading) return <div className="loading">Cargando...</div>;

  return (
    <div className="grupos-page">
      <header className="header">
        <Link to="/dashboard" className="btn-back">← Volver</Link>
        <h1>Grupos FIFA - Mundial 2026</h1>
      </header>

      <div className="content">
        {!selectedGroup ? (
          <>
            <p className="intro">Selecciona un grupo para ver la tabla de posiciones y partidos:</p>
            
            <div className="grupos-grid">
              {grupos.map(grupo => (
                <div 
                  key={grupo.id} 
                  className="grupo-card"
                  onClick={() => setSelectedGroup(grupo.nombre)}
                >
                  <h3>Grupo {grupo.nombre}</h3>
                  <p>{grupo.selecciones.length} selecciones</p>
                </div>
              ))}
            </div>
          </>
        ) : (
          <>
            <button className="btn-volver" onClick={() => setSelectedGroup(null)}>
              ← Volver a grupos
            </button>
            
            {grupoActual && (
              <div className="grupo-detalle">
                <h2>Grupo {grupoActual.nombre}</h2>
                
                <table className="tabla-posiciones">
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>Selección</th>
                      <th>PJ</th>
                      <th>PG</th>
                      <th>PE</th>
                      <th>PP</th>
                      <th>GF</th>
                      <th>GC</th>
                      <th>DG</th>
                      <th>Pts</th>
                    </tr>
                  </thead>
                  <tbody>
                    {grupoActual.selecciones.map((sel, index) => (
                      <tr key={sel.id}>
                        <td className="posicion">{index + 1}</td>
                        <td className="equipo">{sel.nombre}</td>
                        <td>{sel.partidosJugados}</td>
                        <td>{sel.partidosGanados}</td>
                        <td>{sel.partidosEmpatados}</td>
                        <td>{sel.partidosPerdidos}</td>
                        <td>{sel.golesAFavor}</td>
                        <td>{sel.golesEnContra}</td>
                        <td className={sel.diferenciaGoles > 0 ? 'positivo' : sel.diferenciaGoles < 0 ? 'negativo' : ''}>
                          {sel.diferenciaGoles > 0 ? '+' : ''}{sel.diferenciaGoles}
                        </td>
                        <td className="puntos">{sel.puntos}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>

                <h3 className="partidos-title">Partidos del Grupo</h3>
                <div className="partidos-list">
                  {grupoActual.partidos.map(partido => (
                    <div key={partido.id} className="partido-row">
                      <div className="equipos">
                        <span className="equipo">{partido.equipoLocal}</span>
                        <span className="marcador">
                          {partido.golesLocalReal !== null 
                            ? `${partido.golesLocalReal} - ${partido.golesVisitanteReal}` 
                            : 'vs'}
                        </span>
                        <span className="equipo">{partido.equipoVisitante}</span>
                      </div>
                      <div className="info">
                        <span className="fecha">
                          {new Date(partido.fechaHora).toLocaleString('es-MX', {
                            day: 'numeric',
                            month: 'short',
                            hour: '2-digit',
                            minute: '2-digit'
                          })}
                        </span>
                        <span className={`estado estado-${partido.estado.toLowerCase()}`}>
                          {partido.estado}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
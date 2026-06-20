import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useStore } from '../store/useStore';
import { LeaderboardEntry } from '../types';
import './QuinielaDetalle.css';

export default function QuinielaDetalle() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { quinielaDetalle, leaderboard, fetchQuinielaDetalle, fetchLeaderboard } = useStore();
  const [activeTab, setActiveTab] = useState<'partidos' | 'participantes'>('partidos');

  useEffect(() => {
    if (id) {
      fetchQuinielaDetalle(parseInt(id));
      fetchLeaderboard(parseInt(id));
    }
  }, [id]);

  if (!quinielaDetalle) {
    return <div className="loading">Cargando...</div>;
  }

  return (
    <div className="quiniela-detalle">
      <header className="header">
        <button onClick={() => navigate('/dashboard')} className="btn-back">
          ← Volver
        </button>
        <h1>{quinielaDetalle.nombre}</h1>
      </header>

      <div className="tabs">
        <button
          className={`tab ${activeTab === 'partidos' ? 'active' : ''}`}
          onClick={() => setActiveTab('partidos')}
        >
          Partidos
        </button>
        <button
          className={`tab ${activeTab === 'participantes' ? 'active' : ''}`}
          onClick={() => setActiveTab('participantes')}
        >
          Participantes
        </button>
      </div>

      <div className="content">
        {activeTab === 'partidos' ? (
          <div className="partidos-section">
            <h2>Partidos</h2>
            <div className="partidos-list">
              {quinielaDetalle.partidos?.length === 0 ? (
                <p className="empty">No hay partidos disponibles</p>
              ) : (
                quinielaDetalle.partidos?.map((partido: any) => (
                  <div key={partido.id} className="partido-card">
                    <div className="equipos">
                      <span className="equipo">{partido.equipoLocal}</span>
                      <span className="vs">vs</span>
                      <span className="equipo">{partido.equipoVisitante}</span>
                    </div>
                    <div className="resultado">
                      {partido.golesLocalReal !== null ? (
                        <span className="marcador">
                          {partido.golesLocalReal} - {partido.golesVisitanteReal}
                        </span>
                      ) : (
                        <span className="pendiente">Por jugar</span>
                      )}
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
                ))
              )}
            </div>
          </div>
        ) : (
          <div className="participantes-section">
            <h2>Tabla de Posiciones</h2>
            <table className="leaderboard">
              <thead>
                <tr>
                  <th>Posición</th>
                  <th>Usuario</th>
                  <th>Puntos</th>
                </tr>
              </thead>
              <tbody>
                {leaderboard.map((entry: LeaderboardEntry) => (
                  <tr key={entry.usuario.id}>
                    <td className="posicion">#{entry.posicion}</td>
                    <td className="usuario">{entry.usuario.nombre}</td>
                    <td className="puntos">{entry.puntosTotales}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="codigo-invitacion">
        <span>Código de invitación: </span>
        <strong>{quinielaDetalle.codigoInvitacion}</strong>
        <button
          onClick={() => navigator.clipboard.writeText(quinielaDetalle.codigoInvitacion)}
          className="btn-copy"
        >
          📋 Copiar
        </button>
      </div>
    </div>
  );
}
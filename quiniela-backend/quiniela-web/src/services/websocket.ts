import { Client } from '@stomp/stompjs';
import api from './api';
import { getApiUrl } from './apiConfig';

let stompClient: Client | null = null;

export const connectWebSocket = async (onMessage: (partido: any) => void) => {
  try {
    const response = await api.get('/api/resultados/en-vivo');
    const partidosEnVivo = response.data;
    partidosEnVivo.forEach((p: any) => onMessage(p));
  } catch (e) {
    console.log('No se pudieron cargar partidos en vivo');
  }

  const apiUrl = getApiUrl().replace('http://', '').replace('https://', '');
  stompClient = new Client({
    webSocketFactory: () => {
      return new WebSocket(`ws://${apiUrl}/ws`);
    },
    onConnect: () => {
      stompClient?.subscribe('/topic/partidos', (message) => {
        const partido = JSON.parse(message.body);
        onMessage(partido);
      });
    },
    onDisconnect: () => {
      console.log('Desconectado del WebSocket');
    },
  });

  stompClient.activate();
  return stompClient;
};

export const disconnectWebSocket = () => {
  stompClient?.deactivate();
  stompClient = null;
};
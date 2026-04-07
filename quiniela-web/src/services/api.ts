import axios from 'axios';
import { PronosticoItem } from '../types';

const API_URL = 'http://localhost:8080';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    console.log('➡️ Token enviado:', token.substring(0, 30) + '...');
    config.headers.Authorization = 'Bearer ' + token;
  } else {
    console.log('➡️ Sin token');
  }
  return config;
});

api.interceptors.response.use(
  (response) => {
    console.log('✅ Respuesta:', response.status);
    return response;
  },
  (error) => {
    console.log('❌ Error:', error.response?.status, error.response?.data);
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('token');
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

export default api;

export const authService = {
  register: async (data: { nombre: string; email: string; password: string }) => {
    const response = await api.post('/auth/register', data);
    return response.data;
  },

  login: async (data: { email: string; password: string }) => {
    const response = await api.post('/auth/login', data);
    console.log('🔑 Login response:', response.data);
    return response.data;
  },

  getPerfil: async () => {
    const response = await api.get('/auth/me');
    return response.data;
  },
};

export const quinielaService = {
  getQuinielas: async () => {
    const response = await api.get('/quinielas');
    return response.data;
  },

  crearQuiniela: async (data: { nombre: string; codigoInvitacion: string }) => {
    const response = await api.post('/quinielas', data);
    return response.data;
  },

  getQuinielaDetalle: async (id: number) => {
    const response = await api.get(`/quinielas/${id}`);
    return response.data;
  },

  unirseQuiniela: async (codigoInvitacion: string) => {
    const response = await api.post('/quinielas/join', { codigoInvitacion });
    return response.data;
  },

  getLeaderboard: async (quinielaId: number) => {
    const response = await api.get(`/quinielas/${quinielaId}/leaderboard`);
    return response.data;
  },
};

export const partidoService = {
  getPartidos: async () => {
    const response = await api.get('/partidos');
    return response.data;
  },
};

export const pronosticoService = {
  getMisPronosticos: async (quinielaId: number) => {
    const response = await api.get(`/pronosticos/quiniela/${quinielaId}`);
    return response.data;
  },

  guardarPronosticosBatch: async (data: { idQuiniela: number; pronosticos: PronosticoItem[] }) => {
    const response = await api.post('/pronosticos/batch', data);
    return response.data;
  },
};
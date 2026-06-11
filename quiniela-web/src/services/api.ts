import axios from 'axios';
import { PronosticoItem } from '../types';
import { getApiUrl } from './apiConfig';

const API_URL = getApiUrl();

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = 'Bearer ' + token;
  }
  return config;
});

api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    const url = error.config?.url || '';
    const status = error.response?.status;

    if (status === 401 || status === 403) {
      localStorage.removeItem('token');
      localStorage.setItem('sessionExpired', 'true');
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

export const gruposService = {
  getGrupos: async () => {
    const response = await api.get('/api/grupos');
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
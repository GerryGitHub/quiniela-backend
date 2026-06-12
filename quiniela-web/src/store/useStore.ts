import { create } from 'zustand';
import { authService, quinielaService, pronosticoService, partidoService } from '../services/api';

interface AppState {
  usuario: any;
  token: string | null;
  isAuthenticated: boolean;
  loading: boolean;
  quinielas: any[];
  quinielaDetalle: any;
  leaderboard: any[];
  partidos: any[];
  misPronosticos: any[];

  login: (email: string, password: string) => Promise<void>;
  register: (nombre: string, email: string, password: string) => Promise<void>;
  logout: () => void;
  fetchPerfil: () => Promise<void>;

  fetchQuinielas: () => Promise<void>;
  fetchQuinielaDetalle: (id: number) => Promise<void>;
  crearQuiniela: (nombre: string, codigoInvitacion: string) => Promise<void>;
  unirseQuiniela: (codigoInvitacion: string) => Promise<void>;
  fetchLeaderboard: (quinielaId: number) => Promise<void>;

  fetchPartidos: () => Promise<void>;
  fetchMisPronosticos: (quinielaId: number) => Promise<void>;
  guardarPronosticosBatch: (idQuiniela: number, pronosticos: any[]) => Promise<void>;
}

export const useStore = create<AppState>((set, get) => ({
  usuario: null,
  token: null,
  isAuthenticated: false,
  loading: false,
  quinielas: [],
  quinielaDetalle: null,
  leaderboard: [],
  partidos: [],
  misPronosticos: [],

  login: async (email: string, password: string) => {
    set({ loading: true });
    try {
      const response = await authService.login({ email, password });
      localStorage.setItem('token', response.accessToken);
      set({
        token: response.accessToken,
        usuario: response.usuario,
        isAuthenticated: true,
        loading: false,
      });
    } catch (error) {
      set({ loading: false });
      throw error;
    }
  },

  register: async (nombre: string, email: string, password: string) => {
    set({ loading: true });
    try {
      await authService.register({ nombre, email, password });
      set({ loading: false });
    } catch (error) {
      set({ loading: false });
      throw error;
    }
  },

  logout: () => {
    localStorage.removeItem('token');
    window.location.href = '/';
  },

  fetchPerfil: async () => {
    try {
      const response = await authService.getPerfil();
      set({ usuario: response, isAuthenticated: true });
    } catch (error) {
      console.log('Error fetching perfil (silent):', error);
      // No lanzar error - la app puede continuar
    }
  },

  fetchQuinielas: async () => {
    const response = await quinielaService.getQuinielas();
    set({ quinielas: response });
  },

  fetchQuinielaDetalle: async (id: number) => {
    const response = await quinielaService.getQuinielaDetalle(id);
    set({ quinielaDetalle: response });
  },

  crearQuiniela: async (nombre: string, codigoInvitacion: string) => {
    await quinielaService.crearQuiniela({ nombre, codigoInvitacion });
    await get().fetchQuinielas();
  },

  unirseQuiniela: async (codigoInvitacion: string) => {
    await quinielaService.unirseQuiniela(codigoInvitacion);
    await get().fetchQuinielas();
  },

  fetchLeaderboard: async (quinielaId: number) => {
    const response = await quinielaService.getLeaderboard(quinielaId);
    set({ leaderboard: response });
  },

  fetchPartidos: async () => {
    const response = await partidoService.getPartidos();
    set({ partidos: response });
  },

  fetchMisPronosticos: async (quinielaId: number) => {
    const response = await pronosticoService.getMisPronosticos(quinielaId);
    set({ misPronosticos: response?.pronosticos ?? [] });
  },

  guardarPronosticosBatch: async (idQuiniela: number, pronosticos: any[]) => {
    await pronosticoService.guardarPronosticosBatch({ idQuiniela, pronosticos });
    await get().fetchMisPronosticos(idQuiniela);
  },
}));
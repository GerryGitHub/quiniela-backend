export interface Usuario {
  id: number;
  nombre: string;
  email: string;
  puntosTotales?: number;
}

export interface AuthResponse {
  token: string;
  tipo: string;
  usuario: Usuario;
}

export interface RegisterRequest {
  nombre: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface QuinielaResumen {
  id: number;
  nombre: string;
  codigoInvitacion: string;
  puntosTotales: number;
}

export interface Quiniela {
  id: number;
  nombre: string;
  codigoInvitacion: string;
  administrador: Usuario;
  participantes: Usuario[];
  esPublica?: boolean;
  partidos?: Partido[];
}

export interface QuinielaDetalle extends Quiniela {
  participantes: Usuario[];
  partidos: Partido[];
}

export interface LeaderboardEntry {
  posicion: number;
  usuario: Usuario;
  puntosTotales: number;
}

export interface Partido {
  id: number;
  equipoLocal: string;
  equipoVisitante: string;
  fechaHora: string;
  golesLocalReal: number | null;
  golesVisitanteReal: number | null;
  estado: string;
}

export interface Pronostico {
  id: number;
  usuario: Usuario;
  partido: Partido;
  golesLocalPredicho: number;
  golesVisitantePredicho: number;
  puntosObtenidos: number;
}

export interface PronosticoItem {
  idPartido: number;
  golesLocalPredicho: number;
  golesVisitantePredicho: number;
}

export interface CrearPronosticoBatchRequest {
  idQuiniela: number;
  pronosticos: PronosticoItem[];
}

export interface CrearQuinielaRequest {
  nombre: string;
  codigoInvitacion: string;
}

export interface UnirseQuinielaRequest {
  codigoInvitacion: string;
}

export interface UsuarioPerfil {
  id: number;
  nombre: string;
  email: string;
  puntosTotalesGlobales: number;
  quinielas: QuinielaResumen[];
}
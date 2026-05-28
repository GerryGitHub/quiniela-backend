import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useStore } from '../store/useStore';
import Spinner from '../components/Spinner';
import './Login.css';

export default function Register() {
  const navigate = useNavigate();
  const { register, loading } = useStore();
  const [nombre, setNombre] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const getErrorMessage = (err: any): string => {
    if (!err.response) return 'Error de conexión. Intenta más tarde.';
    const status = err.response.status;
    const data = err.response.data;
    if (status === 401 || status === 403) return 'Tu sesión expiró. Por favor inicia sesión.';
    if (status === 400) return data?.error || 'Solicitud inválida';
    if (status === 409) return data?.error || 'El email ya está registrado';
    if (status >= 500) return 'Error del servidor. Intenta más tarde.';
    return data?.error || 'Ocurrió un error';
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      await register(nombre, email, password);
      window.location.href = '/login?registered=true';
    } catch (err: any) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h1>Quiniela</h1>
        <h2>Crear Cuenta</h2>
        
        {error && <div className="error-message">{error}</div>}
        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Nombre</label>
            <input
              type="text"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              required
              placeholder="Tu nombre"
              disabled={loading}
            />
          </div>
          
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              placeholder="tu@email.com"
              disabled={loading}
            />
          </div>
          
          <div className="form-group">
            <label>Contraseña</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              placeholder="••••••••"
              disabled={loading}
            />
          </div>
          
          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? <Spinner /> : 'Crear Cuenta'}
          </button>
        </form>
        
        <p className="link-text">
          ¿Ya tienes cuenta? <Link to="/login">Inicia Sesión</Link>
        </p>
      </div>
    </div>
  );
}
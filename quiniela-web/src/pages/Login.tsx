import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useStore } from '../store/useStore';
import Spinner from '../components/Spinner';
import './Login.css';

export default function Login() {
  const navigate = useNavigate();
  const { login, loading } = useStore();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const [successMsg, setSuccessMsg] = useState('');

  const sessionExpired = localStorage.getItem('sessionExpired');
  if (sessionExpired) {
    localStorage.removeItem('sessionExpired');
    setTimeout(() => setError('Tu sesión expiró. Por favor inicia sesión nuevamente.'), 100);
  }

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    if (params.get('registered') === 'true') {
      setSuccessMsg('Cuenta creada correctamente. Revisa tu correo para verificar tu cuenta con el código OTP.');
    }
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      await login(email, password);
      window.location.href = '/dashboard';
    } catch (err: any) {
      setError(err.response?.data?.error || 'Error al iniciar sesión');
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h1>Quiniela</h1>
        <h2>Iniciar Sesión</h2>
        
        {successMsg && <div className="success-message">{successMsg}</div>}
        {error && <div className="error-message">{error}</div>}
        
        <form onSubmit={handleSubmit}>
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
            {loading ? <Spinner /> : 'Iniciar Sesión'}
          </button>
        </form>
        
        <p className="link-text">
          ¿No tienes cuenta? <Link to="/register">Regístrate</Link>
        </p>
      </div>
    </div>
  );
}
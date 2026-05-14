import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function AdminLoginPage() {
  const { user, login, logout } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  if (user) {
    const isAdmin = user.roles.includes('ADMIN') || user.roles.includes('BUSINESS_OWNER');
    if (isAdmin) return <Navigate to="/admin" replace />;
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, password);
      const token = localStorage.getItem('token')!;
      const payload = JSON.parse(atob(token.split('.')[1]));
      const roles: string[] = payload.roles ?? [];
      if (roles.includes('ADMIN') || roles.includes('BUSINESS_OWNER')) {
        navigate('/admin', { replace: true });
      } else {
        logout();
        setError('Acceso restringido. Esta página es solo para administradores.');
      }
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Error al iniciar sesión');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#f5f5f5] flex items-center justify-center">
      <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-8 w-full max-w-sm">
        <div className="mb-6 text-center">
          <span className="font-extrabold text-2xl text-[#1DA462]">Market</span>
          <h1 className="text-base font-semibold text-gray-700 mt-1">Panel de Administración</h1>
        </div>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input
              type="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              className="w-full border border-gray-300 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-[#1DA462] focus:border-transparent"
              required
              autoComplete="email"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Contraseña</label>
            <input
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              className="w-full border border-gray-300 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-[#1DA462] focus:border-transparent"
              required
              autoComplete="current-password"
            />
          </div>
          {error && <p className="text-red-600 text-sm">{error}</p>}
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-[#1DA462] hover:bg-[#178a52] text-white py-2.5 rounded-full text-sm font-semibold disabled:opacity-50 transition-colors"
          >
            {loading ? 'Iniciando sesión...' : 'Ingresar al panel'}
          </button>
        </form>
      </div>
    </div>
  );
}

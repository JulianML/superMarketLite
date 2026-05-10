import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <nav className="bg-gray-900 text-white px-6 py-3 flex items-center gap-6">
      <span className="font-bold text-lg text-indigo-400">Market</span>
      <NavLink
        to="/products"
        className={({ isActive }) =>
          `text-sm ${isActive ? 'text-white font-medium' : 'text-gray-400 hover:text-white'}`
        }
      >
        Productos
      </NavLink>
      <NavLink
        to="/inventory"
        className={({ isActive }) =>
          `text-sm ${isActive ? 'text-white font-medium' : 'text-gray-400 hover:text-white'}`
        }
      >
        Inventario
      </NavLink>
      <div className="ml-auto flex items-center gap-4">
        {user && (
          <>
            <span className="text-sm text-gray-300">{user.email}</span>
            <button
              onClick={handleLogout}
              className="text-sm text-gray-400 hover:text-white transition-colors"
            >
              Cerrar sesión
            </button>
          </>
        )}
      </div>
    </nav>
  );
}

import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const { itemCount } = useCart();
  const navigate = useNavigate();

  const isAdmin =
    user?.roles.includes('ADMIN') || user?.roles.includes('BUSINESS_OWNER');

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <nav className="bg-gray-900 text-white px-6 py-3 flex items-center gap-6">
      <span className="font-bold text-lg text-indigo-400">Market</span>
      <NavLink
        to="/catalog"
        className={({ isActive }) =>
          `text-sm ${isActive ? 'text-white font-medium' : 'text-gray-400 hover:text-white'}`
        }
      >
        Catálogo
      </NavLink>
      {isAdmin && (
        <>
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
        </>
      )}
      {user && (
        <NavLink
          to="/orders"
          className={({ isActive }) =>
            `text-sm ${isActive ? 'text-white font-medium' : 'text-gray-400 hover:text-white'}`
          }
        >
          Mis pedidos
        </NavLink>
      )}
      <NavLink
        to="/cart"
        className={({ isActive }) =>
          `relative flex items-center gap-1 text-sm ${isActive ? 'text-white font-medium' : 'text-gray-400 hover:text-white'}`
        }
      >
        <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth={1.5} viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 3h1.386c.51 0 .955.343 1.087.835l.383 1.437M7.5 14.25a3 3 0 00-3 3h15.75m-12.75-3h11.218c1.121-2.3 2.1-4.684 2.924-7.138a60.114 60.114 0 00-16.536-1.84M7.5 14.25L5.106 5.272M6 20.25a.75.75 0 11-1.5 0 .75.75 0 011.5 0zm12.75 0a.75.75 0 11-1.5 0 .75.75 0 011.5 0z" />
        </svg>
        {itemCount > 0 && (
          <span className="absolute -top-2 -right-2 bg-indigo-500 text-white text-xs font-bold rounded-full w-4 h-4 flex items-center justify-center">
            {itemCount > 99 ? '99+' : itemCount}
          </span>
        )}
      </NavLink>

      <div className="ml-auto flex items-center gap-4">
        {user ? (
          <>
            <span className="text-sm text-gray-300">{user.email}</span>
            <button
              onClick={handleLogout}
              className="text-sm text-gray-400 hover:text-white transition-colors"
            >
              Cerrar sesión
            </button>
          </>
        ) : (
          <NavLink
            to="/login"
            className="text-sm text-gray-400 hover:text-white transition-colors"
          >
            Iniciar sesión
          </NavLink>
        )}
      </div>
    </nav>
  );
}

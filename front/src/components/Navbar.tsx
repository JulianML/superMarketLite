import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const { itemCount } = useCart();
  const navigate = useNavigate();

  const isAdmin = user?.roles.includes('ADMIN') || user?.roles.includes('BUSINESS_OWNER');

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const navLink = ({ isActive }: { isActive: boolean }) =>
    `text-sm px-3 py-2 rounded-lg font-medium transition-colors ${
      isActive
        ? 'text-[#1DA462] bg-[#f0faf4]'
        : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
    }`;

  return (
    <nav className="sticky top-0 z-50 bg-white border-b border-gray-200 shadow-sm">
      <div className="max-w-7xl mx-auto px-6 flex items-center h-16 gap-6">
        <NavLink to="/catalog" className="flex-shrink-0">
          <span className="font-extrabold text-xl text-[#1DA462] tracking-tight">Market</span>
        </NavLink>

        <div className="flex items-center gap-1">
          <NavLink to="/catalog" className={navLink}>Productos</NavLink>
          {isAdmin && (
            <>
              <NavLink to="/products" className={navLink}>Gestión</NavLink>
              <NavLink to="/inventory" className={navLink}>Inventario</NavLink>
            </>
          )}
          {user && <NavLink to="/orders" className={navLink}>Mis pedidos</NavLink>}
        </div>

        <div className="ml-auto flex items-center gap-3">
          {user ? (
            <>
              <span className="text-sm text-gray-500 hidden md:block max-w-[180px] truncate">
                {user.email}
              </span>
              <button
                onClick={handleLogout}
                className="text-sm text-gray-500 hover:text-gray-800 transition-colors"
              >
                Salir
              </button>
            </>
          ) : (
            <NavLink
              to="/login"
              className="text-sm font-semibold text-[#1DA462] hover:text-[#178a52] transition-colors"
            >
              Iniciar sesión
            </NavLink>
          )}

          <NavLink
            to="/cart"
            className="flex items-center gap-2 bg-[#1DA462] hover:bg-[#178a52] text-white text-sm font-semibold px-4 py-2.5 rounded-full transition-colors"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 3h1.386c.51 0 .955.343 1.087.835l.383 1.437M7.5 14.25a3 3 0 00-3 3h15.75m-12.75-3h11.218c1.121-2.3 2.1-4.684 2.924-7.138a60.114 60.114 0 00-16.536-1.84M7.5 14.25L5.106 5.272M6 20.25a.75.75 0 11-1.5 0 .75.75 0 011.5 0zm12.75 0a.75.75 0 11-1.5 0 .75.75 0 011.5 0z" />
            </svg>
            <span>Cesta</span>
            {itemCount > 0 && (
              <span className="bg-white text-[#1DA462] text-xs font-extrabold rounded-full w-5 h-5 flex items-center justify-center leading-none">
                {itemCount > 99 ? '99+' : itemCount}
              </span>
            )}
          </NavLink>
        </div>
      </div>
    </nav>
  );
}

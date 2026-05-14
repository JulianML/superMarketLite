import { NavLink, Outlet, useNavigate, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function AdminLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  if (!user) return <Navigate to="/admin/login" replace />;
  const isAdmin = user.roles.includes('ADMIN') || user.roles.includes('BUSINESS_OWNER');
  if (!isAdmin) return <Navigate to="/catalog" replace />;

  function handleLogout() {
    logout();
    navigate('/admin/login', { replace: true });
  }

  return (
    <div className="flex min-h-screen bg-[#f5f5f5]">
      <aside className="w-56 bg-white border-r border-gray-200 flex flex-col shrink-0">
        <div className="px-5 py-4 border-b border-gray-200">
          <span className="font-extrabold text-xl text-[#1DA462]">Market</span>
          <p className="text-xs text-gray-500 mt-0.5">Panel Admin</p>
        </div>

        <nav className="flex-1 px-3 py-4 space-y-1">
          <NavLink
            to="/admin/products"
            className={({ isActive }) =>
              `flex items-center px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-[#1DA462]/10 text-[#1DA462]'
                  : 'text-gray-600 hover:bg-gray-100'
              }`
            }
          >
            Productos
          </NavLink>
          <NavLink
            to="/admin/inventory"
            end
            className={({ isActive }) =>
              `flex items-center px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-[#1DA462]/10 text-[#1DA462]'
                  : 'text-gray-600 hover:bg-gray-100'
              }`
            }
          >
            Inventario
          </NavLink>
        </nav>

        <div className="px-3 py-4 border-t border-gray-200 space-y-1">
          <p className="text-xs text-gray-400 px-3 truncate">{user.email}</p>
          <button
            onClick={handleLogout}
            className="w-full text-left px-3 py-2 rounded-lg text-sm text-gray-600 hover:bg-gray-100 font-medium"
          >
            Cerrar sesión
          </button>
        </div>
      </aside>

      <main className="flex-1 px-6 py-6 overflow-auto">
        <Outlet />
      </main>
    </div>
  );
}

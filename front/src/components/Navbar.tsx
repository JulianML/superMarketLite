import { NavLink } from 'react-router-dom';

export default function Navbar() {
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
      <div className="ml-auto flex items-center gap-2">
        <span className="bg-amber-500 text-amber-900 text-xs font-bold px-2 py-0.5 rounded">
          DEMO
        </span>
        <span className="text-sm text-gray-300">Usuario de prueba</span>
      </div>
    </nav>
  );
}

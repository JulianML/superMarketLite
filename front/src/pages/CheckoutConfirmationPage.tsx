import { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCheckout } from '../context/CheckoutContext';

export default function CheckoutConfirmationPage() {
  const navigate = useNavigate();
  const { confirmedOrder, reset } = useCheckout();

  useEffect(() => {
    if (!confirmedOrder) navigate('/cart', { replace: true });
  }, [confirmedOrder, navigate]);

  if (!confirmedOrder) return null;

  return (
    <div className="max-w-lg mx-auto text-center">
      <div className="mb-6">
        <p className="text-xs text-gray-400 uppercase tracking-wide mb-1">Paso 3 de 3</p>
        <h1 className="text-2xl font-bold text-gray-800">Pedido confirmado</h1>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8">
        <div className="w-14 h-14 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-5">
          <svg className="w-7 h-7 text-green-600" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 12.75l6 6 9-13.5" />
          </svg>
        </div>

        <p className="text-gray-500 text-sm mb-1">Número de pedido</p>
        <p className="text-xl font-bold text-indigo-600 font-mono mb-4">{confirmedOrder.orderNumber}</p>

        <p className="text-sm text-gray-600 mb-6">
          Tu pedido está siendo preparado. Puedes seguir el estado en tu historial de pedidos.
        </p>

        <div className="flex flex-col gap-3">
          <Link
            to="/orders"
            onClick={reset}
            className="w-full bg-indigo-600 text-white text-sm font-medium px-6 py-2.5 rounded-lg hover:bg-indigo-700 transition-colors"
          >
            Ver mis pedidos
          </Link>
          <Link
            to="/catalog"
            onClick={reset}
            className="w-full text-sm text-gray-500 hover:text-gray-700 transition-colors"
          >
            Seguir comprando
          </Link>
        </div>
      </div>
    </div>
  );
}

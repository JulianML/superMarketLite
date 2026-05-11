import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { productsApi, type Product } from '../api/products';

function ImagePlaceholder() {
  return (
    <div className="w-full aspect-[4/3] bg-gray-100 rounded-xl flex items-center justify-center">
      <svg
        className="w-16 h-16 text-gray-300"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={1.5}
          d="M4 16l4-4a3 3 0 014.24 0L16 16m-2-2l1.59-1.59A3 3 0 0119 12.24V16M4 8h.01M4 6a2 2 0 012-2h12a2 2 0 012 2v12a2 2 0 01-2 2H6a2 2 0 01-2-2V6z"
        />
      </svg>
    </div>
  );
}

export default function ProductDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    setError(null);
    productsApi.get(Number(id))
      .then(setProduct)
      .catch(e => setError(String(e)))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto animate-pulse">
        <div className="h-4 bg-gray-200 rounded w-32 mb-6" />
        <div className="flex flex-col md:flex-row gap-8">
          <div className="w-full md:w-1/2 aspect-[4/3] bg-gray-200 rounded-xl" />
          <div className="flex-1 space-y-4 pt-2">
            <div className="h-7 bg-gray-200 rounded w-3/4" />
            <div className="h-4 bg-gray-100 rounded w-1/4" />
            <div className="h-4 bg-gray-100 rounded w-full" />
            <div className="h-4 bg-gray-100 rounded w-5/6" />
            <div className="h-8 bg-gray-200 rounded w-1/3 mt-4" />
          </div>
        </div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="max-w-4xl mx-auto">
        <button
          onClick={() => navigate('/catalog')}
          className="text-sm text-indigo-600 hover:text-indigo-800 mb-6 inline-flex items-center gap-1"
        >
          ← Volver al catálogo
        </button>
        <div className="bg-red-50 text-red-700 p-4 rounded-lg text-sm">
          {error ?? 'Producto no encontrado.'}
        </div>
      </div>
    );
  }

  const priceWithVat =
    product.vatRate != null
      ? Number(product.price) * (1 + product.vatRate / 100)
      : null;

  return (
    <div className="max-w-4xl mx-auto">
      <button
        onClick={() => navigate(-1)}
        className="text-sm text-indigo-600 hover:text-indigo-800 mb-6 inline-flex items-center gap-1"
      >
        ← Volver al catálogo
      </button>

      <div className="flex flex-col md:flex-row gap-8 bg-white rounded-xl shadow-sm border border-gray-100 p-6">
        <div className="w-full md:w-5/12 flex-shrink-0">
          <ImagePlaceholder />
        </div>

        <div className="flex-1 flex flex-col gap-4">
          <div>
            <p className="text-xs text-gray-400 font-mono mb-1">SKU: {product.sku}</p>
            <h1 className="text-2xl font-bold text-gray-800 leading-tight">{product.name}</h1>
          </div>

          {product.description && (
            <p className="text-sm text-gray-600 leading-relaxed">{product.description}</p>
          )}

          <div className="border-t border-gray-100 pt-4 space-y-2">
            <div className="flex items-baseline gap-2">
              <span className="text-3xl font-bold text-indigo-600">
                {Number(product.price).toFixed(2)}
              </span>
              <span className="text-base text-gray-500">{product.currency}</span>
            </div>

            {priceWithVat != null && (
              <p className="text-xs text-gray-500">
                {priceWithVat.toFixed(2)} {product.currency} con IVA ({product.vatRate}%)
              </p>
            )}
          </div>

          <div className="flex flex-wrap gap-2 mt-auto pt-2">
            <span
              className={`px-3 py-1 rounded-full text-xs font-medium ${
                product.isActive
                  ? 'bg-green-100 text-green-700'
                  : 'bg-gray-100 text-gray-500'
              }`}
            >
              {product.isActive ? 'Disponible' : 'No disponible'}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}

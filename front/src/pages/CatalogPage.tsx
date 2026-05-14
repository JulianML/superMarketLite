import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { productsApi, type ProductSummary } from '../api/products';
import { useCart } from '../context/CartContext';

function ProductCard({ product }: { product: ProductSummary }) {
  const navigate = useNavigate();
  const { addItem } = useCart();
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);

  const handleAddToCart = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (adding) return;
    setAdding(true);
    try {
      await addItem(product.id, 1);
      setAdded(true);
      setTimeout(() => setAdded(false), 1500);
    } catch {
      // error surfaced via CartContext
    } finally {
      setAdding(false);
    }
  };

  return (
    <div
      onClick={() => navigate(`/catalog/${product.id}`)}
      className="bg-white rounded-2xl border border-gray-200 overflow-hidden cursor-pointer hover:shadow-md transition-shadow duration-200 flex flex-col"
    >
      <div className="aspect-square bg-gray-50 overflow-hidden">
        {product.imageUrl ? (
          <img
            src={product.imageUrl}
            alt={product.name}
            className="w-full h-full object-cover"
            onError={e => {
              (e.currentTarget as HTMLImageElement).style.display = 'none';
              e.currentTarget.nextElementSibling?.classList.remove('hidden');
            }}
          />
        ) : null}
        <div className={`w-full h-full flex items-center justify-center p-6 ${product.imageUrl ? 'hidden' : ''}`}>
          <svg
            className="w-14 h-14 text-gray-300"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1}
              d="M4 16l4-4a3 3 0 014.24 0L16 16m-2-2l1.59-1.59A3 3 0 0119 12.24V16M4 8h.01M4 6a2 2 0 012-2h12a2 2 0 012 2v12a2 2 0 01-2 2H6a2 2 0 01-2-2V6z"
            />
          </svg>
        </div>
      </div>

      <div className="p-3 flex flex-col flex-1">
        <h2 className="text-sm text-gray-800 font-medium leading-snug mb-2 line-clamp-2 flex-1 min-h-[2.5rem]">
          {product.name}
        </h2>
        <div className="mt-1">
          <p className="text-lg font-bold text-gray-900">
            {Number(product.price).toFixed(2)}{' '}
            <span className="text-sm font-normal text-gray-500">{product.currency}</span>
          </p>
          <button
            onClick={handleAddToCart}
            disabled={adding}
            className={`mt-2 w-full py-2 rounded-xl text-sm font-semibold transition-colors disabled:opacity-50 ${
              added
                ? 'bg-gray-100 text-gray-600'
                : 'bg-[#1DA462] hover:bg-[#178a52] text-white'
            }`}
          >
            {added ? '✓ Añadido' : adding ? '...' : '+ Añadir'}
          </button>
        </div>
      </div>
    </div>
  );
}

export default function CatalogPage() {
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await productsApi.list(query || undefined, page);
      setProducts(res.content.filter(p => p.isActive));
      setTotalPages(res.totalPages);
    } catch (e) {
      setError(String(e));
    } finally {
      setLoading(false);
    }
  }, [query, page]);

  useEffect(() => { load(); }, [load]);

  function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    setQuery(search);
    setPage(0);
  }

  return (
    <div>
      <form onSubmit={handleSearch} className="flex gap-2 mb-6">
        <div className="relative flex-1 max-w-md">
          <svg
            className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
            fill="none"
            stroke="currentColor"
            strokeWidth={2}
            viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-4.35-4.35M17 11A6 6 0 115 11a6 6 0 0112 0z" />
          </svg>
          <input
            type="text"
            placeholder="Buscar productos..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full border border-gray-300 rounded-full pl-9 pr-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-[#1DA462] focus:border-transparent"
          />
        </div>
        <button
          type="submit"
          className="bg-[#1DA462] hover:bg-[#178a52] text-white rounded-full px-5 py-2.5 text-sm font-semibold transition-colors"
        >
          Buscar
        </button>
        {query && (
          <button
            type="button"
            onClick={() => { setSearch(''); setQuery(''); setPage(0); }}
            className="text-sm text-gray-500 hover:text-gray-700 px-2"
          >
            Limpiar
          </button>
        )}
      </form>

      {error && (
        <div className="bg-red-50 text-red-700 p-3 rounded-xl mb-4 text-sm">{error}</div>
      )}

      {loading ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
          {Array.from({ length: 10 }).map((_, i) => (
            <div key={i} className="bg-white rounded-2xl border border-gray-200 overflow-hidden animate-pulse">
              <div className="aspect-square bg-gray-100" />
              <div className="p-3 space-y-2">
                <div className="h-3.5 bg-gray-100 rounded-full w-4/5" />
                <div className="h-3.5 bg-gray-100 rounded-full w-3/5" />
                <div className="h-5 bg-gray-200 rounded-full w-1/3 mt-1" />
                <div className="h-8 bg-gray-100 rounded-xl mt-2" />
              </div>
            </div>
          ))}
        </div>
      ) : products.length === 0 ? (
        <div className="text-center py-20 text-gray-400">
          <svg className="w-12 h-12 mx-auto mb-3 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <p className="text-sm">No se encontraron productos</p>
        </div>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
          {products.map(p => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex gap-2 mt-10 justify-center items-center">
          <button
            onClick={() => setPage(p => p - 1)}
            disabled={page === 0}
            className="px-4 py-2 border border-gray-300 rounded-full text-sm disabled:opacity-40 hover:bg-gray-50 transition-colors"
          >
            ← Anterior
          </button>
          <span className="px-3 py-2 text-sm text-gray-600">
            {page + 1} / {totalPages}
          </span>
          <button
            onClick={() => setPage(p => p + 1)}
            disabled={page >= totalPages - 1}
            className="px-4 py-2 border border-gray-300 rounded-full text-sm disabled:opacity-40 hover:bg-gray-50 transition-colors"
          >
            Siguiente →
          </button>
        </div>
      )}
    </div>
  );
}

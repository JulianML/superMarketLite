import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { productsApi, type ProductSummary } from '../api/products';
import { useCart } from '../context/CartContext';

function ImagePlaceholder() {
  return (
    <div className="w-full aspect-[4/3] bg-gray-100 flex items-center justify-center">
      <svg
        className="w-12 h-12 text-gray-300"
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
      className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden cursor-pointer hover:shadow-md hover:-translate-y-0.5 transition-all duration-200 flex flex-col"
    >
      <ImagePlaceholder />
      <div className="p-4 flex flex-col flex-1">
        <p className="text-xs text-gray-400 font-mono mb-1">{product.sku}</p>
        <h2 className="font-semibold text-gray-800 text-sm leading-snug mb-1 line-clamp-2">
          {product.name}
        </h2>
        <div className="mt-auto pt-3 flex items-center justify-between">
          <span className="text-indigo-600 font-bold text-base">
            {Number(product.price).toFixed(2)}{' '}
            <span className="text-xs font-normal text-gray-500">{product.currency}</span>
          </span>
          <button
            onClick={handleAddToCart}
            disabled={adding}
            className={`text-xs font-medium px-2 py-1 rounded-lg transition-colors ${
              added
                ? 'bg-green-100 text-green-700'
                : 'bg-indigo-50 text-indigo-600 hover:bg-indigo-100'
            } disabled:opacity-50`}
          >
            {added ? 'Agregado ✓' : adding ? '...' : '+ Carrito'}
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
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-800 mb-1">Catálogo</h1>
        <p className="text-sm text-gray-500">Explorá todos nuestros productos disponibles</p>
      </div>

      <form onSubmit={handleSearch} className="flex gap-2 mb-6">
        <input
          type="text"
          placeholder="Buscar productos..."
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm flex-1 max-w-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
        <button
          type="submit"
          className="bg-indigo-600 text-white rounded-lg px-4 py-2 text-sm font-medium hover:bg-indigo-700"
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
        <div className="bg-red-50 text-red-700 p-3 rounded-lg mb-4 text-sm">{error}</div>
      )}

      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i} className="bg-white rounded-xl border border-gray-100 overflow-hidden animate-pulse">
              <div className="w-full aspect-[4/3] bg-gray-200" />
              <div className="p-4 space-y-2">
                <div className="h-3 bg-gray-200 rounded w-1/3" />
                <div className="h-4 bg-gray-200 rounded w-3/4" />
                <div className="h-4 bg-gray-100 rounded w-1/2 mt-3" />
              </div>
            </div>
          ))}
        </div>
      ) : products.length === 0 ? (
        <div className="text-center py-16 text-gray-400">
          <svg className="w-12 h-12 mx-auto mb-3 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <p className="text-sm">No se encontraron productos</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {products.map(p => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex gap-2 mt-8 justify-center items-center">
          <button
            onClick={() => setPage(p => p - 1)}
            disabled={page === 0}
            className="px-4 py-2 border border-gray-300 rounded-lg text-sm disabled:opacity-40 hover:bg-gray-50"
          >
            ← Anterior
          </button>
          <span className="px-3 py-2 text-sm text-gray-600">
            {page + 1} / {totalPages}
          </span>
          <button
            onClick={() => setPage(p => p + 1)}
            disabled={page >= totalPages - 1}
            className="px-4 py-2 border border-gray-300 rounded-lg text-sm disabled:opacity-40 hover:bg-gray-50"
          >
            Siguiente →
          </button>
        </div>
      )}
    </div>
  );
}

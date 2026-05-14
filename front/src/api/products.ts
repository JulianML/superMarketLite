import { apiRequest, BUSINESS_ID } from './client';

export interface ProductSummary {
  id: number;
  businessId: number;
  sku: string;
  name: string;
  price: number;
  currency: string;
  isActive: boolean;
  imageUrl?: string;
}

export interface Product extends ProductSummary {
  description: string;
  vatRate: number;
  createdAt: string;
  updatedAt: string;
  deletedAt: string | null;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface ProductCreateDTO {
  businessId: number;
  sku: string;
  name: string;
  description?: string;
  price: number;
  currency: string;
  vatRate?: number;
}

export interface ProductUpdateDTO {
  sku: string;
  name: string;
  description?: string;
  price: number;
  currency: string;
  vatRate?: number;
  isActive?: boolean;
}

export const productsApi = {
  list: (q?: string, page = 0, size = 20) => {
    const params = new URLSearchParams({
      businessId: String(BUSINESS_ID),
      page: String(page),
      size: String(size),
    });
    if (q) params.set('q', q);
    return apiRequest<Page<ProductSummary>>(`/api/v1/products?${params}`);
  },

  get: (id: number) =>
    apiRequest<Product>(`/api/v1/products/${id}?businessId=${BUSINESS_ID}`),

  create: (data: ProductCreateDTO) =>
    apiRequest<Product>('/api/v1/products', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  update: (id: number, data: ProductUpdateDTO) =>
    apiRequest<Product>(`/api/v1/products/${id}?businessId=${BUSINESS_ID}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  delete: (id: number) =>
    apiRequest<void>(`/api/v1/products/${id}?businessId=${BUSINESS_ID}`, {
      method: 'DELETE',
    }),
};

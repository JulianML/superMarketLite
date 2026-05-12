import { apiRequest, BUSINESS_ID } from './client';

export interface CartItemDTO {
  productId: number;
  productName: string;
  sku: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
}

export interface CartDTO {
  businessId: number;
  items: CartItemDTO[];
  total: number;
}

export const cartApi = {
  get: () =>
    apiRequest<CartDTO>(`/api/cart/${BUSINESS_ID}`),

  addItem: (productId: number, quantity: number) =>
    apiRequest<CartDTO>(`/api/cart/${BUSINESS_ID}/items`, {
      method: 'POST',
      body: JSON.stringify({ productId, quantity }),
    }),

  updateItem: (productId: number, quantity: number) =>
    apiRequest<CartDTO>(`/api/cart/${BUSINESS_ID}/items/${productId}`, {
      method: 'PUT',
      body: JSON.stringify({ quantity }),
    }),

  removeItem: (productId: number) =>
    apiRequest<CartDTO>(`/api/cart/${BUSINESS_ID}/items/${productId}`, {
      method: 'DELETE',
    }),

  clear: () =>
    apiRequest<void>(`/api/cart/${BUSINESS_ID}`, {
      method: 'DELETE',
    }),
};

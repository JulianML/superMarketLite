import { apiRequest } from './client';

export interface AddressDTO {
  street: string;
  city: string;
  postalCode: string;
  country: string;
}

export interface OrderItemDTO {
  productId: number;
  productName: string;
  sku: string;
  quantity: number;
  unitPrice: number;
  vatRate: number | null;
  lineTotal: number;
}

export interface StatusHistoryDTO {
  fromStatus: string | null;
  toStatus: string;
  changedAt: string;
  note: string | null;
}

export interface OrderSummaryDTO {
  id: number;
  orderNumber: string;
  status: string;
  subtotal: number;
  taxTotal: number;
  shippingFee: number;
  total: number;
  currency: string;
  placedAt: string;
}

export interface OrderDetailDTO extends OrderSummaryDTO {
  deliveryAddress: AddressDTO;
  items: OrderItemDTO[];
  history: StatusHistoryDTO[];
}

export const ordersApi = {
  checkout: (address: AddressDTO): Promise<OrderSummaryDTO> =>
    apiRequest<OrderSummaryDTO>('/api/orders/checkout', {
      method: 'POST',
      body: JSON.stringify({ address }),
    }),

  list: (): Promise<OrderSummaryDTO[]> =>
    apiRequest<OrderSummaryDTO[]>('/api/orders'),

  get: (id: number): Promise<OrderDetailDTO> =>
    apiRequest<OrderDetailDTO>(`/api/orders/${id}`),
};

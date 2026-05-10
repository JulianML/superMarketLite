import { apiRequest, BUSINESS_ID } from './client';

export interface InventoryDTO {
  businessId: number;
  productId: number;
  stock: number;
  safetyStock: number;
  updatedAt: string;
}

export interface InventoryMovementDTO {
  id: number;
  businessId: number;
  productId: number;
  quantityChange: number;
  reason: string;
  referenceId: number | null;
  createdAt: string;
}

export interface InventorySetDTO {
  stock: number;
  safetyStock?: number;
}

export interface InventoryAdjustDTO {
  delta: number;
  reason: string;
  referenceId?: number | null;
}

export const inventoryApi = {
  list: () =>
    apiRequest<InventoryDTO[]>(`/api/v1/inventory?businessId=${BUSINESS_ID}`),

  belowSafety: () =>
    apiRequest<InventoryDTO[]>(`/api/v1/inventory/below-safety?businessId=${BUSINESS_ID}`),

  get: (productId: number) =>
    apiRequest<InventoryDTO>(`/api/v1/inventory/${productId}?businessId=${BUSINESS_ID}`),

  set: (productId: number, data: InventorySetDTO) =>
    apiRequest<InventoryDTO>(`/api/v1/inventory/${productId}?businessId=${BUSINESS_ID}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  adjust: (productId: number, data: InventoryAdjustDTO) =>
    apiRequest<InventoryDTO>(`/api/v1/inventory/${productId}/adjust?businessId=${BUSINESS_ID}`, {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  movements: (productId: number) =>
    apiRequest<InventoryMovementDTO[]>(
      `/api/v1/inventory/${productId}/movements?businessId=${BUSINESS_ID}`
    ),
};

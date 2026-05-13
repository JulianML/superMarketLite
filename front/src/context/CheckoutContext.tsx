import { createContext, useContext, useState, type ReactNode } from 'react';
import type { AddressDTO, OrderSummaryDTO } from '../api/orders';

interface CheckoutContextType {
  address: AddressDTO | null;
  confirmedOrder: OrderSummaryDTO | null;
  setAddress: (address: AddressDTO) => void;
  setConfirmedOrder: (order: OrderSummaryDTO) => void;
  reset: () => void;
}

const CheckoutContext = createContext<CheckoutContextType | null>(null);

export function CheckoutProvider({ children }: { children: ReactNode }) {
  const [address, setAddress] = useState<AddressDTO | null>(null);
  const [confirmedOrder, setConfirmedOrder] = useState<OrderSummaryDTO | null>(null);

  const reset = () => {
    setAddress(null);
    setConfirmedOrder(null);
  };

  return (
    <CheckoutContext.Provider value={{ address, confirmedOrder, setAddress, setConfirmedOrder, reset }}>
      {children}
    </CheckoutContext.Provider>
  );
}

export function useCheckout() {
  const ctx = useContext(CheckoutContext);
  if (!ctx) throw new Error('useCheckout must be used inside CheckoutProvider');
  return ctx;
}

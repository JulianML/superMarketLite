import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useCheckout } from '../context/CheckoutContext';

const schema = z.object({
  street:     z.string().min(1, 'Requerido'),
  city:       z.string().min(1, 'Requerido'),
  postalCode: z.string().min(1, 'Requerido'),
  country:    z.string().min(1, 'Requerido'),
});

type FormValues = z.infer<typeof schema>;

export default function CheckoutAddressPage() {
  const navigate = useNavigate();
  const { cart } = useCart();
  const { setAddress } = useCheckout();

  const { register, handleSubmit, formState: { errors } } = useForm<FormValues>({
    resolver: zodResolver(schema),
  });

  if (!cart || cart.items.length === 0) {
    navigate('/cart', { replace: true });
    return null;
  }

  const onSubmit = (data: FormValues) => {
    setAddress(data);
    navigate('/checkout/summary');
  };

  return (
    <div className="max-w-lg mx-auto">
      <div className="mb-6">
        <p className="text-xs text-gray-400 uppercase tracking-wide mb-1">Paso 1 de 3</p>
        <h1 className="text-2xl font-bold text-gray-900">Dirección de entrega</h1>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6 space-y-5">
        <Field label="Calle y número" error={errors.street?.message}>
          <input {...register('street')} placeholder="Av. Example 123" className={input(!!errors.street)} />
        </Field>
        <Field label="Ciudad" error={errors.city?.message}>
          <input {...register('city')} placeholder="Madrid" className={input(!!errors.city)} />
        </Field>
        <div className="grid grid-cols-2 gap-4">
          <Field label="Código postal" error={errors.postalCode?.message}>
            <input {...register('postalCode')} placeholder="28001" className={input(!!errors.postalCode)} />
          </Field>
          <Field label="País" error={errors.country?.message}>
            <input {...register('country')} placeholder="España" className={input(!!errors.country)} />
          </Field>
        </div>

        <div className="pt-2 flex justify-end">
          <button
            type="submit"
            className="bg-[#1DA462] hover:bg-[#178a52] text-white text-sm font-semibold px-6 py-2.5 rounded-full transition-colors"
          >
            Continuar al resumen
          </button>
        </div>
      </form>
    </div>
  );
}

function Field({ label, error, children }: { label: string; error?: string; children: React.ReactNode }) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
      {children}
      {error && <p className="text-xs text-red-500 mt-1">{error}</p>}
    </div>
  );
}

function input(hasError: boolean) {
  return `w-full border rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-[#1DA462] focus:border-transparent ${
    hasError ? 'border-red-400' : 'border-gray-300'
  }`;
}

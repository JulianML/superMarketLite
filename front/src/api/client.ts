export const BUSINESS_ID = 1;

const MOCK_TOKEN = 'mock-jwt-placeholder';

export async function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const res = await fetch(path, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${MOCK_TOKEN}`,
      ...(options.headers ?? {}),
    },
  });

  if (res.status === 204) return undefined as T;
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`${res.status}: ${text || res.statusText}`);
  }
  return res.json();
}

import { loadSession } from './session';

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

interface RequestOptions {
  method?: HttpMethod;
  body?: unknown;
  signal?: AbortSignal;
}

interface ErrorBody {
  detail?: string;
  message?: string;
  errors?: string[];
}

export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
    readonly errors: string[] = [],
  ) {
    super(message);
  }

  get conflict(): boolean {
    return this.status === 409;
  }

  get missing(): boolean {
    return this.status === 404 || this.status === 0 || this.status >= 500;
  }
}

let onUnauthorized: () => void = () => undefined;

export function setUnauthorizedHandler(handler: () => void): void {
  onUnauthorized = handler;
}

export function authHeader(): Record<string, string> {
  const token = loadSession()?.token;
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export function notifyUnauthorized(): void {
  onUnauthorized();
}

function parse(text: string): unknown {
  try {
    return text ? JSON.parse(text) : null;
  } catch {
    return null;
  }
}

export function isAbort(err: unknown): boolean {
  return err instanceof DOMException && err.name === 'AbortError';
}

export async function request<T>(path: string, { method = 'GET', body, signal }: RequestOptions = {}): Promise<T> {
  const headers: Record<string, string> = authHeader();
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  let response: Response;
  try {
    response = await fetch(path, {
      method,
      headers,
      signal,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch (err) {
    if (isAbort(err)) throw err;
    throw new ApiError(0, 'Cannot reach the server. Check that the services are running.');
  }

  const data = parse(await response.text());
  if (response.ok) return data as T;

  if (response.status === 401 && headers.Authorization) onUnauthorized();
  if (response.status >= 500) throw new ApiError(response.status, 'The server had a problem. Please try again.');

  const error = (data ?? {}) as ErrorBody;
  throw new ApiError(
    response.status,
    error.detail || error.message || 'Something went wrong. Please try again.',
    error.errors ?? [],
  );
}

export const query = (params: Record<string, string | number | undefined | null>) => {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, String(value));
  });
  const text = search.toString();
  return text ? `?${text}` : '';
};

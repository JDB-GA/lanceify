import { loadSession } from './session';

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE';

interface RequestOptions {
  method?: HttpMethod;
  body?: unknown;
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
}

let onUnauthorized: () => void = () => undefined;

export function setUnauthorizedHandler(handler: () => void): void {
  onUnauthorized = handler;
}

function parse(text: string): unknown {
  try {
    return text ? JSON.parse(text) : null;
  } catch {
    return null;
  }
}

export async function request<T>(path: string, { method = 'GET', body }: RequestOptions = {}): Promise<T> {
  const token = loadSession()?.token;
  const headers: Record<string, string> = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (token) headers.Authorization = `Bearer ${token}`;

  let response: Response;
  try {
    response = await fetch(path, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(0, 'Cannot reach the server. Check that the services are running.');
  }

  const data = parse(await response.text());
  if (response.ok) return data as T;

  if (response.status === 401 && token) onUnauthorized();
  if (response.status >= 500) throw new ApiError(response.status, 'The server had a problem. Please try again.');

  const error = (data ?? {}) as ErrorBody;
  throw new ApiError(
    response.status,
    error.detail || error.message || 'Something went wrong. Please try again.',
    error.errors ?? [],
  );
}

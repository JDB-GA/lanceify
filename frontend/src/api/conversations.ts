import type { ChatMessage, Conversation, ReadReceipt } from '../types';
import { authHeader, notifyUnauthorized, request } from './client';

const BASE = '/api/conversations';

export const conversationsApi = {
  list: (signal?: AbortSignal) => request<Conversation[]>(BASE, { signal }),
  get: (id: string, signal?: AbortSignal) => request<Conversation>(`${BASE}/${id}`, { signal }),
  start: (recipientId: string, projectId?: string | null) =>
    request<Conversation>(BASE, { method: 'POST', body: { recipientId, projectId: projectId ?? null } }),
  messages: (id: string, signal?: AbortSignal) => request<ChatMessage[]>(`${BASE}/${id}/messages`, { signal }),
  send: (id: string, content: string, clientMessageId: string) =>
    request<ChatMessage>(`${BASE}/${id}/messages`, { method: 'POST', body: { content, clientMessageId } }),
  markRead: (id: string) => request<void>(`${BASE}/${id}/read`, { method: 'PATCH' }),
  unread: (signal?: AbortSignal) => request<{ count: number }>(`${BASE}/unread`, { signal }),
};

export type StreamEvent =
  | { type: 'NEW_MESSAGE'; data: ChatMessage }
  | { type: 'READ'; data: ReadReceipt }
  | { type: 'OPEN' };

export async function openInboxStream(onEvent: (event: StreamEvent) => void, signal: AbortSignal): Promise<void> {
  const response = await fetch(`${BASE}/stream`, {
    headers: { ...authHeader(), Accept: 'text/event-stream' },
    signal,
  });
  if (response.status === 401) {
    notifyUnauthorized();
    return;
  }
  if (!response.ok || !response.body) throw new Error(`Stream failed with ${response.status}`);

  onEvent({ type: 'OPEN' });
  const reader = response.body.pipeThrough(new TextDecoderStream()).getReader();
  let buffer = '';
  for (;;) {
    const { value, done } = await reader.read();
    if (done) return;
    buffer += value.replace(/\r\n/g, '\n');
    let boundary = buffer.indexOf('\n\n');
    while (boundary >= 0) {
      dispatch(buffer.slice(0, boundary), onEvent);
      buffer = buffer.slice(boundary + 2);
      boundary = buffer.indexOf('\n\n');
    }
  }
}

function dispatch(block: string, onEvent: (event: StreamEvent) => void) {
  let name = 'message';
  const data: string[] = [];
  block.split('\n').forEach((line) => {
    if (line.startsWith('event:')) name = line.slice(6).trim();
    else if (line.startsWith('data:')) data.push(line.slice(5).replace(/^ /, ''));
  });
  if (data.length === 0 || (name !== 'NEW_MESSAGE' && name !== 'READ')) return;
  try {
    onEvent({ type: name, data: JSON.parse(data.join('\n')) } as StreamEvent);
  } catch {
    return;
  }
}

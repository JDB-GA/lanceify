import type { ChatMessage, Conversation } from '../types';
import { request } from './client';
import { loadSession } from './session';

const BASE = '/api/conversations';

export const chatApi = {
  list: () => request<Conversation[]>(BASE),
  start: (recipientId: string) => request<Conversation>(BASE, { method: 'POST', body: { recipientId } }),
  messages: (id: string) => request<ChatMessage[]>(`${BASE}/${id}/messages`),
  send: (id: string, content: string) =>
    request<ChatMessage>(`${BASE}/${id}/messages`, { method: 'POST', body: { content } }),
};

function parseEvent(block: string): ChatMessage | null {
  const lines = block.split('\n');
  if (!lines.some((line) => line.replace('event: ', 'event:') === 'event:NEW_MESSAGE')) return null;
  const data = lines
    .filter((line) => line.startsWith('data:'))
    .map((line) => line.slice(5).trim())
    .join('');
  try {
    return JSON.parse(data) as ChatMessage;
  } catch {
    return null;
  }
}

export async function streamMessages(
  id: string,
  signal: AbortSignal,
  onMessage: (message: ChatMessage) => void,
): Promise<void> {
  const token = loadSession()?.token;
  const response = await fetch(`${BASE}/${id}/stream`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    signal,
  });
  if (!response.ok || !response.body) return;

  const reader = response.body.pipeThrough(new TextDecoderStream()).getReader();
  let buffer = '';
  for (;;) {
    const { value, done } = await reader.read();
    if (done) return;
    buffer += value.replace(/\r/g, '');
    const blocks = buffer.split('\n\n');
    buffer = blocks.pop() ?? '';
    for (const block of blocks) {
      const message = parseEvent(block);
      if (message) onMessage(message);
    }
  }
}

import type { Contact, Conversation } from '../types';

export const contactName = (contact?: Contact): string =>
  contact ? `${contact.firstName} ${contact.lastName}` : 'Lanceify member';

export const otherParticipant = (conversation: Conversation, userId: string): string =>
  conversation.participantIds.find((id) => id !== userId) ?? userId;

export const messageTime = (iso: string): string =>
  new Date(iso).toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });

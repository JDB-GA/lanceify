import { NavLink } from 'react-router-dom';
import type { Contact, Conversation } from '../types';
import { contactName, otherParticipant } from '../utils/chat';

interface Props {
  conversations: Conversation[];
  contacts: Map<string, Contact>;
  userId: string;
}

export default function ConversationList({ conversations, contacts, userId }: Props) {
  return (
    <ul className="conversation-list">
      {conversations.map((conversation) => (
        <li key={conversation.id}>
          <NavLink to={`/messages/${conversation.id}`} className="conversation-item">
            <span className="conversation-text">
              <strong>{contactName(contacts.get(otherParticipant(conversation, userId)))}</strong>
              <small>{conversation.lastMessage ?? 'No messages yet'}</small>
            </span>
            {conversation.unreadCount > 0 && (
              <span className="unread-count" aria-label={`${conversation.unreadCount} unread`}>{conversation.unreadCount}</span>
            )}
          </NavLink>
        </li>
      ))}
    </ul>
  );
}

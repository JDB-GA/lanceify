import { useNavigate, useParams } from 'react-router-dom';
import { chatApi } from '../api/chat';
import ChatThread from '../components/ChatThread';
import ConversationList from '../components/ConversationList';
import ErrorMessage from '../components/ErrorMessage';
import NewConversationForm from '../components/NewConversationForm';
import { useAuth } from '../context/AuthContext';
import { useAsyncAction } from '../hooks/useAsyncAction';
import { useContacts } from '../hooks/useContacts';
import { useConversations } from '../hooks/useConversations';
import { contactName, otherParticipant } from '../utils/chat';

export default function MessagesPage() {
  const { user } = useAuth();
  const { conversationId } = useParams();
  const navigate = useNavigate();
  const { conversations, loading, error, refresh } = useConversations();
  const { contacts, byId, error: contactsError } = useContacts(user?.role);
  const start = useAsyncAction();

  if (!user) return null;

  const active = conversations.find((conversation) => conversation.id === conversationId);
  const title = active ? contactName(byId.get(otherParticipant(active, user.userId))) : 'Conversation';

  const startChat = (contactId: string) =>
    start.run(async () => {
      const conversation = await chatApi.start(contactId);
      refresh();
      navigate(`/messages/${conversation.id}`);
    });

  return (
    <div className={conversationId ? 'chat-layout has-thread' : 'chat-layout'}>
      <aside className="chat-sidebar">
        <h1>Messages</h1>
        <NewConversationForm
          contacts={contacts}
          label={user.role === 'CLIENT' ? 'freelancer' : 'client'}
          busy={start.busy}
          onStart={startChat}
        />
        <ErrorMessage error={start.error ?? contactsError ?? error} />
        {loading && <p className="muted">Loading conversations…</p>}
        {!loading && conversations.length === 0 && <p className="muted">No conversations yet.</p>}
        <ConversationList conversations={conversations} contacts={byId} userId={user.userId} />
      </aside>
      {conversationId ? (
        <ChatThread key={conversationId} conversationId={conversationId} userId={user.userId} title={title} onSent={refresh} />
      ) : (
        <section className="chat-thread chat-placeholder"><p className="muted">Choose a conversation or start a new one.</p></section>
      )}
    </div>
  );
}

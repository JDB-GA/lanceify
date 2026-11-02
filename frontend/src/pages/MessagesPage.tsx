import { useCallback, useEffect, useRef, useState, type FormEvent, type KeyboardEvent } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { conversationsApi } from '../api/conversations';
import Avatar from '../components/Avatar';
import EmptyState from '../components/EmptyState';
import Icon from '../components/Icon';
import NewConversationForm from '../components/NewConversationForm';
import { useAuth } from '../context/AuthContext';
import { useRealtime } from '../context/RealtimeContext';
import { useToast } from '../context/ToastContext';
import { ApiError } from '../api/client';
import { useContacts } from '../hooks/useContacts';
import { clock, newId, timeAgo } from '../format';
import { useLoad } from '../hooks/useLoad';
import { displayName, useUsers } from '../hooks/useUsers';
import type { ChatMessage, Conversation } from '../types';

const MAX_LENGTH = 3000;

const sortByActivity = (items: Conversation[]) =>
  [...items].sort((a, b) => (b.lastMessageAt ?? b.createdAt).localeCompare(a.lastMessageAt ?? a.createdAt));

export default function MessagesPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const { subscribe, refreshUnread } = useRealtime();
  const conversations = useLoad((signal) => conversationsApi.list(signal), []);
  const [filter, setFilter] = useState('');
  const [starting, setStarting] = useState(false);
  const navigate = useNavigate();
  const { notify } = useToast();
  const { contacts } = useContacts(user?.role);
  const me = user?.userId ?? '';

  const startChat = async (contactId: string) => {
    if (starting) return;
    setStarting(true);
    try {
      const conversation = await conversationsApi.start(contactId);
      await conversations.reload();
      navigate(`/messages/${conversation.id}`);
    } catch (err) {
      notify(err instanceof ApiError ? err.message : 'Could not start the chat', 'error');
    } finally {
      setStarting(false);
    }
  };

  const list = conversations.data ?? [];
  const otherOf = (conversation: Conversation) => conversation.participantIds.find((pid) => pid !== me) ?? '';
  const users = useUsers(list.map(otherOf));

  useEffect(
    () =>
      subscribe((event) => {
        if (event.type === 'OPEN') {
          conversations.reload();
          return;
        }
        if (event.type === 'NEW_MESSAGE') {
          const message = event.data;
          conversations.setData((current) => {
            if (!current) return current;
            const target = current.find((item) => item.id === message.conversationId);
            if (!target) {
              conversations.reload();
              return current;
            }
            const unreadDelta = message.recipientId === me && message.conversationId !== id ? 1 : 0;
            return sortByActivity(
              current.map((item) =>
                item.id === message.conversationId
                  ? { ...item, lastMessage: message.content, lastSenderId: message.senderId, lastMessageAt: message.createdAt, unreadCount: item.unreadCount + unreadDelta }
                  : item,
              ),
            );
          });
        }
        if (event.type === 'READ' && event.data.readerId === me) {
          conversations.setData((current) => current?.map((item) => (item.id === event.data.conversationId ? { ...item, unreadCount: 0 } : item)) ?? null);
        }
      }),
    [subscribe, me, id, conversations.setData, conversations.reload],
  );

  const visible = list.filter((conversation) => {
    if (!filter.trim()) return true;
    return displayName(users.get(otherOf(conversation)), '').toLowerCase().includes(filter.trim().toLowerCase());
  });
  const active = list.find((item) => item.id === id);

  return (
    <div className={id ? 'chat-shell thread-open' : 'chat-shell'}>
      <aside className="chat-list">
        <div className="chat-list-head">
          <h1>Messages</h1>
          <NewConversationForm
            contacts={contacts}
            label={user?.role === 'CLIENT' ? 'freelancer' : 'client'}
            busy={starting}
            onStart={startChat}
          />
          <label className="search-field sm">
            <Icon name="search" size={16} />
            <input type="search" placeholder="Search people" value={filter} onChange={(event) => setFilter(event.target.value)} aria-label="Search conversations" />
          </label>
        </div>
        {conversations.loading && !conversations.data && [0, 1, 2, 3].map((key) => <div key={key} className="skeleton convo-sk" />)}
        {conversations.error && <div className="alert error">{conversations.error.message}</div>}
        {conversations.data && visible.length === 0 && (
          <p className="muted pad">{filter ? 'No one matches that name.' : 'No conversations yet. Start one from a proposal or project.'}</p>
        )}
        <ul>
          {visible.map((conversation) => {
            const other = otherOf(conversation);
            const name = displayName(users.get(other));
            const mine = conversation.lastSenderId === me;
            return (
              <li key={conversation.id}>
                <Link to={`/messages/${conversation.id}`} className={conversation.id === id ? 'convo active' : 'convo'}>
                  <Avatar name={name} seed={other} />
                  <span className="convo-body">
                    <span className="row-between">
                      <strong>{name}</strong>
                      <small className="muted">{timeAgo(conversation.lastMessageAt)}</small>
                    </span>
                    <span className="row-between">
                      <span className={conversation.unreadCount > 0 ? 'preview unread' : 'preview'}>
                        {conversation.lastMessage ? `${mine ? 'You: ' : ''}${conversation.lastMessage}` : 'Say hello 👋'}
                      </span>
                      {conversation.unreadCount > 0 && <span className="count-bubble static">{conversation.unreadCount}</span>}
                    </span>
                  </span>
                </Link>
              </li>
            );
          })}
        </ul>
      </aside>
      <section className="chat-thread">
        {id ? (
          <Thread
            key={id}
            conversationId={id}
            me={me}
            otherId={active ? otherOf(active) : ''}
            otherName={active ? displayName(users.get(otherOf(active))) : 'Conversation'}
            projectId={active?.projectId ?? null}
            onRead={() => {
              conversations.setData((current) => current?.map((item) => (item.id === id ? { ...item, unreadCount: 0 } : item)) ?? null);
              refreshUnread();
            }}
          />
        ) : (
          <EmptyState title="Pick a conversation" icon="chat">
            Messages arrive instantly. You can chat with clients and freelancers about any project.
          </EmptyState>
        )}
      </section>
    </div>
  );
}

interface ThreadProps {
  conversationId: string;
  me: string;
  otherId: string;
  otherName: string;
  projectId: string | null;
  onRead: () => void;
}

function Thread({ conversationId, me, otherId, otherName, projectId, onRead }: ThreadProps) {
  const navigate = useNavigate();
  const { subscribe } = useRealtime();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [loaded, setLoaded] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [draft, setDraft] = useState('');
  const bottomRef = useRef<HTMLDivElement>(null);
  const readTimer = useRef<number>();

  const merge = useCallback((incoming: ChatMessage) => {
    setMessages((current) => {
      const index = current.findIndex(
        (item) => item.id === incoming.id || (incoming.clientMessageId && item.clientMessageId === incoming.clientMessageId),
      );
      if (index === -1) return [...current, incoming];
      const next = [...current];
      next[index] = { ...incoming, pending: false, failed: false };
      return next;
    });
  }, []);

  const load = useCallback(async (signal?: AbortSignal) => {
    try {
      const fetched = await conversationsApi.messages(conversationId, signal);
      setMessages((current) => {
        const known = new Set(fetched.map((item) => item.clientMessageId ?? item.id));
        const unsent = current.filter((item) => (item.pending || item.failed) && !known.has(item.clientMessageId ?? item.id));
        return [...fetched, ...unsent];
      });
      setError(null);
      onRead();
    } catch (err) {
      if (signal?.aborted) return;
      setError(err instanceof Error ? err.message : 'Could not load messages');
    } finally {
      if (!signal?.aborted) setLoaded(true);
    }
  }, [conversationId]);

  useEffect(() => {
    const controller = new AbortController();
    load(controller.signal);
    return () => controller.abort();
  }, [load]);

  const scheduleRead = useCallback(() => {
    window.clearTimeout(readTimer.current);
    readTimer.current = window.setTimeout(() => {
      conversationsApi.markRead(conversationId).then(onRead).catch(() => undefined);
    }, 400);
  }, [conversationId]);

  useEffect(() => () => window.clearTimeout(readTimer.current), []);

  useEffect(
    () =>
      subscribe((event) => {
        if (event.type === 'OPEN') {
          load();
        } else if (event.type === 'NEW_MESSAGE' && event.data.conversationId === conversationId) {
          merge(event.data);
          if (event.data.recipientId === me && document.visibilityState === 'visible') scheduleRead();
        } else if (event.type === 'READ' && event.data.conversationId === conversationId && event.data.readerId !== me) {
          setMessages((current) => current.map((item) => (item.senderId === me ? { ...item, read: true } : item)));
        }
      }),
    [subscribe, conversationId, me, merge, load, scheduleRead],
  );

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: loaded ? 'smooth' : 'auto', block: 'end' });
  }, [messages.length, loaded]);

  const deliver = async (message: ChatMessage) => {
    try {
      merge(await conversationsApi.send(conversationId, message.content, message.clientMessageId ?? message.id));
    } catch {
      setMessages((current) =>
        current.map((item) => (item.clientMessageId === message.clientMessageId ? { ...item, pending: false, failed: true } : item)),
      );
    }
  };

  const send = (event?: FormEvent) => {
    event?.preventDefault();
    const content = draft.trim();
    if (!content) return;
    const clientMessageId = newId();
    const optimistic: ChatMessage = {
      id: clientMessageId,
      conversationId,
      senderId: me,
      recipientId: otherId,
      content,
      clientMessageId,
      read: false,
      createdAt: new Date().toISOString(),
      pending: true,
    };
    setMessages((current) => [...current, optimistic]);
    setDraft('');
    deliver(optimistic);
  };

  const retry = (message: ChatMessage) => {
    setMessages((current) =>
      current.map((item) => (item.clientMessageId === message.clientMessageId ? { ...item, pending: true, failed: false } : item)),
    );
    deliver(message);
  };

  const onKey = (event: KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      send();
    }
  };

  const lastMine = [...messages].reverse().find((item) => item.senderId === me && !item.pending && !item.failed);

  return (
    <>
      <header className="thread-head">
        <button className="icon-button mobile-only" aria-label="Back to conversations" onClick={() => navigate('/messages')}><Icon name="back" /></button>
        <Avatar name={otherName} seed={otherId || conversationId} />
        <div>
          <strong>{otherName}</strong>
          {projectId && <Link to={`/projects/${projectId}`} className="small link">View project</Link>}
        </div>
      </header>
      <div className="thread-body">
        {!loaded && <div className="skeleton bubble-sk" />}
        {error && <div className="alert error">{error}</div>}
        {loaded && messages.length === 0 && !error && (
          <div className="thread-empty"><Icon name="chat" size={28} /><p>No messages yet. Say hello to {otherName}!</p></div>
        )}
        {messages.map((message, index) => {
          const mine = message.senderId === me;
          const previous = messages[index - 1];
          const grouped = previous && previous.senderId === message.senderId &&
            new Date(message.createdAt).getTime() - new Date(previous.createdAt).getTime() < 120_000;
          return (
            <div key={message.clientMessageId ?? message.id} className={`bubble-row ${mine ? 'mine' : ''} ${grouped ? 'grouped' : ''}`}>
              <div className={`bubble ${message.pending ? 'pending' : ''} ${message.failed ? 'failed' : ''}`}>
                <p>{message.content}</p>
                <span className="bubble-meta">
                  {clock(message.createdAt)}
                  {message.pending && ' · sending'}
                  {mine && message === lastMine && (message.read ? ' · Seen' : ' · Delivered')}
                </span>
              </div>
              {message.failed && (
                <button className="retry" onClick={() => retry(message)}><Icon name="x" size={12} />Not sent · Retry</button>
              )}
            </div>
          );
        })}
        <div ref={bottomRef} />
      </div>
      <form className="composer" onSubmit={send}>
        <textarea
          rows={1}
          placeholder={`Message ${otherName}…`}
          maxLength={MAX_LENGTH}
          value={draft}
          onChange={(event) => setDraft(event.target.value)}
          onKeyDown={onKey}
          aria-label="Write a message"
        />
        <button className="send-button" disabled={!draft.trim()} aria-label="Send message"><Icon name="send" size={18} /></button>
      </form>
    </>
  );
}

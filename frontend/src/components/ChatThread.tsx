import { useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import { chatApi } from '../api/chat';
import { useAsyncAction } from '../hooks/useAsyncAction';
import { useMessages } from '../hooks/useMessages';
import { messageTime } from '../utils/chat';
import ErrorMessage from './ErrorMessage';
import MessageComposer from './MessageComposer';

interface Props {
  conversationId: string;
  userId: string;
  title: string;
  onSent: () => void;
}

export default function ChatThread({ conversationId, userId, title, onSent }: Props) {
  const { messages, loading, error, add } = useMessages(conversationId);
  const send = useAsyncAction();
  const end = useRef<HTMLDivElement>(null);

  useEffect(() => {
    end.current?.scrollIntoView({ block: 'nearest' });
  }, [messages.length]);

  const onSend = (content: string) =>
    send.run(async () => {
      add(await chatApi.send(conversationId, content));
      onSent();
    });

  return (
    <section className="chat-thread" aria-label={`Conversation with ${title}`}>
      <header className="chat-header">
        <Link className="chat-back" to="/messages">Back</Link>
        <h2>{title}</h2>
      </header>
      <div className="chat-messages" aria-live="polite">
        {loading && <p className="muted">Loading messages…</p>}
        {!loading && messages.length === 0 && !error && <p className="muted">No messages yet. Say hello.</p>}
        {messages.map((message) => {
          const mine = message.senderId === userId;
          return (
            <article key={message.id} className={mine ? 'bubble mine' : 'bubble'}>
              <p>{message.content}</p>
              <time dateTime={message.createdAt}>{messageTime(message.createdAt)}{mine && message.read ? ' · Read' : ''}</time>
            </article>
          );
        })}
        <div ref={end} />
      </div>
      <ErrorMessage error={error ?? send.error} />
      <MessageComposer busy={send.busy} onSend={onSend} />
    </section>
  );
}

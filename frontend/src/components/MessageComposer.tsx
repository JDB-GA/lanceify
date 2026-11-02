import { useState, type FormEvent, type KeyboardEvent } from 'react';

const MAX_LENGTH = 3000;

interface Props {
  busy: boolean;
  onSend: (content: string) => Promise<boolean>;
}

export default function MessageComposer({ busy, onSend }: Props) {
  const [content, setContent] = useState('');

  const send = async () => {
    const text = content.trim();
    if (!text || busy) return;
    if (await onSend(text)) setContent('');
  };

  const submit = (event: FormEvent) => {
    event.preventDefault();
    send();
  };

  const onKeyDown = (event: KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      send();
    }
  };

  return (
    <form className="message-composer" onSubmit={submit}>
      <textarea
        aria-label="Message"
        placeholder="Write a message"
        rows={2}
        maxLength={MAX_LENGTH}
        value={content}
        onChange={(event) => setContent(event.target.value)}
        onKeyDown={onKeyDown}
      />
      <button className="button" disabled={busy || !content.trim()}>Send</button>
    </form>
  );
}

import { useState, type FormEvent } from 'react';
import type { Contact } from '../types';
import { contactName } from '../utils/chat';

interface Props {
  contacts: Contact[];
  label: string;
  busy: boolean;
  onStart: (contactId: string) => void;
}

export default function NewConversationForm({ contacts, label, busy, onStart }: Props) {
  const [contactId, setContactId] = useState('');

  const submit = (event: FormEvent) => {
    event.preventDefault();
    if (contactId) onStart(contactId);
  };

  return (
    <form className="new-conversation" onSubmit={submit}>
      <select value={contactId} onChange={(event) => setContactId(event.target.value)} required aria-label={`Message a ${label}`}>
        <option value="">{contacts.length === 0 ? `No ${label}s available` : `New chat with a ${label}…`}</option>
        {contacts.map((contact) => <option key={contact.id} value={contact.id}>{contactName(contact)}</option>)}
      </select>
      <button className="button sm" disabled={busy || !contactId}>Start</button>
    </form>
  );
}

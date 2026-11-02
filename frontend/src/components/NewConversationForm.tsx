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
      <label>Message a {label}
        <select value={contactId} onChange={(event) => setContactId(event.target.value)} required>
          <option value="">{contacts.length === 0 ? `No ${label}s available` : `Choose a ${label}`}</option>
          {contacts.map((contact) => <option key={contact.id} value={contact.id}>{contactName(contact)}</option>)}
        </select>
      </label>
      <button className="button secondary" disabled={busy || !contactId}>Start chat</button>
    </form>
  );
}

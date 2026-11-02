import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ApiError } from '../api/client';
import { conversationsApi } from '../api/conversations';
import { useToast } from '../context/ToastContext';
import Icon from './Icon';

interface Props {
  recipientId: string;
  projectId?: string | null;
  label?: string;
  className?: string;
}

export default function MessageButton({ recipientId, projectId, label = 'Message', className = 'button secondary sm' }: Props) {
  const navigate = useNavigate();
  const { notify } = useToast();
  const [busy, setBusy] = useState(false);

  const open = async () => {
    if (busy) return;
    setBusy(true);
    try {
      const conversation = await conversationsApi.start(recipientId, projectId);
      navigate(`/messages/${conversation.id}`);
    } catch (err) {
      notify(err instanceof ApiError ? err.message : 'Could not open the conversation', 'error');
      setBusy(false);
    }
  };

  return (
    <button type="button" className={className} disabled={busy} onClick={open}>
      <Icon name="chat" size={16} />{label}
    </button>
  );
}

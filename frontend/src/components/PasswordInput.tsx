import { useState, type InputHTMLAttributes } from 'react';

export default function PasswordInput(props: Omit<InputHTMLAttributes<HTMLInputElement>, 'type'>) {
  const [visible, setVisible] = useState(false);
  return (
    <div className="input-group">
      <input {...props} type={visible ? 'text' : 'password'} className="with-reveal" />
      <button
        type="button"
        className="reveal"
        aria-label={visible ? 'Hide password' : 'Show password'}
        onClick={() => setVisible(!visible)}
      >
        {visible ? 'Hide' : 'Show'}
      </button>
    </div>
  );
}

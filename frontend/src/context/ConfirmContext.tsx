import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';

export interface ConfirmOptions {
  title: string;
  message?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  danger?: boolean;
}

type Confirm = (options: ConfirmOptions) => Promise<boolean>;

const ConfirmContext = createContext<Confirm | null>(null);

export function ConfirmProvider({ children }: { children: ReactNode }) {
  const [options, setOptions] = useState<ConfirmOptions | null>(null);
  const resolver = useRef<((confirmed: boolean) => void) | null>(null);
  const dialog = useRef<HTMLDialogElement>(null);

  const confirm = useCallback<Confirm>((next) => {
    resolver.current?.(false);
    setOptions(next);
    return new Promise((resolve) => {
      resolver.current = resolve;
    });
  }, []);

  const close = (confirmed: boolean) => {
    resolver.current?.(confirmed);
    resolver.current = null;
    setOptions(null);
  };

  useEffect(() => {
    if (options && !dialog.current?.open) dialog.current?.showModal();
  }, [options]);

  const value = useMemo(() => confirm, [confirm]);

  return (
    <ConfirmContext.Provider value={value}>
      {children}
      {options && (
        <dialog
          ref={dialog}
          className="confirm-dialog"
          aria-labelledby="confirm-title"
          aria-describedby={options.message ? 'confirm-message' : undefined}
          onCancel={(event) => {
            event.preventDefault();
            close(false);
          }}
          onClick={(event) => {
            if (event.target === dialog.current) close(false);
          }}
        >
          <div className="confirm-body">
            <h2 id="confirm-title">{options.title}</h2>
            {options.message && <p id="confirm-message">{options.message}</p>}
            <div className="confirm-actions">
              <button type="button" className="button secondary" autoFocus onClick={() => close(false)}>
                {options.cancelLabel ?? 'Cancel'}
              </button>
              <button type="button" className={options.danger ? 'button danger' : 'button'} onClick={() => close(true)}>
                {options.confirmLabel ?? 'Confirm'}
              </button>
            </div>
          </div>
        </dialog>
      )}
    </ConfirmContext.Provider>
  );
}

export function useConfirm(): Confirm {
  const context = useContext(ConfirmContext);
  if (!context) throw new Error('useConfirm must be used within ConfirmProvider');
  return context;
}

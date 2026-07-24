import React, { useEffect, useState } from 'react';
import { TOAST_EVENT } from '../utils/toastBus';
import './ToastHost.css';

function ToastHost() {
  const [toast, setToast] = useState(null);

  useEffect(() => {
    let timer;
    const onToast = (e) => {
      const message = e.detail?.message || 'Готово';
      clearTimeout(timer);
      setToast({ message, id: Date.now() });
      timer = setTimeout(() => setToast(null), 3200);
    };
    window.addEventListener(TOAST_EVENT, onToast);
    return () => {
      clearTimeout(timer);
      window.removeEventListener(TOAST_EVENT, onToast);
    };
  }, []);

  if (!toast) return null;

  return (
    <div className="app-toast" role="status" aria-live="polite">
      {toast.message}
    </div>
  );
}

export default ToastHost;

export const TOAST_EVENT = 'admin-app-toast';

/**
 * Универсальное всплывающее уведомление (слушает {@link ToastHost} в App).
 * @param {string} [message='Успешно обновлено!']
 */
export function toastSuccess(message = 'Успешно обновлено!') {
  window.dispatchEvent(new CustomEvent(TOAST_EVENT, { detail: { message } }));
}

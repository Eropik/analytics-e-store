/**
 * Дата и время заказа для интерфейса: ДД.ММ.ГГГГ и ЧЧ:ММ (24 ч), локаль ru-RU.
 */
export function formatOrderDateTime(raw) {
  if (raw == null || raw === '') return '—';

  let d;
  if (typeof raw === 'number') {
    d = new Date(raw);
  } else if (typeof raw === 'string') {
    const s = raw.trim();
    d = new Date(s.includes(' ') && !s.includes('T') ? s.replace(' ', 'T') : s);
  } else {
    d = new Date(raw);
  }

  if (Number.isNaN(d.getTime())) {
    return typeof raw === 'string' ? raw : '—';
  }

  const datePart = d.toLocaleDateString('ru-RU', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
  const timePart = d.toLocaleTimeString('ru-RU', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  });

  return `${datePart}, ${timePart}`;
}

/**
 * Форматирует дату с бэкенда (ISO-строка или массив LocalDateTime от Jackson).
 * Выход: yyyy-mm-dd hh:mm
 */
export function formatAdminDateTime(value) {
  if (value === null || value === undefined || value === '') return '—';

  if (Array.isArray(value)) {
    const [y, mo = 1, d = 1, h = 0, mi = 0] = value;
    const pad = (n, l = 2) => String(n).padStart(l, '0');
    if (value.length >= 5) {
      return `${pad(y, 4)}-${pad(mo)}-${pad(d)} ${pad(h)}:${pad(mi)}`;
    }
    return `${pad(y, 4)}-${pad(mo)}-${pad(d)} 00:00`;
  }

  const d = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(d.getTime())) return String(value);

  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

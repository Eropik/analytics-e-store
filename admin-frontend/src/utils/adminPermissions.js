/**
 * Права с бэкенда (POST /auth/login → permissions).
 * Фолбэк по локальному adminDepartment для старых сессий без JSON в localStorage.
 */
export function getAdminPermissions() {
  try {
    const raw = localStorage.getItem('adminPermissions');
    if (raw) {
      const p = JSON.parse(raw);
      if (p && typeof p === 'object') return p;
    }
  } catch (_) {
    /* ignore */
  }
  const dep = localStorage.getItem('adminDepartment') || '';
  /* Строго по отделу; ANALYZE — только свои эндпоинты аналитики на бэкенде */
  return {
    hasAnalytics: dep === 'ANALYZE',
    hasProductManagement: dep === 'PRODUCT_MANAGE',
    hasOrderManagement: dep === 'ORDER_MANAGE',
    hasUserManagement: dep === 'USER_MANAGE',
  };
}

export const PERMISSION_TO_DEPARTMENTS = {
  hasProductManagement: ['PRODUCT_MANAGE'],
  hasOrderManagement: ['ORDER_MANAGE'],
  hasUserManagement: ['USER_MANAGE'],
  hasAnalytics: ['ANALYZE'],
};

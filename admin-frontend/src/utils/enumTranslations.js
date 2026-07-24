const DEPARTMENT_MAP = {
  ANALYZE: 'Аналитик',
  USER_MANAGE: 'Менеджер пользователей',
  PRODUCT_MANAGE: 'Менеджер товаров',
  ORDER_MANAGE: 'Менеджер заказов',
};

const STATUS_MAP = {
  PROCESSING: 'В обработке',
  IN_TRANSIT: 'В пути',
  DELIVERED: 'Доставлен',
  CANCELLED: 'Отменен',
};

const ROLE_MAP = {
  ROLE_ADMIN: 'Администратор',
  ROLE_CUSTOMER: 'Покупатель',
};

const GENDER_MAP = {
  M: 'Мужской',
  F: 'Женский',
  N: 'Не указан',
};

const DELIVERY_METHOD_MAP = {
  'Self-Pickup': 'Самовывоз',
  'Standard Delivery': 'Стандартная доставка',
  'Express Delivery': 'Экспресс-доставка',
};

const PAYMENT_METHOD_MAP = {
  'Card Online': 'Карта онлайн',
  'Cash on Delivery': 'Наличными при получении',
  'Bank Transfer': 'Банковский перевод',
};

export const translateDepartment = (value) => DEPARTMENT_MAP[value] || value || 'Не указан';
export const translateOrderStatus = (value) => STATUS_MAP[value] || value || 'Не указан';
export const translateRole = (value) => ROLE_MAP[value] || value || 'Не указан';
export const translateGender = (value) => GENDER_MAP[value] || value || 'Не указан';
export const translateDeliveryMethod = (value) => DELIVERY_METHOD_MAP[value] || value || 'Не указан';
export const translatePaymentMethod = (value) => PAYMENT_METHOD_MAP[value] || value || 'Не указан';

export const formatAccessDenied = (errorText) => {
  const text = String(errorText || '');
  const knownDepartments = Object.keys(DEPARTMENT_MAP).filter((dep) => text.includes(dep));
  if (knownDepartments.length > 0) {
    const translated = knownDepartments.map((dep) => `${translateDepartment(dep)} (${dep})`).join(', ');
    return `Нет доступа к разделу. Требуется отдел: ${translated}.`;
  }
  if (/access|forbidden|denied|доступ/i.test(text)) {
    return `Нет доступа к разделу. (${text})`;
  }
  return text || 'Нет доступа к разделу.';
};

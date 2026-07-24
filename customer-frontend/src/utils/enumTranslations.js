const ORDER_STATUS_MAP = {
  PROCESSING: 'В обработке',
  IN_TRANSIT: 'В пути',
  DELIVERED: 'Доставлен',
  CANCELLED: 'Отменен',
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

const GENDER_MAP = {
  M: 'Мужской',
  F: 'Женский',
  N: 'Не указан',
};

export const translateOrderStatus = (value) => ORDER_STATUS_MAP[value] || value || 'Не указан';

export const translateDeliveryMethod = (value) => DELIVERY_METHOD_MAP[value] || value || 'Не указан';

export const translatePaymentMethod = (value) => PAYMENT_METHOD_MAP[value] || value || 'Не указан';

export const translateGender = (value) => GENDER_MAP[value] || value || 'Не указан';

import axios from 'axios';

// Используем прокси через setupProxy (порт 3000 -> 8020)
const API_BASE_URL = process.env.REACT_APP_API_BASE || '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const authService = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  checkEmail: (email) => api.get(`/auth/check-email?email=${encodeURIComponent(email)}`),
};

export const productService = {
  getAll: (page = 0, size = 20, params = {}) =>
    api.get('/customer/products', { params: { page, size, ...params } }),
  
  getById: (id) => 
    api.get(`/customer/products/${id}`),
  
  search: (query, page = 0, size = 20) =>
    api.get('/customer/products/search', { params: { query, page, size } }),

  getCategories: () => api.get('/customer/products/categories'),
  getBrands: () => api.get('/customer/products/brands'),

  byCategory: (categoryId, page = 0, size = 20) =>
    api.get(`/customer/products/category/${categoryId}`, { params: { page, size } }),

  byBrand: (brandId, page = 0, size = 20) =>
    api.get(`/customer/products/brand/${brandId}`, { params: { page, size } }),

  byPriceRange: (min, max, page = 0, size = 20) =>
    api.get('/customer/products/price-range', { params: { min, max, page, size } }),
};

export const cartService = {
  get: (userId) => 
    api.get(`/customer/cart/${userId}`),
  
  add: (data) => 
    api.post('/customer/cart/add', data),
  
  update: (data) => 
    api.put('/customer/cart/update', data),
  
  remove: (data) => 
    api.delete('/customer/cart/remove', { data }),
};

export const orderService = {
  create: (data) => 
    api.post('/customer/orders', data),
  
  getUserOrders: (userId, page = 0, size = 20) => 
    api.get(`/customer/orders/user/${userId}?page=${page}&size=${size}`),

  getDeliveryMethods: () => api.get('/customer/orders/delivery-methods'),
  getPaymentMethods: () => api.get('/customer/orders/payment-methods'),

  cancel: (orderId, userId) =>
    api.post(`/customer/orders/${orderId}/cancel?userId=${userId}`),

  getById: (orderId) => api.get(`/customer/orders/${orderId}`),
};

export const profileService = {
  get: (userId) => 
    api.get(`/customer/profile/${userId}`),
  
  update: (userId, data) => 
    api.put(`/customer/profile/${userId}`, data),

  uploadAvatar: (userId, file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post(`/customer/profile/${userId}/avatar`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  getCities: () => api.get('/customer/profile/cities'),
};

export default api;


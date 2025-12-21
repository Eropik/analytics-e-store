import axios from 'axios';

// Используем прокси: относительный базовый путь, чтобы обойти CORS через setupProxy
const API_BASE_URL = process.env.REACT_APP_API_BASE || '/api/admin';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const userService = {
  getAll: (page = 0, size = 20, adminUserId) => 
    api.get(`/users?adminUserId=${adminUserId}&page=${page}&size=${size}`),
  
  getById: (id, adminUserId) => 
    api.get(`/users/${id}?adminUserId=${adminUserId}`),

  getByRole: (roleName, adminUserId, page = 0, size = 50) =>
    api.get(`/users/role/${encodeURIComponent(roleName)}?adminUserId=${adminUserId}&page=${page}&size=${size}`),

  getFullInfo: (id, adminUserId) =>
    api.get(`/users/userfullinfo/${id}?adminUserId=${adminUserId}`),

  search: (query, adminUserId, page = 0, size = 50) =>
    api.get(`/users/search?adminUserId=${adminUserId}&email=${encodeURIComponent(query)}&page=${page}&size=${size}`),

  activate: (id, adminUserId) =>
    api.put(`/users/${id}/activate?adminUserId=${adminUserId}`),

  deactivate: (id, adminUserId) =>
    api.put(`/users/${id}/deactivate?adminUserId=${adminUserId}`),
  
  create: (data, adminUserId) => 
    api.post(`/users?adminUserId=${adminUserId}`, data),
  
  update: (id, data, adminUserId) => 
    api.put(`/users/${id}?adminUserId=${adminUserId}`, data),
};

export const productService = {
  getAll: (page = 0, size = 20, adminUserId) => 
    api.get(`/products?adminUserId=${adminUserId}&page=${page}&size=${size}`),

  search: ({ productId, name, categoryId, brandId, minPrice, maxPrice, page = 0, size = 200, adminUserId }) =>
    api.get('/products/search', {
      params: {
        adminUserId,
        productId,
        query: name,
        categoryId,
        brandId,
        minPrice,
        maxPrice,
        page,
        size
      }
    }),

  getCategories: (adminUserId) =>
    api.get(`/products/categories?adminUserId=${adminUserId}`),

  getBrands: (adminUserId) =>
    api.get(`/products/brands?adminUserId=${adminUserId}`),
  
  getById: (id, adminUserId) => 
    api.get(`/products/${id}?adminUserId=${adminUserId}`),
  
  create: (data, adminUserId) => 
    api.post(`/products?adminUserId=${adminUserId}`, data),
  
  update: (id, data, adminUserId) => 
    api.put(`/products/${id}?adminUserId=${adminUserId}`, data),
  
  delete: (id, adminUserId) => 
    api.delete(`/products/${id}?adminUserId=${adminUserId}`),

  uploadImage: (file, adminUserId) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post(`/products/upload-image?adminUserId=${adminUserId}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  addImageToProduct: (productId, payload, adminUserId) =>
    api.post(`/products/${productId}/images?adminUserId=${adminUserId}`, payload),

  addCategory: (name, adminUserId) =>
    api.post(`/products/categories?adminUserId=${adminUserId}`, { categoryName: name }),

  addBrand: (name, adminUserId) =>
    api.post(`/products/brands?adminUserId=${adminUserId}`, { brandName: name }),

  updateCategory: (categoryId, name, adminUserId) =>
    api.put(`/products/categories/${categoryId}?adminUserId=${adminUserId}`, { categoryName: name }),

  updateBrand: (brandId, name, adminUserId) =>
    api.put(`/products/brands/${brandId}?adminUserId=${adminUserId}`, { brandName: name }),

  searchCategories: (query, adminUserId) =>
    api.get(`/products/categories/search?adminUserId=${adminUserId}&query=${encodeURIComponent(query)}`),

  searchBrands: (query, adminUserId) =>
    api.get(`/products/brands/search?adminUserId=${adminUserId}&query=${encodeURIComponent(query)}`),
};

export const orderService = {
  getAll: (page = 0, size = 20, adminUserId) => 
    api.get(`/orders?adminUserId=${adminUserId}&page=${page}&size=${size}`),
  
  getById: (id, adminUserId) => 
    api.get(`/orders/${id}?adminUserId=${adminUserId}`),

  getUserBasic: (userId, adminUserId) =>
    api.get(`/orders/user/${userId}?adminUserId=${adminUserId}`),
  
  updateStatus: (id, statusId, adminUserId) => 
    api.put(`/orders/${id}/status?adminUserId=${adminUserId}&statusId=${statusId}`),

  updateLogistics: (id, adminUserId, payload) =>
    api.put(`/orders/${id}/logistics`, null, { params: { adminUserId, ...payload } }),
};

export const analyticsService = {
  getBestSellers: (adminUserId, limit = 10) => 
    api.get(`/analytics/product/best-sellers?adminUserId=${adminUserId}&limit=${limit}`),
  
  getCategoryBrandAnalysis: (adminUserId) => 
    api.get(`/analytics/product/category-brand?adminUserId=${adminUserId}`),
  
  getAgeGroupAnalysis: (adminUserId) => 
    api.get(`/analytics/user/age-groups?adminUserId=${adminUserId}`),
  
  getRouteAnalysis: (adminUserId) => 
    api.get(`/analytics/product/routes?adminUserId=${adminUserId}`),
  
  getPaymentMethodAnalysis: (adminUserId) => 
    api.get(`/analytics/order/payment-methods?adminUserId=${adminUserId}`),
  
  getDeliveryMethodAnalysis: (adminUserId) => 
    api.get(`/analytics/order/delivery-methods?adminUserId=${adminUserId}`),

  productOverview: (adminUserId) =>
    api.get(`/analytics/product/overview?adminUserId=${adminUserId}`),
  userOverview: (adminUserId) =>
    api.get(`/analytics/user/overview?adminUserId=${adminUserId}`),
  orderOverview: (adminUserId) =>
    api.get(`/analytics/order/overview?adminUserId=${adminUserId}`),
  orderFilter: (adminUserId, params) =>
    api.get(`/analytics/order/filter`, { params: { adminUserId, ...params } }),
  analyzeGeneric: (adminUserId, params) =>
    api.get(`/analytics/analyze`, { params: { adminUserId, ...params } }),
};

export const authService = {
  login: (data) => api.post('/auth/login', data),
};

export const cityService = {
  getAll: (adminUserId) => api.get(`/cities?adminUserId=${adminUserId}`),
  search: (query, adminUserId) => api.get(`/cities/search?adminUserId=${adminUserId}&query=${encodeURIComponent(query)}`),
  create: (name, adminUserId) =>
    api.post(`/cities?adminUserId=${adminUserId}`, { cityName: name }),
  update: (cityId, name, adminUserId) =>
    api.put(`/cities/${cityId}?adminUserId=${adminUserId}`, { cityName: name }),
  getRoutes: (adminUserId) => api.get(`/cities/routes?adminUserId=${adminUserId}`),
  createRoute: (payload, adminUserId) =>
    api.post(`/cities/routes?adminUserId=${adminUserId}`, payload),
  updateRoute: (routeId, payload, adminUserId) =>
    api.put(`/cities/routes/${routeId}?adminUserId=${adminUserId}`, payload),
};

export const warehouseService = {
  getAll: (adminUserId) => api.get(`/warehouses?adminUserId=${adminUserId}`),
  create: (data, adminUserId) => api.post(`/warehouses?adminUserId=${adminUserId}`, data),
  update: (id, data, adminUserId) => api.put(`/warehouses/${id}?adminUserId=${adminUserId}`, data),
};

export default api;




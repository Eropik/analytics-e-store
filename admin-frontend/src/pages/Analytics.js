import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { analyticsService } from '../services/api';
import './Analytics.css';
import {
  PieChart, Pie, Cell, Tooltip, Legend,
  BarChart, Bar, XAxis, YAxis, CartesianGrid,
  LineChart, Line, ResponsiveContainer
} from 'recharts';

const Section = ({ title, children }) => (
  <details className="analytics-section" open>
    <summary>{title}</summary>
    {children}
  </details>
);

function Analytics() {
  const palette = ['#3498db', '#e67e22', '#9b59b6', '#16a085', '#e74c3c', '#f1c40f', '#2ecc71', '#34495e'];
  const [loading, setLoading] = useState(true);
  const [productData, setProductData] = useState({});
  const [userData, setUserData] = useState({});
  const [orderData, setOrderData] = useState({});
  const [analyzeData, setAnalyzeData] = useState([]);
  const [productError, setProductError] = useState('');
  const [userError, setUserError] = useState('');
  const [orderError, setOrderError] = useState('');
  const [analyzeError, setAnalyzeError] = useState('');
  const [analyzeFilters, setAnalyzeFilters] = useState({
    scope: 'products',
    gender: '',
    ageGroup: '',
    month: ''
  });
  const [orderFilter, setOrderFilter] = useState({
    status: '',
    gender: '',
    ageGroup: '',
    categoryId: '',
    brandId: ''
  });
  const navigate = useNavigate();
  const adminUserId = localStorage.getItem('adminUserId');
  const department = localStorage.getItem('adminDepartment') || '';
  const canProduct = department === 'PRODUCT_MANAGE' || department === 'ANALYZE';
  const canOrder = department === 'ORDER_MANAGE' || department === 'ANALYZE';
  const canUser = department === 'USER_MANAGE' || department === 'ANALYZE';
  const canAnalyze = department === 'ANALYZE';
  const availableTabs = [
    ...(canProduct ? ['product'] : []),
    ...(canUser ? ['user'] : []),
    ...(canOrder ? ['order'] : []),
    ...(canAnalyze ? ['analyze'] : []),
  ];
  const [activeTab, setActiveTab] = useState(availableTabs[0] || '');

  useEffect(() => {
    if (!adminUserId) {
      navigate('/login');
      return;
    }
    loadAnalytics();
  }, [adminUserId, navigate, canProduct, canOrder, canUser, canAnalyze]);

  const loadAnalytics = async () => {
    setLoading(true);
    setProductError('');
    setUserError('');
    setOrderError('');
    setAnalyzeError('');

    const tasks = [];

    if (canProduct) {
      tasks.push(
        analyticsService.productOverview(adminUserId)
          .then(res => setProductData(res.data || {}))
          .catch(err => {
            console.error('Product analytics error', err);
            setProductData({});
            setProductError(err.response?.data?.error || 'Нет доступа: требуется PRODUCT_MANAGE или ANALYZE');
          })
      );
    }

    if (canUser) {
      tasks.push(
        analyticsService.userOverview(adminUserId)
          .then(res => setUserData(res.data || {}))
          .catch(err => {
            console.error('User analytics error', err);
            setUserData({});
            setUserError(err.response?.data?.error || 'Нет доступа: требуется USER_MANAGE или ANALYZE');
          })
      );
    }

    if (canOrder) {
      tasks.push(
        analyticsService.orderOverview(adminUserId)
          .then(res => setOrderData(res.data || {}))
          .then(() => loadOrderFilter(orderFilter))
          .catch(err => {
            console.error('Order analytics error', err);
            setOrderData({});
            setOrderError(err.response?.data?.error || 'Нет доступа: требуется ORDER_MANAGE или ANALYZE');
          })
      );
    }

    if (canAnalyze) {
      tasks.push(loadAnalyze(analyzeFilters).catch(() => {}));
    }

    Promise.all(tasks).finally(() => setLoading(false));
  };

  const loadAnalyze = async (filters) => {
    try {
      const res = await analyticsService.analyzeGeneric(adminUserId, {
        scope: filters.scope,
        gender: filters.gender || undefined,
        ageGroup: filters.ageGroup || undefined,
        month: filters.month || undefined
      });
      setAnalyzeData(res.data.result || []);
      setAnalyzeError('');
    } catch (e) {
      console.error('Error analyze', e);
      setAnalyzeData([]);
      setAnalyzeError(e.response?.data?.error || 'Нет доступа: требуется ANALYZE');
    }
  };

  const loadOrderFilter = async (filters) => {
    try {
      const res = await analyticsService.orderFilter(adminUserId, {
        status: filters.status || undefined,
        gender: filters.gender || undefined,
        ageGroup: filters.ageGroup || undefined,
        categoryId: filters.categoryId || undefined,
        brandId: filters.brandId || undefined,
      });
      setOrderData((prev) => ({ ...prev, filtered: res.data }));
    } catch (e) {
      console.error('Error order filter', e);
      setOrderData((prev) => ({ ...prev, filtered: { brands: [], categories: [], products: [] } }));
      setOrderError(e.response?.data?.error || 'Нет доступа: требуется ORDER_MANAGE или ANALYZE');
    }
  };

  if (loading) return <div>Загрузка...</div>;

  const renderPie = (data) => (
    <div className="chart-box">
      <ResponsiveContainer width="100%" height={260}>
        <PieChart>
          <Pie
            data={data || []}
            dataKey="value"
            nameKey="label"
            cx="50%"
            cy="50%"
            outerRadius={90}
            label
          >
            {(data || []).map((_, idx) => (
              <Cell key={idx} fill={palette[idx % palette.length]} />
            ))}
          </Pie>
          <Tooltip />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );

  const renderBuckets = (data, label1 = 'Bucket') => (
    <div className="chart-box">
      <ResponsiveContainer width="100%" height={260}>
        <BarChart data={data || []}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="label" />
          <YAxis />
          <Tooltip />
          <Bar dataKey="value" fill={palette[0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );

  const renderTimeSeries = (data) => (
    <div className="chart-box">
      <ResponsiveContainer width="100%" height={260}>
        <LineChart data={data || []}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="label" />
          <YAxis />
          <Tooltip />
          <Line type="monotone" dataKey="value" stroke={palette[1]} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );

  return (
    <div className="analytics">
      <h1>Аналитика</h1>
      <div className="tabs">
        {availableTabs.map((tab) => (
          <button
            key={tab}
            className={activeTab === tab ? 'active' : ''}
            onClick={() => setActiveTab(tab)}
          >
            {tab === 'product' && 'Product'}
            {tab === 'user' && 'User'}
            {tab === 'order' && 'Order'}
            {tab === 'analyze' && 'Analyze'}
          </button>
        ))}
      </div>

      {!availableTabs.length && <div className="error-msg">Нет доступа ни к одному разделу аналитики</div>}

      {activeTab === 'product' && (
        <Section title="Product (категории/бренды/цены/маршруты)">
          {productError && <div className="error-msg">{productError}</div>}
          <h4>Категории</h4>
          {renderPie(productData.categoryShare)}
          <h4>Бренды</h4>
          {renderPie(productData.brandShare)}
          <h4>Ценовые диапазоны</h4>
          {renderBuckets(productData.priceBuckets, 'Диапазон')}
          <h4>Топ городов маршрутов</h4>
          {renderPie(productData.topCitiesRoutes)}
          <h4>Дистанции маршрутов</h4>
          {renderBuckets(productData.routeDistanceBuckets, 'Дистанция')}
        </Section>
      )}

      {activeTab === 'user' && (
        <Section title="User (возраст, логины по часам)">
          {userError && <div className="error-msg">{userError}</div>}
          <h4>Возраст 5-летние</h4>
          {renderBuckets(userData.ageBuckets, 'Возраст')}
          <h4>Логины по часам (30 дней)</h4>
          {renderBuckets(userData.loginByHour, 'Час')}
        </Section>
      )}

      {activeTab === 'order' && (
        <Section title="Order (топы, выручка)">
          {orderError && <div className="error-msg">{orderError}</div>}
          <h4>Топ бренды</h4>
          {renderPie(orderData.topBrands)}
          <h4>Топ категории</h4>
          {renderPie(orderData.topCategories)}
          <h4>Топ товары</h4>
          {renderPie(orderData.topProducts)}
          <h4>Выручка по месяцам (12м)</h4>
          {renderTimeSeries(orderData.revenueByMonth)}
          <h4>Бестселлеры по месяцам</h4>
          {renderTimeSeries(orderData.bestsellersByMonth)}
          <h4>Фильтр по статусу/полу/возрасту/категории/бренду</h4>
          <div className="filters">
            <select
              value={orderFilter.status}
              onChange={(e) => {
                const next = { ...orderFilter, status: e.target.value };
                setOrderFilter(next);
                loadOrderFilter(next);
              }}
            >
              <option value="">Все статусы</option>
              <option value="PROCESSING">PROCESSING</option>
              <option value="IN_TRANSIT">IN_TRANSIT</option>
              <option value="DELIVERED">DELIVERED</option>
              <option value="CANCELLED">CANCELLED</option>
            </select>
            <select
              value={orderFilter.gender}
              onChange={(e) => {
                const next = { ...orderFilter, gender: e.target.value };
                setOrderFilter(next);
                loadOrderFilter(next);
              }}
            >
              <option value="">Пол: любой</option>
              <option value="M">M</option>
              <option value="F">F</option>
              <option value="N">N</option>
            </select>
            <input
              placeholder="Возрастной bucket"
              value={orderFilter.ageGroup}
              onChange={(e) => {
                const next = { ...orderFilter, ageGroup: e.target.value };
                setOrderFilter(next);
                loadOrderFilter(next);
              }}
            />
            <input
              placeholder="Категория ID"
              value={orderFilter.categoryId}
              onChange={(e) => {
                const next = { ...orderFilter, categoryId: e.target.value };
                setOrderFilter(next);
                loadOrderFilter(next);
              }}
              type="number"
            />
            <input
              placeholder="Бренд ID"
              value={orderFilter.brandId}
              onChange={(e) => {
                const next = { ...orderFilter, brandId: e.target.value };
                setOrderFilter(next);
                loadOrderFilter(next);
              }}
              type="number"
            />
            <button
              type="button"
              onClick={() => {
                const next = { status: '', gender: '', ageGroup: '', categoryId: '', brandId: '' };
                setOrderFilter(next);
                loadOrderFilter(next);
              }}
            >
              Очистить фильтр
            </button>
          </div>
          <h4>Бренды (фильтр)</h4>
          {renderPie(orderData.filtered?.brands)}
          <h4>Категории (фильтр)</h4>
          {renderPie(orderData.filtered?.categories)}
          <h4>Товары (фильтр)</h4>
          {renderPie(orderData.filtered?.products)}
        </Section>
      )}

      {activeTab === 'analyze' && (
        <Section title="Analyze (фильтры gender/ageGroup/month)">
          {analyzeError && <div className="error-msg">{analyzeError}</div>}
          <div className="filters">
            <select
              value={analyzeFilters.scope}
              onChange={(e) => {
                const next = { ...analyzeFilters, scope: e.target.value };
                setAnalyzeFilters(next);
                loadAnalyze(next);
              }}
            >
              <option value="products">Товары</option>
              <option value="categories">Категории</option>
              <option value="brands">Бренды</option>
            </select>
            <select
              value={analyzeFilters.gender}
              onChange={(e) => {
                const next = { ...analyzeFilters, gender: e.target.value };
                setAnalyzeFilters(next);
                loadAnalyze(next);
              }}
            >
              <option value="">Пол: любой</option>
              <option value="M">M</option>
              <option value="F">F</option>
              <option value="N">N</option>
            </select>
            <input
              placeholder="Возрастной bucket (например 18-24)"
              value={analyzeFilters.ageGroup}
              onChange={(e) => {
                const next = { ...analyzeFilters, ageGroup: e.target.value };
                setAnalyzeFilters(next);
                loadAnalyze(next);
              }}
            />
            <input
              placeholder="Месяц (1-12)"
              value={analyzeFilters.month}
              onChange={(e) => {
                const next = { ...analyzeFilters, month: e.target.value };
                setAnalyzeFilters(next);
                loadAnalyze(next);
              }}
              type="number"
              min="1"
              max="12"
            />
            <button
              type="button"
              onClick={() => {
                const next = { scope: 'products', gender: '', ageGroup: '', month: '' };
                setAnalyzeFilters(next);
                loadAnalyze(next);
              }}
            >
              Очистить фильтр
            </button>
          </div>
          {renderPie(analyzeData)}
        </Section>
      )}
    </div>
  );
}

export default Analytics;




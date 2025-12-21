import React, { useState, useEffect, useCallback } from 'react';
import { profileService, orderService } from '../services/api';
import './Profile.css';

const renderCustomerStatus = (detail) => {
  const status = (detail?.order?.status?.statusName || detail?.statusName || '').toUpperCase();
  const wh = detail?.order?.sourceWarehouse;
  if (status === 'DELIVERED') {
    return (
      <>
        <p><strong>Статус:</strong> Товар доставлен</p>
        {wh && (
          <p><strong>Склад:</strong> {wh.cityName || '—'}, {wh.address || '—'}, {wh.warehouseName || '—'}</p>
        )}
      </>
    );
  }
  if (status === 'IN_TRANSIT') {
    return <p><strong>Статус:</strong> Заказ в пути</p>;
  }
  if (status === 'PROCESSING') {
    return <p><strong>Статус:</strong> Заказ обрабатывается на складе</p>;
  }
  if (status === 'CANCELLED') {
    return <p><strong>Статус:</strong> Заказ отменён</p>;
  }
  return <p><strong>Статус:</strong> {detail?.order?.status?.statusName || detail?.statusName || '—'}</p>;
};

function Profile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [orders, setOrders] = useState([]);
  const [orderDetails, setOrderDetails] = useState({});
  const [orderLoading, setOrderLoading] = useState({});
  const [edit, setEdit] = useState(false);
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    phoneNumber: '',
    dateOfBirth: '',
    gender: '',
    cityId: '',
    profilePictureUrl: '',
  });
  const [cities, setCities] = useState([]);
  const userId = localStorage.getItem('userId');

  const loadProfile = useCallback(async () => {
    setLoading(true);
    try {
      const response = await profileService.get(userId);
      setProfile(response.data);
      setForm({
        firstName: response.data.firstName || '',
        lastName: response.data.lastName || '',
        phoneNumber: response.data.phoneNumber || '',
        dateOfBirth: response.data.dateOfBirth || '',
        gender: response.data.gender || '',
        cityId: response.data.cityId || '',
        profilePictureUrl: response.data.profilePictureUrl || '',
      });
    } catch (error) {
      console.error('Error loading profile:', error);
    } finally {
      setLoading(false);
    }
  }, [userId]);

  const loadOrders = useCallback(async () => {
    try {
      const res = await orderService.getUserOrders(userId, 0, 50);
      const list = Array.isArray(res.data?.orders)
        ? res.data.orders
        : Array.isArray(res.data?.content)
          ? res.data.content
          : Array.isArray(res.data)
            ? res.data
            : [];
      setOrders(list);
    } catch (e) {
      console.error('Error loading orders:', e);
      setOrders([]);
    }
  }, [userId]);

  const toggleOrder = async (id) => {
    setOrderDetails((prev) => ({ ...prev, [id]: prev[id] || null }));
    if (orderDetails[id]) return; // уже загружено
    setOrderLoading((prev) => ({ ...prev, [id]: true }));
    try {
      const res = await orderService.getById(id);
      setOrderDetails((prev) => ({ ...prev, [id]: res.data }));
    } catch (e) {
      console.error('Order details error', e);
      setOrderDetails((prev) => ({ ...prev, [id]: { error: true } }));
    } finally {
      setOrderLoading((prev) => ({ ...prev, [id]: false }));
    }
  };

  useEffect(() => {
    if (userId) {
      loadProfile();
      loadOrders();
      profileService.getCities().then(res => setCities(res.data || [])).catch(() => setCities([]));
    }
  }, [userId, loadProfile, loadOrders]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSave = async () => {
    try {
      await profileService.update(userId, {
        ...profile,
        ...form,
        dateOfBirth: form.dateOfBirth || profile.dateOfBirth || null,
        city: form.cityId ? { cityId: Number(form.cityId) } : null,
      });
      setEdit(false);
      loadProfile();
    } catch (e) {
      console.error('Error updating profile:', e);
    }
  };

  if (loading) return <div>Загрузка...</div>;
  if (!profile) return <div>Профиль не найден</div>;

  return (
    <div className="profile">
      <h1>Профиль</h1>

      <details open>
        <summary>Мои заказы</summary>
        <div className="orders">
          {orders.length === 0 && <p>Заказов пока нет</p>}
          {orders.map((o) => (
            <div key={o.orderId || o.id} className="order-card">
              <div className="order-row">
                <span>{o.createdAt || o.orderDate || ''}</span>
                <span>{o.statusName || o.status?.statusName || o.status || '—'}</span>
                <span>{(o.totalAmount || o.total || 0) + ' ₽'}</span>
                <button type="button" onClick={() => toggleOrder(o.orderId || o.id)}>
                  {orderDetails[o.orderId || o.id] ? 'Свернуть' : 'Подробнее'}
                </button>
              </div>
              {orderDetails[o.orderId || o.id] && (
                <div className="order-details">
                  {orderLoading[o.orderId || o.id] && <p>Загрузка...</p>}
                  {orderDetails[o.orderId || o.id]?.items && orderDetails[o.orderId || o.id]?.items.map((it) => (
                    <div key={it.orderItemId} className={`order-item status-${(orderDetails[o.orderId || o.id]?.order?.status?.statusName || orderDetails[o.orderId || o.id]?.statusName || 'unk').toLowerCase()}`}>
                      <img src={it.product?.mainImageUrl || '/placeholder.png'} alt={it.product?.name} />
                      <div>
                        <p>{it.product?.name}</p>
                        <p>Количество: {it.quantity}</p>
                        <p>Цена: {it.unitPrice} ₽</p>
                      </div>
                    </div>
                  ))}
                  <div className="order-meta">
                    <p><strong>Адрес доставки:</strong> {orderDetails[o.orderId || o.id]?.order?.shippingAddressText || orderDetails[o.orderId || o.id]?.shippingAddressText || '—'}</p>
                    {renderCustomerStatus(orderDetails[o.orderId || o.id])}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      </details>

      <details>
        <summary>Личные данные</summary>
        <div className="profile-info">
          <p><strong>Имя:</strong> {profile.firstName} {profile.lastName}</p>
          <p><strong>Email:</strong> {profile.user?.email}</p>
          <p><strong>Телефон:</strong> {profile.phoneNumber || 'Не указан'}</p>
          <p><strong>Дата рождения:</strong> {profile.dateOfBirth || 'Не указана'}</p>
          <p><strong>Пол:</strong> {profile.gender || 'Не указан'}</p>
          <p><strong>Город:</strong> {profile.cityName || 'Не указан'}</p>
          {profile.profilePictureUrl && (
            <div className="profile-picture">
              <img src={profile.profilePictureUrl} alt="profile" />
            </div>
          )}
          <div className="actions">
            <button type="button" onClick={() => setEdit((v) => !v)}>
              {edit ? 'Скрыть редактирование' : 'Редактировать'}
            </button>
          </div>
          {edit && (
            <div className="edit-form">
              <label>
                Имя
                <input name="firstName" value={form.firstName} onChange={handleChange} />
              </label>
              <label>
                Фамилия
                <input name="lastName" value={form.lastName} onChange={handleChange} />
              </label>
              <label>
                Телефон
                <input name="phoneNumber" value={form.phoneNumber} onChange={handleChange} />
              </label>
              <label>
                Дата рождения
                <input type="date" name="dateOfBirth" value={form.dateOfBirth} onChange={handleChange} />
              </label>
              <label>
                Пол (M/F/N)
                <select name="gender" value={form.gender} onChange={handleChange}>
                  <option value="">Не указан</option>
                  <option value="M">M</option>
                  <option value="F">F</option>
                  <option value="N">N</option>
                </select>
              </label>
              <label>
                Город
                <select name="cityId" value={form.cityId} onChange={handleChange}>
                  <option value="">Не указан</option>
                  {cities.map((c) => (
                    <option key={c.cityId} value={c.cityId}>
                      {c.cityName} (ID {c.cityId})
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Фото (файл)
                <input
                  type="file"
                  accept="image/*"
                  onChange={async (e) => {
                    const file = e.target.files?.[0];
                    if (!file) return;
                    try {
                      const res = await profileService.uploadAvatar(userId, file);
                      if (res.data?.url) {
                        setForm((prev) => ({ ...prev, profilePictureUrl: res.data.url }));
                        localStorage.setItem('profileAvatar', res.data.url);
                        window.dispatchEvent(new Event('profileAvatarUpdated'));
                      }
                    } catch (err) {
                      console.error('upload avatar error', err);
                    }
                  }}
                />
              </label>
              <div className="actions">
                <button type="button" onClick={handleSave}>Сохранить</button>
                <button type="button" onClick={() => { setEdit(false); setForm({
                  firstName: profile.firstName || '',
                  lastName: profile.lastName || '',
                  phoneNumber: profile.phoneNumber || '',
                  dateOfBirth: profile.dateOfBirth || '',
                  gender: profile.gender || '',
                  cityId: profile.cityId || '',
                  profilePictureUrl: profile.profilePictureUrl || '',
                }); }}>Отмена</button>
              </div>
            </div>
          )}
        </div>
      </details>
    </div>
  );
}

export default Profile;




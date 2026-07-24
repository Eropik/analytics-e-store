import React, { useState, useEffect, useCallback } from 'react';
import { profileService, orderService,authService } from '../services/api';
import './Profile.css';
import { useNavigate } from 'react-router-dom';
import { translateGender, translateOrderStatus } from '../utils/enumTranslations';
import { formatOrderDateTime } from '../utils/formatOrderDate';

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
  return <p><strong>Статус:</strong> {translateOrderStatus(detail?.order?.status?.statusName || detail?.statusName)}</p>;
};

function Profile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [orders, setOrders] = useState([]);
  const [orderDetails, setOrderDetails] = useState({});
  const [orderLoading, setOrderLoading] = useState({});
  const [priceDetailsOpen, setPriceDetailsOpen] = useState({});
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
  const userEmail = localStorage.getItem('userEmail');
 
  

  const loadProfile = useCallback(async () => {
    setLoading(true);
    try {
      const response = await profileService.get(userId);
       console.log("Загруженный профиль:", response.data);
      setProfile(response.data);
      setForm({
        firstName: response.data.firstName || '',
        lastName: response.data.lastName || '',
        phoneNumber: response.data.phoneNumber || '',
        dateOfBirth: response.data.dateOfBirth || '',
        gender:
          response.data.gender === 'M' || response.data.gender === 'F'
            ? response.data.gender
            : '',
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
      const cityEmpty = form.cityId === '' || form.cityId == null;
      const genderEmpty = form.gender === '' || form.gender == null;
      await profileService.update(userId, {
        firstName: form.firstName,
        lastName: form.lastName,
        phoneNumber: form.phoneNumber || null,
        dateOfBirth: form.dateOfBirth || null,
        profilePictureUrl: form.profilePictureUrl || profile.profilePictureUrl || null,
        ...(cityEmpty ? { clearCity: true } : { cityId: Number(form.cityId) }),
        ...(genderEmpty ? { clearGender: true } : { gender: form.gender }),
      });
      setEdit(false);
      loadProfile();
    } catch (e) {
      console.error('Error updating profile:', e);
    }
  };

  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await authService.logout();
    } catch (error) {
      console.error('Ошибка при выходе:', error);
    } finally {
      localStorage.removeItem('userId');
      localStorage.removeItem('userEmail');
      localStorage.removeItem('role');
      localStorage.removeItem('profileAvatar');
      window.dispatchEvent(new Event('authChanged'));
      navigate('/login');
    }};

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
                <span>{formatOrderDateTime(o.createdAt || o.orderDate)}</span>
                <span>{translateOrderStatus(o.statusName || o.status?.statusName || o.status)}</span>
                <span>{(o.totalAmount || o.total || 0) + ' р.'}</span>
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
                        <p>Цена: {it.unitPrice} р.</p>
                      </div>
                    </div>
                  ))}
                  <div className="order-meta">
                    <p><strong>Адрес доставки:</strong> {orderDetails[o.orderId || o.id]?.order?.shippingAddressText || orderDetails[o.orderId || o.id]?.shippingAddressText || '—'}</p>
                    {renderCustomerStatus(orderDetails[o.orderId || o.id])}
                    <p
                      className="order-price-main"
                      onClick={() => setPriceDetailsOpen((prev) => ({ ...prev, [o.orderId || o.id]: !prev[o.orderId || o.id] }))}
                    >
                      <strong>Итоговая цена:</strong> {orderDetails[o.orderId || o.id]?.pricing?.finalTotal || o.totalAmount || o.total || 0} р.
                    </p>
                    {priceDetailsOpen[o.orderId || o.id] && (
                      <div className="order-price-breakdown">
                        <p>Базовая цена: {orderDetails[o.orderId || o.id]?.pricing?.baseTotal || 0} р.</p>
                        <p className="discount-note">
                          В заказе {orderDetails[o.orderId || o.id]?.pricing?.itemsCount || 0} товаров, скидка: {orderDetails[o.orderId || o.id]?.pricing?.discountPercent || 0}% ({orderDetails[o.orderId || o.id]?.pricing?.discountValue || 0} р.)
                        </p>
                        {(orderDetails[o.orderId || o.id]?.pricing?.deliveryPercent || 0) > 0 ? (
                          <p className="delivery-note">
                            Дополнительная плата за доставку: {orderDetails[o.orderId || o.id]?.pricing?.deliveryPercent || 0}% ({orderDetails[o.orderId || o.id]?.pricing?.deliveryExtraValue || 0} р.) при расстоянии {orderDetails[o.orderId || o.id]?.pricing?.deliveryDistanceKm || 0} км
                          </p>
                        ) : (
                          <p className="delivery-note">Самовывоз: дополнительной платы за доставку нет.</p>
                        )}
                      </div>
                    )}
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
          <p><strong>Email:</strong> {profile.email || userEmail}</p>
          <button className="logout-btn" onClick={handleLogout}>
          Выйти из аккаунта
          </button>
          <p><strong>Телефон:</strong> {profile.phoneNumber || 'Не указан'}</p>
          <p><strong>Дата рождения:</strong> {profile.dateOfBirth || 'Не указана'}</p>
          <p><strong>Пол:</strong> {translateGender(profile.gender)}</p>
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
                  <option value="M">Мужской</option>
                  <option value="F">Женский</option>
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
                  gender:
                    profile.gender === 'M' || profile.gender === 'F'
                      ? profile.gender
                      : '',
                  cityId: profile.cityId ?? '',
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




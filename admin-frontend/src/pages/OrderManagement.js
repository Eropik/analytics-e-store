import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { orderService, warehouseService } from '../services/api';
import './OrderManagement.css';

function OrderManagement() {
  const STATUS_OPTIONS = [
    { id: 1, name: 'PROCESSING' },
    { id: 2, name: 'IN_TRANSIT' },
    { id: 3, name: 'DELIVERED' },
    { id: 4, name: 'CANCELLED' },
  ];
  const STATUS_PRIORITY = {
    PROCESSING: 1,
    IN_TRANSIT: 2,
    DELIVERED: 3,
    CANCELLED: 4,
  };

  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');
  const [detailModal, setDetailModal] = useState({ open: false, loading: false, order: null, items: [], error: '' });
  const [warehouses, setWarehouses] = useState([]);
  const [logistics, setLogistics] = useState({ warehouseId: '', deliveryDate: '', statusId: '' });
  const [showUser, setShowUser] = useState(false);
  const [userInfo, setUserInfo] = useState(null);
  const navigate = useNavigate();
  const adminUserId = localStorage.getItem('adminUserId');

  useEffect(() => {
    if (!adminUserId) {
      navigate('/login');
      return;
    }
    loadOrders();
  }, [adminUserId, navigate]);

  const loadOrders = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const response = await orderService.getAll(0, 20, adminUserId);
      const list = Array.isArray(response.data?.orders)
        ? response.data.orders
        : Array.isArray(response.data?.content)
          ? response.data.content
          : Array.isArray(response.data)
            ? response.data
            : [];
      setOrders(list);
    } catch (error) {
      console.error('Error loading orders:', error);
      setErrorMsg(error.response?.data?.error || 'Нет доступа: требуется отдел ORDER_MANAGE / неверный adminUserId');
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (orderId, newStatusId) => {
    try {
      await orderService.updateStatus(orderId, newStatusId, adminUserId);
      loadOrders();
    } catch (error) {
      console.error('Error updating order status:', error);
    }
  };

  const openDetails = async (order) => {
    setDetailModal({ open: true, loading: true, order: null, items: [], error: '' });
    setShowUser(false);
    setUserInfo(null);
    try {
      const [detailRes, whRes] = await Promise.all([
        orderService.getById(order.id || order.orderId, adminUserId),
        warehouses.length ? Promise.resolve({ data: warehouses }) : warehouseService.getAll(adminUserId)
      ]);
      const detail = detailRes.data?.order || detailRes.data?.orderDto || detailRes.data || {};
      const items = detailRes.data?.items || [];
      const whs = warehouses.length ? warehouses : (Array.isArray(whRes.data) ? whRes.data : []);
      setWarehouses(whs);
      setDetailModal({ open: true, loading: false, order: detail, items, error: '' });
      setLogistics({
        warehouseId: detail?.sourceWarehouse?.warehouseId || detail?.sourceWarehouse?.id || '',
        deliveryDate: detail?.actualDeliveryDate ? detail.actualDeliveryDate.slice(0,10) : '',
        statusId: detail?.statusId || detail?.status?.statusId || '',
      });

      const uid = detail?.user?.userId || detail?.userId;
      if (uid) {
        try {
          const ures = await orderService.getUserBasic(uid, adminUserId);
          setUserInfo(ures.data);
        } catch (err) {
          // ignore fetch user errors inside modal
        }
      }
    } catch (e) {
      setDetailModal({ open: true, loading: false, order: null, items: [], error: e.response?.data?.error || 'Ошибка загрузки заказа' });
    }
  };

  const closeDetails = () => setDetailModal({ open: false, loading: false, order: null, items: [], error: '' });

  const saveLogistics = async () => {
    if (!detailModal.order) return;
    try {
      const fallbackDate = detailModal.order.actualDeliveryDate
        ? detailModal.order.actualDeliveryDate.slice(0, 10)
        : undefined;
      await orderService.updateLogistics(detailModal.order.orderId, adminUserId, {
        warehouseId: logistics.warehouseId ? Number(logistics.warehouseId) : undefined,
        deliveryDate: logistics.deliveryDate || fallbackDate || undefined,
        statusId: logistics.statusId ? Number(logistics.statusId) : undefined,
      });
      await loadOrders();
      closeDetails();
    } catch (e) {
      setDetailModal((prev) => ({ ...prev, error: e.response?.data?.error || 'Ошибка сохранения логистики' }));
    }
  };

  if (loading) return <div>Загрузка...</div>;

  return (
    <div className="order-management">
      <h1>Управление заказами</h1>
      {errorMsg && <div className="error-msg">{errorMsg}</div>}
      <table className="orders-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Дата</th>
            <th>Статус</th>
            <th>Сумма</th>
            <th>Действия</th>
          </tr>
        </thead>
        <tbody>
          {[...orders]
            .sort((a, b) => {
              const sa = (a.statusName || a.status?.statusName || a.status || '').toUpperCase();
              const sb = (b.statusName || b.status?.statusName || b.status || '').toUpperCase();
              return (STATUS_PRIORITY[sa] || 99) - (STATUS_PRIORITY[sb] || 99);
            })
            .map(order => (
            <tr key={order.id || order.orderId} className={`status-${(order.statusName || order.status)?.toLowerCase()}`}>
              <td>{order.id || order.orderId}</td>
              <td>{order.orderDate ? new Date(order.orderDate).toLocaleDateString() : order.createdAt || ''}</td>
              <td>
                <span className={`status-text status-${(order.statusName || order.status?.statusName || order.status || '').toLowerCase()}`}>
                  {order.statusName || order.status?.statusName || order.status || '—'}
                </span>
              </td>
              <td>{(order.totalAmount || order.total || 0)} ₽</td>
              <td>
                <button className="details-btn" onClick={() => openDetails(order)}>Детали</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {detailModal.open && (
        <div className="modal-backdrop" onClick={closeDetails}>
          <div className="modal wide" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Заказ {detailModal.order?.orderId}</h3>
              <button onClick={closeDetails}>×</button>
            </div>
            {detailModal.loading && <p>Загрузка...</p>}
            {detailModal.error && <p className="error-msg">{detailModal.error}</p>}
            {!detailModal.loading && detailModal.order && (
              <>
                <div className="order-info">
                  <div>
                    <p><strong>Email:</strong> {detailModal.order.userEmail || detailModal.order.user?.email || '—'}</p>
                    <p><strong>Город доставки:</strong> {detailModal.order.shippingCityName || '—'}</p>
                    <p><strong>Адрес доставки:</strong> {detailModal.order.shippingAddressText || '—'}</p>
                    <p><strong>Сумма:</strong> {detailModal.order.totalAmount} ₽</p>
                    <p><strong>Статус:</strong> <span className={`status-text status-${(detailModal.order.statusName || '').toLowerCase()}`}>{detailModal.order.statusName}</span></p>
                    {detailModal.order.deliveryMethod && (
                      <>
                        <p><strong>Способ доставки:</strong> {detailModal.order.deliveryMethod.methodName}</p>
                        {detailModal.order.deliveryMethod.description && (
                          <p className="method-description"><em>{detailModal.order.deliveryMethod.description}</em></p>
                        )}
                      </>
                    )}
                    {detailModal.order.paymentMethod && (
                      <>
                        <p><strong>Способ оплаты:</strong> {detailModal.order.paymentMethod.methodName}</p>
                        {detailModal.order.paymentMethod.description && (
                          <p className="method-description"><em>{detailModal.order.paymentMethod.description}</em></p>
                        )}
                      </>
                    )}
                  </div>
                  <div className="warehouse-info">
                    {detailModal.order.sourceWarehouse && (
                      <>
                        <p><strong>Склад:</strong> {detailModal.order.sourceWarehouse.warehouseName} ({detailModal.order.sourceWarehouse.cityName || ''})</p>
                        <p><strong>Адрес склада:</strong> {detailModal.order.sourceWarehouse.address || '—'}</p>
                        {detailModal.order.shippingCityName && (
                          <p><strong>Город заказчика:</strong> {detailModal.order.shippingCityName}</p>
                        )}
                        <p><strong>Дистанция:</strong> {
                          detailModal.order.distanceKm !== null && detailModal.order.distanceKm !== undefined
                            ? `${detailModal.order.distanceKm} км`
                            : detailModal.order.shippingCityName ? 'Маршрут не найден' : '—'
                        }</p>
                        {detailModal.order.routePath && (
                          <p><strong>Маршрут:</strong> {detailModal.order.routePath}</p>
                        )}
                      </>
                    )}
                  </div>
                </div>

                {detailModal.items && detailModal.items.length > 0 && (
                  <div className="items-grid">
                    {detailModal.items.map((it) => (
                      <div key={it.orderItemId} className="item-card">
                        <div className="item-name">{it.product?.name}</div>
                        <div>Qty: {it.quantity}</div>
                        <div>Цена: {it.unitPrice} ₽</div>
                      </div>
                    ))}
                  </div>
                )}

                <details className="user-inline" open={showUser}>
                  <summary onClick={() => setShowUser((v) => !v)}>
                    Пользователь
                  </summary>
                  <div className="user-details">
                    <p><strong>Email:</strong> {userInfo?.email || detailModal.order.user?.email || detailModal.order.userEmail || '—'}</p>
                    <p><strong>Роль:</strong> {userInfo?.role?.roleName || detailModal.order.user?.role?.roleName || detailModal.order.role || '—'}</p>
                    <p><strong>Имя:</strong> {userInfo?.firstName || detailModal.order.user?.firstName || '—'} {userInfo?.lastName || detailModal.order.user?.lastName || ''}</p>
                    <p><strong>Телефон:</strong> {userInfo?.phoneNumber || detailModal.order.user?.phoneNumber || '—'}</p>
                    <p><strong>Город:</strong> {userInfo?.cityName || detailModal.order.user?.cityName || '—'}</p>
                    <p><strong>Пол:</strong> {userInfo?.gender || detailModal.order.user?.gender || '—'}</p>
                    {userInfo?.department && (
                      <p><strong>Отдел:</strong> {userInfo.department}</p>
                    )}
                  </div>
                </details>

                <div className="logistics">
                  <label>
                    Статус заказа
                    <select
                      value={logistics.statusId}
                      onChange={(e) => setLogistics((p) => ({ ...p, statusId: e.target.value }))}
                    >
                      <option value="">Без изменений</option>
                      {STATUS_OPTIONS.map((s) => (
                        <option key={s.id} value={s.id}>{s.name}</option>
                      ))}
                    </select>
                  </label>

                  {detailModal.order.statusName === 'PROCESSING' && (
                    <>
                      <label>
                        Склад
                        <select
                          value={logistics.warehouseId}
                          onChange={(e) => setLogistics((p) => ({ ...p, warehouseId: e.target.value }))}
                        >
                          <option value="">Не выбран</option>
                          {warehouses.map((w) => (
                            <option key={w.id || w.warehouseId} value={w.id || w.warehouseId}>
                              {(w.name || w.warehouseName || '—')} ({w.city?.cityName || w.cityName || '—'})
                            </option>
                          ))}
                        </select>
                      </label>
                      <label>
                        Дата доставки
                        <input
                          type="date"
                          value={logistics.deliveryDate}
                          onChange={(e) => setLogistics((p) => ({ ...p, deliveryDate: e.target.value }))}
                        />
                      </label>
                      <div className="logistics-actions">
                        <button className="btn-success" onClick={saveLogistics}>Сохранить</button>
                        <button className="btn-danger" onClick={() => setLogistics((p) => ({ ...p, statusId: '4' }))}>Отменить</button>
                      </div>
                    </>
                  )}

                  {detailModal.order.statusName === 'IN_TRANSIT' && (
                    <>
                      <label>
                        Обновить дату доставки
                        <input
                          type="date"
                          value={logistics.deliveryDate}
                          onChange={(e) => setLogistics((p) => ({ ...p, deliveryDate: e.target.value }))}
                        />
                      </label>
                      <div className="logistics-actions">
                        <button className="btn-success" onClick={saveLogistics}>Сохранить</button>
                        <button className="btn-danger" onClick={() => { setLogistics((p) => ({ ...p, statusId: '4' })); saveLogistics(); }}>Отменить</button>
                      </div>
                    </>
                  )}

                  {detailModal.order.statusName === 'CANCELLED' && (
                    <div className="info-block">
                      <p>Заказ отменён. Доступна только информация.</p>
                    </div>
                  )}

                  {detailModal.order.statusName === 'DELIVERED' && (
                    <div className="info-block">
                      <p><strong>Дата заказа:</strong> {detailModal.order.orderDate ? new Date(detailModal.order.orderDate).toLocaleDateString() : '—'}</p>
                      <p><strong>Дата доставки:</strong> {detailModal.order.actualDeliveryDate ? new Date(detailModal.order.actualDeliveryDate).toLocaleDateString() : '—'}</p>
                      {detailModal.order.sourceWarehouse && (
                        <p><strong>Склад:</strong> {detailModal.order.sourceWarehouse.warehouseName} ({detailModal.order.sourceWarehouse.cityName || '—'})</p>
                      )}
                      {detailModal.order.distanceKm && (
                        <p><strong>Дистанция:</strong> {detailModal.order.distanceKm} км</p>
                      )}
                    </div>
                  )}
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default OrderManagement;




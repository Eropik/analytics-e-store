import React, { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { orderService, warehouseService } from '../services/api';
import './OrderManagement.css';
import {
  formatAccessDenied,
  translateDepartment,
  translateDeliveryMethod,
  translateGender,
  translateOrderStatus,
  translatePaymentMethod,
  translateRole
} from '../utils/enumTranslations';
import { toastSuccess } from '../utils/toastBus';

/** Суффикс для классов статуса (совпадает с таблицей заказов) */
function orderStatusSlug(name) {
  return String(name || '').toLowerCase();
}

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
  const [statusMenuOpen, setStatusMenuOpen] = useState(false);
  const statusDropdownRef = useRef(null);
  const navigate = useNavigate();
  const adminUserId = localStorage.getItem('adminUserId');

  const warehouseSelectItems = useMemo(
    () =>
      warehouses.map((w) => {
        const wid = w.id ?? w.warehouseId;
        const title = w.name || w.warehouseName || 'Склад';
        const city = w.city?.cityName || w.cityName || '';
        return {
          id: wid,
          label: city ? `${title} (${city})` : title,
        };
      }),
    [warehouses]
  );

  const loadOrders = useCallback(async () => {
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
      setErrorMsg(formatAccessDenied(error.response?.data?.error || 'Нет доступа: требуется отдел ORDER_MANAGE / неверный adminUserId'));
    } finally {
      setLoading(false);
    }
  }, [adminUserId]);

  useEffect(() => {
    if (!detailModal.open) setStatusMenuOpen(false);
  }, [detailModal.open]);

  useEffect(() => {
    if (!statusMenuOpen) return;
    const onDoc = (e) => {
      if (statusDropdownRef.current && !statusDropdownRef.current.contains(e.target)) {
        setStatusMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', onDoc);
    return () => document.removeEventListener('mousedown', onDoc);
  }, [statusMenuOpen]);
  useEffect(() => {
    if (!adminUserId) {
      navigate('/login');
      return;
    }
    loadOrders();
  }, [adminUserId, navigate, loadOrders]);

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
      toastSuccess('Успешно обновлено!');
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
                  {translateOrderStatus(order.statusName || order.status?.statusName || order.status)}
                </span>
              </td>
              <td>{(order.totalAmount || order.total || 0)} р.</td>
              <td>
                <button className="details-btn" onClick={() => openDetails(order)}>Детали</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {detailModal.open && (
        <div className="modal-backdrop admin-order-modal-backdrop" onClick={closeDetails}>
          <div className="modal wide admin-order-modal" onClick={(e) => e.stopPropagation()}>
            <div className="admin-order-modal__accent" aria-hidden />
            <header className="admin-order-modal__topbar">
              {detailModal.loading ? (
                <h3 className="admin-order-modal__title-plain">Загрузка заказа…</h3>
              ) : detailModal.order ? (
                <div className="admin-order-modal__hero">
                  <div className="admin-order-modal__hero-text">
                    <p className="admin-order-modal__eyebrow">Детали заказа</p>
                    <h3 className="admin-order-modal__order-no">№ {detailModal.order.orderId}</h3>
                    <p className="admin-order-modal__date-sum">
                      {detailModal.order.orderDate
                        ? new Date(detailModal.order.orderDate).toLocaleString('ru-RU', {
                            day: 'numeric',
                            month: 'long',
                            year: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit',
                          })
                        : '—'}
                      <span className="admin-order-modal__dot" aria-hidden>
                        ·
                      </span>
                      <strong>{detailModal.order.totalAmount} р.</strong>
                    </p>
                  </div>
                  <div className="admin-order-modal__hero-aside">
                    <span
                      className={`admin-order-chip status-${(detailModal.order.statusName || '').toLowerCase()}`}
                    >
                      {translateOrderStatus(detailModal.order.statusName)}
                    </span>
                  </div>
                </div>
              ) : (
                <h3 className="admin-order-modal__title-plain">Заказ</h3>
              )}
              <button
                type="button"
                className="admin-order-modal__close"
                onClick={closeDetails}
                aria-label="Закрыть"
              >
                ×
              </button>
            </header>

            {detailModal.loading && <p className="admin-order-modal__loading-msg">Подождите…</p>}
            {detailModal.error && (
              <div className="admin-order-modal__error-banner error-msg">{detailModal.error}</div>
            )}
            {!detailModal.loading && detailModal.order && (
              <>
                <div className="logistics admin-order-logistics admin-order-logistics--priority">
                  <h4 className="admin-order-logistics__title">Управление логистикой</h4>
                  <div className="admin-order-logistics__status-row">
                    <span className="logistics-field-label" id="admin-order-status-lbl">
                      Статус заказа
                    </span>
                    <div ref={statusDropdownRef} className={`admin-order-status-picker${statusMenuOpen ? ' is-open' : ''}`}>
                      <button
                        type="button"
                        className="admin-order-status-picker__trigger"
                        aria-labelledby="admin-order-status-lbl"
                        aria-expanded={statusMenuOpen}
                        aria-haspopup="listbox"
                        onClick={() => setStatusMenuOpen((o) => !o)}
                      >
                        {!logistics.statusId ? (
                          <span className="admin-order-status-picker__neutral">
                            Не менять (как сохранено)
                          </span>
                        ) : (
                          (() => {
                            const sel = STATUS_OPTIONS.find(
                              (x) => String(x.id) === String(logistics.statusId)
                            );
                            if (!sel) {
                              return <span className="admin-order-status-picker__neutral">{logistics.statusId}</span>;
                            }
                            return (
                              <span
                                className={`admin-order-chip admin-order-chip--trigger status-${orderStatusSlug(
                                  sel.name
                                )}`}
                              >
                                {translateOrderStatus(sel.name)}
                              </span>
                            );
                          })()
                        )}
                        <span className="admin-order-status-picker__caret" aria-hidden>
                          ▼
                        </span>
                      </button>
                      {statusMenuOpen && (
                        <ul className="admin-order-status-picker__menu" role="listbox">
                          <li role="presentation">
                            <button
                              type="button"
                              role="option"
                              className="admin-order-status-picker__menu-item admin-order-status-picker__menu-item--neutral"
                              onClick={() => {
                                setLogistics((p) => ({ ...p, statusId: '' }));
                                setStatusMenuOpen(false);
                              }}
                            >
                              Не менять
                            </button>
                          </li>
                          {STATUS_OPTIONS.map((s) => (
                            <li key={s.id} role="presentation">
                              <button
                                type="button"
                                role="option"
                                className={`admin-order-status-picker__menu-item status-strip-${orderStatusSlug(s.name)}`}
                                onClick={() => {
                                  setLogistics((p) => ({
                                    ...p,
                                    statusId: String(s.id),
                                  }));
                                  setStatusMenuOpen(false);
                                }}
                              >
                                <span
                                  className={`admin-order-chip admin-order-chip--menu status-${orderStatusSlug(
                                    s.name
                                  )}`}
                                >
                                  {translateOrderStatus(s.name)}
                                </span>
                              </button>
                            </li>
                          ))}
                        </ul>
                      )}
                    </div>
                  </div>

                  {detailModal.order.statusName === 'PROCESSING' && (
                    <>
                      <label className="admin-order-logistics__field admin-order-logistics__field--warehouse">
                        <span className="logistics-field-label">Склад отправки</span>
                        <select
                          className="admin-order-warehouse-select"
                          value={
                            logistics.warehouseId !== undefined && logistics.warehouseId !== null
                              ? String(logistics.warehouseId)
                              : ''
                          }
                          onChange={(e) =>
                            setLogistics((p) => ({ ...p, warehouseId: e.target.value }))
                          }
                        >
                          <option value="">— Выберите склад —</option>
                          {warehouseSelectItems.map((item) => (
                            <option key={String(item.id)} value={String(item.id)}>
                              {item.label}
                            </option>
                          ))}
                        </select>
                        <span className="admin-order-logistics__field-hint">
                          Встроенное окно выбора — не обрезается в карточке, удобно при многих складах.
                        </span>
                      </label>
                      <label className="admin-order-logistics__field">
                        <span className="logistics-field-label">Дата доставки</span>
                        <input
                          type="date"
                          value={logistics.deliveryDate}
                          onChange={(e) =>
                            setLogistics((p) => ({ ...p, deliveryDate: e.target.value }))
                          }
                        />
                      </label>
                      <div className="logistics-actions">
                        <button className="btn-success admin-order-logistics__btn" onClick={saveLogistics}>
                          Сохранить
                        </button>
                        <button
                          type="button"
                          className="btn-danger admin-order-logistics__btn"
                          onClick={() => setLogistics((p) => ({ ...p, statusId: '4' }))}
                        >
                          Отменить
                        </button>
                      </div>
                    </>
                  )}

                  {detailModal.order.statusName === 'IN_TRANSIT' && (
                    <>
                      <label className="admin-order-logistics__field">
                        <span className="logistics-field-label">Обновить дату доставки</span>
                        <input
                          type="date"
                          value={logistics.deliveryDate}
                          onChange={(e) =>
                            setLogistics((p) => ({ ...p, deliveryDate: e.target.value }))
                          }
                        />
                      </label>
                      <div className="logistics-actions">
                        <button className="btn-success admin-order-logistics__btn" onClick={saveLogistics}>
                          Сохранить
                        </button>
                        <button
                          type="button"
                          className="btn-danger admin-order-logistics__btn"
                          onClick={() => {
                            setLogistics((p) => ({ ...p, statusId: '4' }));
                            saveLogistics();
                          }}
                        >
                          Отменить заказ
                        </button>
                      </div>
                    </>
                  )}

                  {detailModal.order.statusName === 'CANCELLED' && (
                    <div className="info-block admin-order-note">
                      <p>Заказ отменён. Доступна только информация.</p>
                    </div>
                  )}

                  {detailModal.order.statusName === 'DELIVERED' && (
                    <div className="info-block admin-order-note admin-order-note--success">
                      <p>
                        <strong>Завершён</strong> · дата заказа:{' '}
                        {detailModal.order.orderDate
                          ? new Date(detailModal.order.orderDate).toLocaleDateString('ru-RU')
                          : '—'}
                      </p>
                      <p>
                        Доставка:{' '}
                        {detailModal.order.actualDeliveryDate
                          ? new Date(detailModal.order.actualDeliveryDate).toLocaleDateString('ru-RU')
                          : '—'}
                      </p>
                      {detailModal.order.sourceWarehouse && (
                        <p>
                          Склад: {detailModal.order.sourceWarehouse.warehouseName} (
                          {detailModal.order.sourceWarehouse.cityName || '—'})
                        </p>
                      )}
                      {detailModal.order.distanceKm != null && (
                        <p>Дистанция: {detailModal.order.distanceKm} км</p>
                      )}
                    </div>
                  )}
                </div>

                <div className="admin-order-cards">
                  <section className="admin-order-panel">
                    <h4 className="admin-order-panel__title">Доставка и клиент</h4>
                    <dl className="admin-order-fields">
                      <div className="admin-order-field">
                        <dt>Email</dt>
                        <dd>{detailModal.order.userEmail || detailModal.order.user?.email || '—'}</dd>
                      </div>
                      <div className="admin-order-field admin-order-field--wide">
                        <dt>Город доставки</dt>
                        <dd>{detailModal.order.shippingCityName || '—'}</dd>
                      </div>
                      <div className="admin-order-field admin-order-field--full">
                        <dt>Адрес</dt>
                        <dd>{detailModal.order.shippingAddressText || '—'}</dd>
                      </div>
                      {detailModal.order.deliveryMethod && (
                        <div className="admin-order-field admin-order-field--full">
                          <dt>Способ доставки</dt>
                          <dd>
                            {translateDeliveryMethod(detailModal.order.deliveryMethod.methodName)}
                            {detailModal.order.deliveryMethod.description && (
                              <span className="admin-order-muted">
                                {' '}
                                — {detailModal.order.deliveryMethod.description}
                              </span>
                            )}
                          </dd>
                        </div>
                      )}
                      {detailModal.order.paymentMethod && (
                        <div className="admin-order-field admin-order-field--full">
                          <dt>Оплата</dt>
                          <dd>
                            {translatePaymentMethod(detailModal.order.paymentMethod.methodName)}
                            {detailModal.order.paymentMethod.description && (
                              <span className="admin-order-muted">
                                {' '}
                                — {detailModal.order.paymentMethod.description}
                              </span>
                            )}
                          </dd>
                        </div>
                      )}
                    </dl>
                  </section>

                  <section className="admin-order-panel admin-order-panel--accent">
                    <h4 className="admin-order-panel__title">Склад и маршрут</h4>
                    {detailModal.order.sourceWarehouse ? (
                      <dl className="admin-order-fields">
                        <div className="admin-order-field admin-order-field--wide">
                          <dt>Склад</dt>
                          <dd>
                            <strong>{detailModal.order.sourceWarehouse.warehouseName}</strong>
                            {detailModal.order.sourceWarehouse.cityName
                              ? ` · ${detailModal.order.sourceWarehouse.cityName}`
                              : ''}
                          </dd>
                        </div>
                        <div className="admin-order-field admin-order-field--full">
                          <dt>Адрес склада</dt>
                          <dd>{detailModal.order.sourceWarehouse.address || '—'}</dd>
                        </div>
                        {detailModal.order.shippingCityName && (
                          <div className="admin-order-field">
                            <dt>Город получателя</dt>
                            <dd>{detailModal.order.shippingCityName}</dd>
                          </div>
                        )}
                        <div className="admin-order-field">
                          <dt>Дистанция</dt>
                          <dd>
                            {detailModal.order.distanceKm !== null &&
                            detailModal.order.distanceKm !== undefined
                              ? `${detailModal.order.distanceKm} км`
                              : detailModal.order.shippingCityName
                                ? 'Не рассчитана'
                                : '—'}
                          </dd>
                        </div>
                        {detailModal.order.routePath && (
                          <div className="admin-order-field admin-order-field--full">
                            <dt>Маршрут</dt>
                            <dd className="admin-order-route-path">{detailModal.order.routePath}</dd>
                          </div>
                        )}
                      </dl>
                    ) : (
                      <p className="admin-order-empty-hint">
                        Склад не назначен — можно указать при статусе «В обработке».
                      </p>
                    )}
                  </section>
                </div>

                {detailModal.items && detailModal.items.length > 0 && (
                  <section className="admin-order-positions">
                    <h4 className="admin-order-positions__title">
                      Позиции заказа
                      <span className="admin-order-positions__count">{detailModal.items.length}</span>
                    </h4>
                    <div className="admin-order-items-grid">
                      {detailModal.items.map((it) => (
                        <article key={it.orderItemId} className="admin-order-item-card">
                          <span className="admin-order-item-card__qty">×{it.quantity}</span>
                          <div className="admin-order-item-card__body">
                            <div className="admin-order-item-card__name">
                              {it.product?.name || 'Товар'}
                            </div>
                            <div className="admin-order-item-card__meta">
                              <span>
                                {(Number(it.unitPrice) || 0).toLocaleString('ru-RU')} р. / шт.
                              </span>
                              <span className="admin-order-item-card__sep">·</span>
                              <span>
                                итого{' '}
                                {((Number(it.unitPrice) || 0) * (Number(it.quantity) || 0)).toLocaleString(
                                  'ru-RU'
                                )}{' '}
                                р.
                              </span>
                            </div>
                          </div>
                        </article>
                      ))}
                    </div>
                  </section>
                )}

                <details className="admin-order-user-block" open={showUser}>
                  <summary className="admin-order-user-block__summary" onClick={() => setShowUser((v) => !v)}>
                    Профиль пользователя
                  </summary>
                  <div className="admin-order-user-details">
                    <dl className="admin-order-fields admin-order-fields--compact">
                      <div className="admin-order-field">
                        <dt>Email</dt>
                        <dd>
                          {userInfo?.email ||
                            detailModal.order.user?.email ||
                            detailModal.order.userEmail ||
                            '—'}
                        </dd>
                      </div>
                      <div className="admin-order-field">
                        <dt>Роль</dt>
                        <dd>
                          {translateRole(
                            userInfo?.role?.roleName ||
                              detailModal.order.user?.role?.roleName ||
                              detailModal.order.role
                          )}
                        </dd>
                      </div>
                      <div className="admin-order-field">
                        <dt>Имя</dt>
                        <dd>
                          {userInfo?.firstName || detailModal.order.user?.firstName || '—'}{' '}
                          {userInfo?.lastName || detailModal.order.user?.lastName || ''}
                        </dd>
                      </div>
                      <div className="admin-order-field">
                        <dt>Телефон</dt>
                        <dd>{userInfo?.phoneNumber || detailModal.order.user?.phoneNumber || '—'}</dd>
                      </div>
                      <div className="admin-order-field">
                        <dt>Город</dt>
                        <dd>{userInfo?.cityName || detailModal.order.user?.cityName || '—'}</dd>
                      </div>
                      <div className="admin-order-field">
                        <dt>Пол</dt>
                        <dd>{translateGender(userInfo?.gender || detailModal.order.user?.gender)}</dd>
                      </div>
                      {userInfo?.department && (
                        <div className="admin-order-field admin-order-field--full">
                          <dt>Отдел</dt>
                          <dd>{translateDepartment(userInfo.department)}</dd>
                        </div>
                      )}
                    </dl>
                  </div>
                </details>

              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default OrderManagement;




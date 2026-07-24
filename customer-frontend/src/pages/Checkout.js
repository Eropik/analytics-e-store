import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { cartService, orderService, profileService } from '../services/api';
import './Checkout.css';
import { translateDeliveryMethod, translatePaymentMethod } from '../utils/enumTranslations';

function Checkout() {
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [cityId, setCityId] = useState('');
  const [address, setAddress] = useState('');
  const [deliveryMethodId, setDeliveryMethodId] = useState('');
  const [paymentMethodId, setPaymentMethodId] = useState('');
  const [cities, setCities] = useState([]);
  const [deliveryMethods, setDeliveryMethods] = useState([]);
  const [paymentMethods, setPaymentMethods] = useState([]);
  const [pendingOrder, setPendingOrder] = useState(false);
  const [nearestWarehouse, setNearestWarehouse] = useState(null);
  const [quote, setQuote] = useState({
    itemsCount: 0,
    baseTotal: 0,
    discountPercent: 0,
    discountValue: 0,
    afterDiscountTotal: 0,
    distanceKm: 0,
    deliveryPercent: 0,
    deliveryExtra: 0,
    finalTotal: 0,
  });
  const userId = localStorage.getItem('userId');

  const loadCart = useCallback(async () => {
    try {
      const response = await cartService.get(userId);
      const data = response.data || {};
      const normalized = data.cart
        ? {
            items: data.cart.items || [],
            totalAmount: data.total || data.cart.totalAmount || 0,
          }
        : data;
      setCart(normalized);
    } catch (error) {
      console.error('Error loading cart:', error);
    }
  }, [userId]);

  const loadDictionaries = useCallback(async () => {
    try {
      const [cityRes, delivRes, payRes] = await Promise.all([
        profileService.getCities(),
        orderService.getDeliveryMethods(),
        orderService.getPaymentMethods(),
      ]);
      setCities(cityRes.data || []);
      setDeliveryMethods(delivRes.data || []);
      setPaymentMethods(payRes.data || []);
    } catch (e) {
      console.error('Dict load error', e);
      setCities([]);
      setDeliveryMethods([]);
      setPaymentMethods([]);
    }
  }, []);

  const prefillCity = useCallback(async () => {
    try {
      const prof = await profileService.get(userId);
      if (prof.data?.cityId) setCityId(prof.data.cityId);
    } catch (e) {
      // ignore
    }
  }, [userId]);

  const loadNearestWarehouse = useCallback(async (selectedCityId) => {
    if (!selectedCityId) {
      setNearestWarehouse(null);
      return;
    }
    try {
      const res = await orderService.getNearestWarehouse(selectedCityId);
      setNearestWarehouse(res.data || null);
    } catch (e) {
      setNearestWarehouse(null);
    }
  }, []);

  const recalculateQuote = useCallback(() => {
    if (!cart?.items?.length) {
      setQuote({
        itemsCount: 0, baseTotal: 0, discountPercent: 0, discountValue: 0,
        afterDiscountTotal: 0, distanceKm: 0, deliveryPercent: 0, deliveryExtra: 0, finalTotal: 0,
      });
      return;
    }
    const itemsCount = cart.items.reduce((sum, i) => sum + (i.quantity || 0), 0);
    const baseTotal = cart.items.reduce((sum, i) => sum + Number(i.unitPrice || 0) * Number(i.quantity || 0), 0);
    const discountPercent = itemsCount >= 10 ? 10 : (itemsCount > 5 ? 8 : 0);
    const discountValue = Math.round((baseTotal * discountPercent) / 100);
    const afterDiscountTotal = baseTotal - discountValue;

    const selectedDelivery = deliveryMethods.find((d) => String(d.methodId || d.deliveryMethodId || d.id) === String(deliveryMethodId));
    const deliveryName = (selectedDelivery?.methodName || selectedDelivery?.deliveryMethodName || selectedDelivery?.name || '').toLowerCase();
    const isSelfPickup = deliveryName.includes('pickup');
    const distanceKm = Math.round(Number(nearestWarehouse?.distance || 0));
    const deliveryPercent = isSelfPickup ? 0 : (distanceKm <= 150 ? 5 : 8);
    const deliveryExtra = Math.round((afterDiscountTotal * deliveryPercent) / 100);
    const finalTotal = afterDiscountTotal + deliveryExtra;

    setQuote({
      itemsCount,
      baseTotal,
      discountPercent,
      discountValue,
      afterDiscountTotal,
      distanceKm,
      deliveryPercent,
      deliveryExtra,
      finalTotal,
    });
  }, [cart, deliveryMethods, deliveryMethodId, nearestWarehouse]);

  useEffect(() => {
    if (userId) {
      loadCart();
      loadDictionaries();
      prefillCity();
    }
  }, [userId, loadCart, loadDictionaries, prefillCity]);

  useEffect(() => {
    loadNearestWarehouse(cityId);
  }, [cityId, loadNearestWarehouse]);

  useEffect(() => {
    recalculateQuote();
  }, [recalculateQuote]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!cart || !cart.items || cart.items.length === 0) {
      alert('Корзина пуста');
      return;
    }

    // Проверка наличия на складе (если данные есть)
    const insufficient = (cart.items || []).find(
      (it) => (it.product?.stockQuantity ?? it.stockQuantity ?? Infinity) < it.quantity
    );
    if (insufficient) {
      alert(`Недостаточно товара "${insufficient.product?.name || insufficient.productName || 'товар'}" на складе`);
      return;
    }

    const orderData = {
      userId: userId,
      shippingCityId: parseInt(cityId),
      shippingAddressText: address,
      deliveryMethodId: parseInt(deliveryMethodId),
      paymentMethodId: parseInt(paymentMethodId),
      items: cart.items.map(item => ({
        productId: item.product?.productId || item.productId,
        quantity: item.quantity
      }))
    };

    setPendingOrder(true);
    try {
      await orderService.create(orderData);
      setPendingOrder(false);
      navigate('/profile');
    } catch (error) {
      console.error('Error creating order:', error);
      alert('Ошибка при оформлении заказа');
      setPendingOrder(false);
    }
  };

  if (!cart) return <div>Загрузка...</div>;

  return (
    <div className="checkout">
      <h1>Оформление заказа</h1>
      <form onSubmit={handleSubmit} className="checkout-grid">
        <div className="checkout-left">
          <div className="form-group">
            <label>Город доставки:</label>
            <select value={cityId} onChange={(e) => setCityId(e.target.value)} required>
              <option value="">Выберите город</option>
              {cities.map((c) => (
                <option key={c.cityId} value={c.cityId}>{c.cityName}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Адрес доставки:</label>
            <input
              type="text"
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label>Способ доставки:</label>
            <select value={deliveryMethodId} onChange={(e) => setDeliveryMethodId(e.target.value)} required>
              <option value="">Выберите</option>
              {deliveryMethods.map((d) => (
                <option key={d.methodId || d.deliveryMethodId || d.id} value={d.methodId || d.deliveryMethodId || d.id}>
                  {translateDeliveryMethod(d.methodName || d.deliveryMethodName || d.name || d.title)}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Способ оплаты:</label>
            <select value={paymentMethodId} onChange={(e) => setPaymentMethodId(e.target.value)} required>
              <option value="">Выберите</option>
              {paymentMethods.map((p) => (
                <option key={p.methodId || p.paymentMethodId || p.id} value={p.methodId || p.paymentMethodId || p.id}>
                  {translatePaymentMethod(p.methodName || p.paymentMethodName || p.name || p.title)}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="checkout-right">
          <button className="confirm-btn" type="submit" disabled={pendingOrder}>Подтвердить заказ</button>
          <div className="order-summary">
            <h2>Сводка заказа</h2>
            <p><strong>Откуда:</strong> {nearestWarehouse?.warehouseName ? `${nearestWarehouse.warehouseName}, ${nearestWarehouse.address || 'адрес не указан'}` : 'Будет назначено после подтверждения админом'}</p>
            <p><strong>Куда:</strong> {cities.find((c) => String(c.cityId) === String(cityId))?.cityName || '—'}, {address || '—'}</p>
            <p><strong>Товаров:</strong> {quote.itemsCount}</p>
            <p className="price-row"><strong>Базовая сумма:</strong> {quote.baseTotal} р.</p>
            {quote.discountPercent > 0 && (
              <p className="discount-row">
                <strong>Скидка:</strong> {quote.discountPercent}% ({quote.discountValue} р.) - в заказе {quote.itemsCount} товаров
              </p>
            )}
            {quote.deliveryPercent > 0 && (
              <p className="delivery-row">
                <strong>Доплата за доставку:</strong> {quote.deliveryPercent}% ({quote.deliveryExtra} р.) при расстоянии {quote.distanceKm} км
              </p>
            )}
            <p className="total-row"><strong>Итог:</strong> {quote.finalTotal} р.</p>
            {cart?.items?.length > 0 && (
              <div className="order-products-list">
                <strong>Товары в заказе:</strong>
                <ul>
                  {cart.items.map((item, idx) => (
                    <li key={`${item.product?.productId || item.productId || idx}-${idx}`}>
                      {item.product?.name || item.productName || 'Товар без названия'} x {item.quantity}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>
      </form>
    </div>
  );
}

export default Checkout;




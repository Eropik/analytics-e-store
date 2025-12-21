import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { cartService, orderService, profileService } from '../services/api';
import './Checkout.css';

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

  useEffect(() => {
    if (userId) {
      loadCart();
      loadDictionaries();
      prefillCity();
    }
  }, [userId, loadCart, loadDictionaries, prefillCity]);

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
      navigate('/orders');
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
      <form onSubmit={handleSubmit}>
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
                {d.methodName || d.deliveryMethodName || d.name || d.title}
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
                {p.methodName || p.paymentMethodName || p.name || p.title}
              </option>
            ))}
          </select>
        </div>

        <div className="order-summary">
          <h2>Итого: {cart.totalAmount || 0} ₽</h2>
        </div>

        <button type="submit" disabled={pendingOrder}>Подтвердить заказ</button>
      </form>
    </div>
  );
}

export default Checkout;




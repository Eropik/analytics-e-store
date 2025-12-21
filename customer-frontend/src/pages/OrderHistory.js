import React, { useState, useEffect, useCallback } from 'react';
import { orderService } from '../services/api';
import './OrderHistory.css';

function OrderHistory() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const userId = localStorage.getItem('userId');

  const loadOrders = useCallback(async () => {
    setLoading(true);
    try {
      const response = await orderService.getUserOrders(userId);
      setOrders(response.data.orders || []);
    } catch (error) {
      console.error('Error loading orders:', error);
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    if (userId) {
      loadOrders();
    }
  }, [userId, loadOrders]);

  if (loading) return <div>Загрузка...</div>;

  return (
    <div className="order-history">
      <h1>История заказов</h1>
      {orders.length === 0 ? (
        <p>Заказов пока нет</p>
      ) : (
        <div className="orders-list">
          {orders.map(order => (
            <div key={order.id} className="order-card">
              <h3>Заказ #{order.id}</h3>
              <p><strong>Дата:</strong> {new Date(order.orderDate).toLocaleDateString()}</p>
              <p><strong>Статус:</strong> {order.statusName}</p>
              <p><strong>Сумма:</strong> {order.totalAmount} ₽</p>
              <p><strong>Адрес доставки:</strong> {order.deliveryInfo?.shippingAddressText || 'Не указан'}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default OrderHistory;




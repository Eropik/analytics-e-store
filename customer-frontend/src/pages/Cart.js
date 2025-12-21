import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { cartService } from '../services/api';
import './Cart.css';

function Cart() {
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const userId = localStorage.getItem('userId');

  const loadCart = useCallback(async () => {
    setLoading(true);
    try {
      const response = await cartService.get(userId);
      const data = response.data || {};
      const normalized = {
        items: data.cart?.items || [],
        totalAmount: data.total || data.cart?.totalAmount || 0,
        itemsCount: data.itemsCount || data.cart?.itemsCount || 0,
      };
      setCart(normalized);
    } catch (error) {
      console.error('Error loading cart:', error);
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    if (userId) {
      loadCart();
    }
  }, [userId, loadCart]);

  const handleCheckout = () => {
    navigate('/checkout');
  };

  const handleRemoveItem = async (productId) => {
    try {
      await cartService.remove({
        userId: userId,
        productId: productId
      });
      await loadCart();
    } catch (error) {
      console.error('Error removing item from cart:', error);
      alert('Не удалось удалить товар из корзины');
    }
  };

  if (loading) return <div>Загрузка...</div>;
  if (!cart || !cart.items || cart.items.length === 0) {
    return (
      <div className="cart">
        <h1>Корзина пуста</h1>
      </div>
    );
  }

  return (
    <div className="cart">
      <h1>Корзина</h1>
      <div className="cart-items">
        {cart.items.map(item => (
          <div key={item.cartItemId} className="cart-item">
            <img src={item.product?.mainImageUrl || '/placeholder.png'} alt={item.product?.name} />
            <div className="item-info">
              <h3>{item.product?.name}</h3>
              <p>Цена: {item.unitPrice} ₽</p>
              <p>Количество: {item.quantity}</p>
              <p>Итого: {(item.unitPrice || 0) * (item.quantity || 0)} ₽</p>
              <button 
                className="remove-btn" 
                onClick={() => handleRemoveItem(item.product?.productId)}
              >
                Удалить
              </button>
            </div>
          </div>
        ))}
      </div>
      <div className="cart-total">
        <h2>Итого: {cart.totalAmount || 0} ₽</h2>
        <button onClick={handleCheckout}>Оформить заказ</button>
      </div>
    </div>
  );
}

export default Cart;




import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { productService, cartService } from '../services/api';
import './ProductDetail.css';

function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const userId = localStorage.getItem('userId'); // В реальном приложении получать из контекста

  const loadProduct = useCallback(async () => {
    setLoading(true);
    try {
      const response = await productService.getById(id);
      setProduct(response.data);
    } catch (error) {
      console.error('Error loading product:', error);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadProduct();
  }, [id, loadProduct]);

  const handleAddToCart = async () => {
    if (!userId) {
      navigate('/login');
      return;
    }

    try {
      await cartService.add({
        userId: userId,
        productId: id,
        quantity: quantity,
        unitPrice: product.price
      });
      alert('Товар добавлен в корзину');
    } catch (error) {
      console.error('Error adding to cart:', error);
      alert('Ошибка при добавлении товара в корзину');
    }
  };

  if (loading) return <div>Загрузка...</div>;
  if (!product) return <div>Товар не найден</div>;

  return (
    <div className="product-detail">
      <button onClick={() => navigate(-1)}>Назад</button>
      <div className="product-info">
        <img src={product.mainImageUrl || '/placeholder.png'} alt={product.name} />
        <div className="product-details">
          <h1>{product.name}</h1>
          <p className="price">{product.price} ₽</p>
          <p>{product.description}</p>
          <p>В наличии: {product.stockQuantity}</p>
          
          <div className="add-to-cart">
            <input
              type="number"
              min="1"
              max={product.stockQuantity}
              value={quantity}
              onChange={(e) => setQuantity(parseInt(e.target.value))}
            />
            <button onClick={handleAddToCart}>Добавить в корзину</button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProductDetail;




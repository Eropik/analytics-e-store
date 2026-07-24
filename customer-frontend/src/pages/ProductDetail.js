import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { productService, cartService } from '../services/api';
import './ProductDetail.css';

const PLACEHOLDER = '/placeholder.png';

function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [selectedImage, setSelectedImage] = useState('');
  const [cartMessage, setCartMessage] = useState('');
  const [cartError, setCartError] = useState('');
  const userId = localStorage.getItem('userId');

  const gallerySlides = useMemo(() => {
    if (!product) return [];
    const main = product.mainImageUrl;
    const sorted = [...(product.images || [])].sort(
      (a, b) => (a.sortOrder || 0) - (b.sortOrder || 0)
    );
    const out = [];
    const seen = new Set();
    if (main) {
      seen.add(main);
      out.push({ key: 'main', url: main });
    }
    sorted.forEach((img, idx) => {
      const u = img.imageUrl;
      if (!u || seen.has(u)) return;
      seen.add(u);
      out.push({ key: img.imageId ?? `img-${idx}-${u}`, url: u });
    });
    if (out.length === 0) out.push({ key: 'placeholder', url: PLACEHOLDER });
    return out;
  }, [product]);

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

  useEffect(() => {
    if (gallerySlides.length === 0) return;
    setSelectedImage((prev) => {
      if (gallerySlides.some((s) => s.url === prev)) return prev;
      return gallerySlides[0].url;
    });
  }, [gallerySlides]);

  const handleAddToCart = async () => {
    if (!userId) {
      navigate('/login');
      return;
    }

    if (!quantity || quantity < 1) {
      setCartError('Введите корректное количество.');
      setCartMessage('');
      return;
    }
    if (quantity > (product.stockQuantity || 0)) {
      setCartError(`На складе только ${product.stockQuantity} шт.`);
      setCartMessage('');
      return;
    }

    try {
      const res = await cartService.add({
        userId: userId,
        productId: id,
        quantity: quantity,
        unitPrice: product.price,
      });
      setCartMessage(res.data?.message || 'Товар добавлен в корзину.');
      setCartError('');
    } catch (error) {
      console.error('Error adding to cart:', error);
      setCartError(error.response?.data?.error || 'Ошибка при добавлении товара в корзину.');
      setCartMessage('');
    }
  };

  if (loading) return <div>Загрузка...</div>;
  if (!product) return <div>Товар не найден</div>;

  const hasGallery = gallerySlides.length > 1;

  return (
    <div className="product-detail">
      <button className="bck-btn" type="button" onClick={() => navigate(-1)}>
        Назад
      </button>
      <div className="product-info">
        <div className="product-gallery-card">
          <div className="product-gallery-main">
            <img
              src={selectedImage || PLACEHOLDER}
              alt={product.name}
              className="main-product-image"
              onError={(e) => {
                e.target.onerror = null;
                e.target.src = PLACEHOLDER;
              }}
            />
          </div>
          {hasGallery && (
            <div className="product-gallery-strip" role="tablist" aria-label="Галерея изображений">
              <span className="product-gallery-caption">Все фото — выберите для просмотра</span>
              <div className="gallery-thumbs-grid">
                {gallerySlides.map((slide, index) => (
                  <button
                    key={slide.key}
                    type="button"
                    role="tab"
                    aria-selected={selectedImage === slide.url}
                    className={`thumb-btn ${selectedImage === slide.url ? 'active' : ''}`}
                    onClick={() => setSelectedImage(slide.url)}
                    title={`Фото ${index + 1}`}
                  >
                    <img src={slide.url} alt="" onError={(e) => { e.target.onerror = null; e.target.src = PLACEHOLDER; }} />
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>
        <div className="product-details">
          <h1>{product.name}</h1>
          <p className="price">{product.price} р.</p>
          <p>{product.description}</p>
          <p>В наличии: {product.stockQuantity}</p>

          <div className="add-to-cart">
            <input
              type="number"
              min="1"
              max={product.stockQuantity}
              value={quantity}
              onChange={(e) => setQuantity(parseInt(e.target.value, 10) || 1)}
            />
            <button type="button" onClick={handleAddToCart}>
              Добавить в корзину
            </button>
          </div>
          {cartMessage && <p className="cart-success">{cartMessage}</p>}
          {cartError && <p className="cart-error">{cartError}</p>}
        </div>
      </div>
    </div>
  );
}

export default ProductDetail;

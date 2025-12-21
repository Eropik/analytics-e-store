import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { productService } from '../services/api';
import './ProductCatalog.css';

function ProductCatalog() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [page, setPage] = useState(0);
  const [categoryId, setCategoryId] = useState('');
  const [brandId, setBrandId] = useState('');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [maxPriceLimit, setMaxPriceLimit] = useState(50000);
  const [categories, setCategories] = useState([]);
  const [brands, setBrands] = useState([]);

  const loadProducts = useCallback(async () => {
    setLoading(true);
    try {
      let response;

      // Выбор эндпоинта в зависимости от фильтров
      if (minPrice || maxPrice) {
        response = await productService.byPriceRange(
          minPrice || 0,
          maxPrice || maxPriceLimit,
          page,
          20
        );
      } else if (categoryId) {
        response = await productService.byCategory(categoryId, page, 20);
      } else if (brandId) {
        response = await productService.byBrand(brandId, page, 20);
      } else if (searchQuery) {
        response = await productService.search(searchQuery, page);
      } else {
        response = await productService.getAll(page, 20);
      }

      const list = response.data?.products || response.data?.content || response.data || [];
      setProducts(Array.isArray(list) ? list : []);
    } catch (error) {
      console.error('Error loading products:', error);
    } finally {
      setLoading(false);
    }
  }, [page, searchQuery, categoryId, brandId, minPrice, maxPrice]);

  useEffect(() => {
    loadProducts();
  }, [page, searchQuery, loadProducts]);

  useEffect(() => {
    const fetchDicts = async () => {
      try {
        const [catRes, brRes] = await Promise.all([
          productService.getCategories(),
          productService.getBrands(),
        ]);
        setCategories(catRes.data || []);
        setBrands(brRes.data || []);
      } catch (e) {
        setCategories([]);
        setBrands([]);
      }
    };
    fetchDicts();
  }, []);

  return (
    <div className="product-catalog">
      <h1>Каталог товаров</h1>
      
      <div className="search-bar">
        <input
          type="text"
          placeholder="Поиск товаров..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
        <details className="filters-collapse">
          <summary>
            Показать фильтры
          </summary>
          <div className="filters">
            <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
              <option value="">Категория: любая</option>
              {categories.map((c) => (
                <option key={c.categoryId} value={c.categoryId}>
                  {c.categoryName} (ID {c.categoryId})
                </option>
              ))}
            </select>
            <select value={brandId} onChange={(e) => setBrandId(e.target.value)}>
              <option value="">Бренд: любой</option>
              {brands.map((b) => (
                <option key={b.brandId} value={b.brandId}>
                  {b.brandName} (ID {b.brandId})
                </option>
              ))}
            </select>
            <div className="range duo range-max">
              <label>Макс. цена</label>
              <input
                type="number"
                value={maxPrice}
                onChange={(e) => setMaxPrice(e.target.value)}
                min="0"
              />
              <div className="range-slider">
                <input
                  type="range"
                  min="0"
                  max={maxPriceLimit}
                  step="100"
                  value={maxPrice || maxPriceLimit}
                  onChange={(e) => setMaxPrice(e.target.value)}
                />
              </div>
            </div>
            <div className="range duo range-min">
              <label>Мин. цена</label>
              <input
                type="number"
                value={minPrice}
                onChange={(e) => setMinPrice(e.target.value)}
                min="0"
              />
              <div className="range-slider">
                <input
                  type="range"
                  min="0"
                  max={maxPriceLimit}
                  step="100"
                  value={minPrice || 0}
                  onChange={(e) => setMinPrice(e.target.value)}
                />
              </div>
            </div>
            <button
              type="button"
              className="clear-btn"
              onClick={() => {
                setCategoryId('');
                setBrandId('');
                setMinPrice('');
                setMaxPrice('');
                setSearchQuery('');
                setPage(0);
              }}
            >
              Очистить
            </button>
          </div>
        </details>
      </div>

      {loading ? (
        <div className="loading-text">Загрузка...</div>
      ) : (
        <div className="products-grid">
          {products.map(product => (
            <Link
              key={product.productId}
              to={`/products/${product.productId}`}
              className={`product-card ${product.stockQuantity === 0 ? 'out-of-stock' : ''}`}
            >
              <img src={product.mainImageUrl || '/placeholder.png'} alt={product.name} />
              <h3>{product.name}</h3>
              <p className="price">{product.price} ₽</p>
              <p className="stock">В наличии: {product.stockQuantity}</p>
            </Link>
          ))}
        </div>
      )}

      <div className="pagination">
        <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}>
          Назад
        </button>
        <span>Страница {page + 1}</span>
        <button onClick={() => setPage(p => p + 1)}>
          Вперед
        </button>
      </div>
    </div>
  );
}

export default ProductCatalog;




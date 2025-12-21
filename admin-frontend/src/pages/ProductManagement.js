import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { productService } from '../services/api';
import './ProductManagement.css';

// Встраиваем плейсхолдер как data URI, чтобы не было множества сетевых запросов
const PLACEHOLDER_IMAGE =
  'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjYwIiBoZWlnaHQ9IjYwIiBmaWxsPSIjZWVlIiByeD0iMTIiLz48cGF0aCBkPSJNMjAgMzAgbDggOCAxMi0xOGw4IDEyIiBzdHJva2U9IiNkZGQiIHN0cm9rZS13aWR0aD0iMyIgZmlsbD0ibm9uZSIgLz48Y2lyY2xlIGN4PSIyMyIgY3k9IjIwIiByPSI2IiBzdHJva2U9IiNkZGQiIHN0cm9rZS13aWR0aD0iMyIgZmlsbD0iI2ZmZiIvPjwvc3ZnPg==';

function ProductManagement() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [selected, setSelected] = useState(null);
  const [errorMsg, setErrorMsg] = useState('');
  const [detailError, setDetailError] = useState('');
  const [form, setForm] = useState({
    name: '',
    description: '',
    price: '',
    stockQuantity: '',
    mainImageUrl: '',
    categoryId: '',
    brandId: '',
  });
  const [saving, setSaving] = useState(false);
  const [gallerySort, setGallerySort] = useState('');
  const [brandName, setBrandName] = useState('');
  const [categoryName, setCategoryName] = useState('');
  const [categories, setCategories] = useState([]);
  const [brands, setBrands] = useState([]);
  const [editCategoryId, setEditCategoryId] = useState(null);
  const [editCategoryName, setEditCategoryName] = useState('');
  const [editBrandId, setEditBrandId] = useState(null);
  const [editBrandName, setEditBrandName] = useState('');
  const [searchProduct, setSearchProduct] = useState('');
  const [searchProductId, setSearchProductId] = useState('');
  const [searchCategory, setSearchCategory] = useState('');
  const [searchBrand, setSearchBrand] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [filterCategoryId, setFilterCategoryId] = useState('');
  const [filterBrandId, setFilterBrandId] = useState('');
  const [filterMinPrice, setFilterMinPrice] = useState('');
  const [filterMaxPrice, setFilterMaxPrice] = useState('');
  const [searchCategoryId, setSearchCategoryId] = useState('');
  const [searchBrandId, setSearchBrandId] = useState('');
  const navigate = useNavigate();
  const adminUserId = localStorage.getItem('adminUserId');

  useEffect(() => {
    if (!adminUserId) {
      navigate('/login');
      return;
    }
    loadProducts();
    loadDicts();
  }, [adminUserId, navigate]);

  const loadProducts = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const response = await productService.search({
        productId: searchProductId || undefined,
        name: searchProduct || undefined,
        categoryId: filterCategoryId || undefined,
        brandId: filterBrandId || undefined,
        minPrice: filterMinPrice || undefined,
        maxPrice: filterMaxPrice || undefined,
        adminUserId,
      });
      setProducts(response.data.products || []);
    } catch (error) {
      console.error('Error loading products:', error);
      setErrorMsg(error.response?.data?.error || 'Не удалось загрузить товары');
    } finally {
      setLoading(false);
    }
  };

  const handleClearProducts = () => {
    setSearchProduct('');
    setSearchProductId('');
    setFilterCategoryId('');
    setFilterBrandId('');
    setFilterMinPrice('');
    setFilterMaxPrice('');
    setTimeout(loadProducts, 0);
  };

  const closeDetail = () => {
    setShowModal(false);
    setSelected(null);
    setDetailError('');
  };

  const loadDicts = async () => {
    try {
      const [catRes, brandRes] = await Promise.all([
        productService.getCategories(adminUserId),
        productService.getBrands(adminUserId),
      ]);
      let cats = catRes.data || [];
      let brs = brandRes.data || [];
      if (searchCategory.trim()) {
        const q = searchCategory.trim().toLowerCase();
        cats = cats.filter((c) => c.categoryName?.toLowerCase().includes(q) || String(c.categoryId).includes(q));
      }
      if (searchCategoryId.trim()) {
        const qid = searchCategoryId.trim().toLowerCase();
        cats = cats.filter((c) => String(c.categoryId).toLowerCase().includes(qid));
      }
      if (searchBrand.trim()) {
        const q = searchBrand.trim().toLowerCase();
        brs = brs.filter((b) => b.brandName?.toLowerCase().includes(q) || String(b.brandId).includes(q));
      }
      if (searchBrandId.trim()) {
        const qid = searchBrandId.trim().toLowerCase();
        brs = brs.filter((b) => String(b.brandId).toLowerCase().includes(qid));
      }
      setCategories(cats);
      setBrands(brs);
    } catch (e) {
      console.error('Error loading dicts', e);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setSaving(true);
    setErrorMsg('');
    try {
      const payload = {
        name: form.name,
        description: form.description,
        price: form.price ? Number(form.price) : 0,
        stockQuantity: form.stockQuantity ? Number(form.stockQuantity) : 0,
        mainImageUrl: form.mainImageUrl,
        categoryId: form.categoryId ? Number(form.categoryId) : null,
        brandId: form.brandId ? Number(form.brandId) : null,
        images: [],
      };
      await productService.create(payload, adminUserId);
      setShowForm(false);
      setShowModal(false);
      setSelected(null);
      setForm({
        name: '',
        description: '',
        price: '',
        stockQuantity: '',
        mainImageUrl: '',
        categoryId: '',
        brandId: '',
      });
      loadProducts();
      loadDicts();
    } catch (error) {
      console.error('Error creating product:', error);
      setErrorMsg(error.response?.data?.error || 'Не удалось создать товар');
    } finally {
      setSaving(false);
    }
  };

  const handleSelectProduct = async (productId) => {
    setDetailError('');
    setErrorMsg('');
    try {
      const res = await productService.getById(productId, adminUserId);
      setSelected(res.data);
      setShowModal(true);
      setForm({
        name: res.data.name || '',
        description: res.data.description || '',
        price: res.data.price || '',
        stockQuantity: res.data.stockQuantity || '',
        mainImageUrl: res.data.mainImageUrl || '',
        categoryId: res.data.category?.categoryId || '',
        brandId: res.data.brand?.brandId || '',
      });
    } catch (e) {
      console.error('Error loading product', e);
      setDetailError(e.response?.data?.error || 'Не удалось загрузить товар');
    }
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    if (!selected) return;
    setSaving(true);
    setErrorMsg('');
    try {
      const payload = {
        productId: selected.productId,
        name: form.name,
        description: form.description,
        price: form.price ? Number(form.price) : 0,
        stockQuantity: form.stockQuantity ? Number(form.stockQuantity) : 0,
        mainImageUrl: form.mainImageUrl,
        category: form.categoryId ? { categoryId: Number(form.categoryId) } : null,
        brand: form.brandId ? { brandId: Number(form.brandId) } : null,
      };
      await productService.update(selected.productId, payload, adminUserId);
      await loadProducts();
      await handleSelectProduct(selected.productId);
      loadDicts();
    } catch (e) {
      console.error('Error updating product', e);
      setErrorMsg(e.response?.data?.error || 'Не удалось обновить товар');
    } finally {
      setSaving(false);
    }
  };

  const uploadMainImage = async (file) => {
    try {
      const res = await productService.uploadImage(file, adminUserId);
      setForm((prev) => ({ ...prev, mainImageUrl: res.data.imageUrl }));
    } catch (e) {
      console.error('Upload main image failed', e);
      alert('Не удалось загрузить главное изображение');
    }
  };

  const uploadGalleryImage = async (file) => {
    try {
      const res = await productService.uploadImage(file, adminUserId);
      const imageUrl = res.data.imageUrl;
      if (!selected) {
        // создаём при добавлении
        const current = form.images || [];
        const sortOrder = gallerySort ? Number(gallerySort) : current.length + 1;
        setForm((prev) => ({
          ...prev,
          images: [...current, { imageUrl, sortOrder }],
        }));
      } else {
        const sortOrder = gallerySort ? Number(gallerySort) : ((selected.images?.length || 0) + 1);
        await productService.addImageToProduct(
          selected.productId,
          { imageUrl, sortOrder },
          adminUserId
        );
        await handleSelectProduct(selected.productId);
      }
      setGallerySort('');
    } catch (e) {
      console.error('Upload gallery image failed', e);
      alert('Не удалось загрузить фото галереи');
    }
  };

  const handleAddCategory = async () => {
    if (!categoryName.trim()) return;
    try {
      await productService.addCategory(categoryName.trim(), adminUserId);
      alert('Категория создана');
      setCategoryName('');
      loadDicts();
    } catch (e) {
      console.error('Add category failed', e);
      alert('Не удалось создать категорию');
    }
  };

  const handleAddBrand = async () => {
    if (!brandName.trim()) return;
    try {
      await productService.addBrand(brandName.trim(), adminUserId);
      alert('Бренд создан');
      setBrandName('');
      loadDicts();
    } catch (e) {
      console.error('Add brand failed', e);
      alert('Не удалось создать бренд');
    }
  };

  const handleStartEditBrand = (b) => {
    setEditBrandId(b.brandId);
    setEditBrandName(b.brandName);
  };

  const handleSaveBrand = async () => {
    if (!editBrandId || !editBrandName.trim()) return;
    try {
      await productService.updateBrand(editBrandId, editBrandName.trim(), adminUserId);
      setEditBrandId(null);
      setEditBrandName('');
      loadDicts();
    } catch (e) {
      console.error('Update brand failed', e);
      alert('Не удалось обновить бренд');
    }
  };

  const handleStartEditCategory = (cat) => {
    setEditCategoryId(cat.categoryId);
    setEditCategoryName(cat.categoryName);
  };

  const handleSaveCategory = async () => {
    if (!editCategoryId || !editCategoryName.trim()) return;
    try {
      await productService.updateCategory(editCategoryId, editCategoryName.trim(), adminUserId);
      setEditCategoryId(null);
      setEditCategoryName('');
      loadDicts();
    } catch (e) {
      console.error('Update category failed', e);
      alert('Не удалось обновить категорию');
    }
  };

  if (loading) return <div>Загрузка...</div>;

  return (
    <div className="product-management">
      <h1>Управление товарами</h1>
      {errorMsg && <div className="error-msg">{errorMsg}</div>}
      {detailError && <div className="error-msg">{detailError}</div>}
      <div className="search-row">
        <input
          placeholder="ID товара"
          value={searchProductId}
          onChange={(e) => setSearchProductId(e.target.value)}
        />
        <input
          placeholder="Поиск товара по названию"
          value={searchProduct}
          onChange={(e) => setSearchProduct(e.target.value)}
        />
        <button onClick={loadProducts}>Искать</button>
        <button onClick={handleClearProducts}>Очистить</button>
      </div>
      <div className="search-row">
        <input
          placeholder="Категория ID"
          value={filterCategoryId}
          onChange={(e) => setFilterCategoryId(e.target.value)}
        />
        <input
          placeholder="Бренд ID"
          value={filterBrandId}
          onChange={(e) => setFilterBrandId(e.target.value)}
        />
        <input
          placeholder="Мин. цена"
          value={filterMinPrice}
          onChange={(e) => setFilterMinPrice(e.target.value)}
          type="number"
          step="0.01"
        />
        <input
          placeholder="Макс. цена"
          value={filterMaxPrice}
          onChange={(e) => setFilterMaxPrice(e.target.value)}
          type="number"
          step="0.01"
        />
      </div>
      <button
        className="add-button"
        onClick={() => {
          setSelected(null);
          setShowModal(false);
          setDetailError('');
          setForm({
            name: '',
            description: '',
            price: '',
            stockQuantity: '',
            mainImageUrl: '',
            categoryId: '',
            brandId: '',
          });
          setShowForm((v) => !v);
        }}>
        {showForm ? 'Закрыть форму' : 'Добавить товар'}
      </button>

      {showForm && (
        <form className="product-form" onSubmit={handleCreate}>
          <div className="form-row">
            <label>Название</label>
            <input name="name" value={form.name} onChange={handleChange} required />
          </div>
          <div className="form-row">
            <label>Описание</label>
            <textarea name="description" value={form.description} onChange={handleChange} />
          </div>
          <div className="form-row">
            <label>Цена</label>
            <input
              type="number"
              step="0.01"
              name="price"
              value={form.price}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-row">
            <label>Остаток</label>
            <input
              type="number"
              name="stockQuantity"
              value={form.stockQuantity}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-row">
            <label>Главное изображение URL</label>
            <input
              name="mainImageUrl"
              value={form.mainImageUrl}
              onChange={handleChange}
            />
            <input type="file" onChange={(e) => e.target.files[0] && uploadMainImage(e.target.files[0])} />
          </div>
          <div className="form-row">
            <label>ID категории</label>
            <input
              type="number"
              name="categoryId"
              value={form.categoryId}
              onChange={handleChange}
            />
          </div>
          <div className="form-row">
            <label>ID бренда</label>
            <input
              type="number"
              name="brandId"
              value={form.brandId}
              onChange={handleChange}
            />
          </div>
          <button type="submit" disabled={saving}>
            {saving ? 'Сохраняю...' : 'Создать'}
          </button>
        </form>
      )}

      <table className="products-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Название</th>
            <th>Цена</th>
            <th>Остаток</th>
            <th>Фото</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {products.map(product => (
            <tr key={product.productId}>
              <td>{product.productId}</td>
              <td>{product.name}</td>
              <td>{product.price} ₽</td>
              <td>{product.stockQuantity}</td>
              <td>
                <img
                  src={product.mainImageUrl || PLACEHOLDER_IMAGE}
                  alt="thumb"
                  className="thumb"
                  onError={(e) => { e.target.onerror = null; e.target.src = PLACEHOLDER_IMAGE; }}
                />
              </td>
              <td>
                <button onClick={() => handleSelectProduct(product.productId)}>Подробнее</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {selected && showModal && (
        <div className="modal-backdrop" onClick={closeDetail}>
          <div className="product-detail modal-card" onClick={(e) => e.stopPropagation()}>
            <button className="close-btn" onClick={closeDetail}>×</button>
            <h3>Товар: {selected.name}</h3>
            <img
              src={selected.mainImageUrl || PLACEHOLDER_IMAGE}
              alt={selected.name}
              className="product-main-image"
              onError={(e) => { e.target.onerror = null; e.target.src = PLACEHOLDER_IMAGE; }}
            />
            <div className="gallery">
              {selected.images && selected.images.map((img) => (
                <div key={img.imageId} className="gallery-item">
                  <img
                    src={img.imageUrl}
                    alt="gallery"
                    onError={(e) => { e.target.onerror = null; e.target.src = PLACEHOLDER_IMAGE; }}
                  />
                  <span>Порядок: {img.sortOrder}</span>
                </div>
              ))}
            </div>

            <h4>Редактирование</h4>
            <form className="product-form" onSubmit={handleUpdate}>
              <div className="form-row">
                <label>Название</label>
                <input name="name" value={form.name} onChange={handleChange} required />
              </div>
              <div className="form-row">
                <label>Описание</label>
                <textarea name="description" value={form.description} onChange={handleChange} />
              </div>
              <div className="form-row">
                <label>Цена</label>
                <input
                  type="number"
                  step="0.01"
                  name="price"
                  value={form.price}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="form-row">
                <label>Остаток</label>
                <input
                  type="number"
                  name="stockQuantity"
                  value={form.stockQuantity}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="form-row">
                <label>Главное изображение URL</label>
                <input
                  name="mainImageUrl"
                  value={form.mainImageUrl}
                  onChange={handleChange}
                />
                <input type="file" onChange={(e) => e.target.files[0] && uploadMainImage(e.target.files[0])} />
              </div>
              <div className="form-row">
                <label>ID категории</label>
                <input
                  type="number"
                  name="categoryId"
                  value={form.categoryId}
                  onChange={handleChange}
                />
              </div>
              <div className="form-row">
                <label>ID бренда</label>
                <input
                  type="number"
                  name="brandId"
                  value={form.brandId}
                  onChange={handleChange}
                />
              </div>
              <button type="submit" disabled={saving}>
                {saving ? 'Сохраняю...' : 'Обновить'}
              </button>
            </form>

            <div className="product-form">
              <div className="form-row">
                <label>Файл для галереи</label>
                <input type="file" onChange={(e) => e.target.files[0] && uploadGalleryImage(e.target.files[0])} />
              </div>
              <div className="form-row">
                <label>Порядок</label>
                <input
                  type="number"
                  value={gallerySort}
                  onChange={(e) => setGallerySort(e.target.value)}
                />
              </div>
            </div>
          </div>
        </div>
      )}

      <div className="product-form">
        <details open>
          <summary><h4 style={{ display: 'inline', margin: 0 }}>Добавить категорию</h4></summary>
          <div className="form-row-inline">
            <input
              placeholder="Поиск категории"
              value={searchCategory}
              onChange={(e) => setSearchCategory(e.target.value)}
            />
            <input
              placeholder="ID категории"
              value={searchCategoryId}
              onChange={(e) => setSearchCategoryId(e.target.value)}
            />
            <input
              placeholder="Название категории"
              value={categoryName}
              onChange={(e) => setCategoryName(e.target.value)}
            />
            <button type="button" onClick={loadDicts}>Искать</button>
            <button type="button" onClick={() => { setSearchCategory(''); setSearchCategoryId(''); loadDicts(); }}>Очистить</button>
            <button type="button" onClick={handleAddCategory}>Добавить категорию</button>
          </div>
          {editCategoryId && (
            <div className="form-row-inline" style={{ marginTop: '10px' }}>
              <input
                value={editCategoryName}
                onChange={(e) => setEditCategoryName(e.target.value)}
                placeholder="Редактировать название"
              />
              <button type="button" onClick={handleSaveCategory}>Сохранить категорию</button>
            </div>
          )}
          <details style={{ marginTop: '12px' }}>
            <summary><strong>Категории</strong></summary>
            <div className="list-box">
              {categories.map((c) => (
                <div key={c.categoryId} className="list-row">
                  <span>{c.categoryName}</span>
                  <button onClick={() => handleStartEditCategory(c)}>Редактировать</button>
                </div>
              ))}
            </div>
          </details>
        </details>

        <details open style={{ marginTop: '20px' }}>
          <summary><h4 style={{ display: 'inline', margin: 0 }}>Добавить бренд</h4></summary>
          <div className="form-row-inline">
            <input
              placeholder="Поиск бренда"
              value={searchBrand}
              onChange={(e) => setSearchBrand(e.target.value)}
            />
            <input
              placeholder="ID бренда"
              value={searchBrandId}
              onChange={(e) => setSearchBrandId(e.target.value)}
            />
            <input
              placeholder="Название бренда"
              value={brandName}
              onChange={(e) => setBrandName(e.target.value)}
            />
            <button type="button" onClick={loadDicts}>Искать</button>
            <button type="button" onClick={() => { setSearchBrand(''); setSearchBrandId(''); loadDicts(); }}>Очистить</button>
            <button type="button" onClick={handleAddBrand}>Добавить бренд</button>
          </div>
          {editBrandId && (
            <div className="form-row-inline" style={{ marginTop: '10px' }}>
              <input
                value={editBrandName}
                onChange={(e) => setEditBrandName(e.target.value)}
                placeholder="Редактировать название"
              />
              <button type="button" onClick={handleSaveBrand}>Сохранить бренд</button>
            </div>
          )}
          <details style={{ marginTop: '12px' }}>
            <summary><strong>Бренды</strong></summary>
            <div className="list-box">
              {brands.map((b) => (
                <div key={b.brandId} className="list-row">
                  <span>{b.brandName}</span>
                  <button onClick={() => handleStartEditBrand(b)}>Редактировать</button>
                </div>
              ))}
            </div>
          </details>
        </details>
      </div>
    </div>
  );
}

export default ProductManagement;




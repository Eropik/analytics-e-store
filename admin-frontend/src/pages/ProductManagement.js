import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { productService } from '../services/api';
import './ProductManagement.css';
import { formatAccessDenied } from '../utils/enumTranslations';
import { toastSuccess } from '../utils/toastBus';
import AutocompleteSelect from '../components/AutocompleteSelect';

// Встраиваем плейсхолдер как data URI, чтобы не было множества сетевых запросов
const PLACEHOLDER_IMAGE =
  'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjYwIiBoZWlnaHQ9IjYwIiBmaWxsPSIjZWVlIiByeD0iMTIiLz48cGF0aCBkPSJNMjAgMzAgbDggOCAxMi0xOGw4IDEyIiBzdHJva2U9IiNkZGQiIHN0cm9rZS13aWR0aD0iMyIgZmlsbD0ibm9uZSIgLz48Y2lyY2xlIGN4PSIyMyIgY3k9IjIwIiByPSI2IiBzdHJva2U9IiNkZGQiIHN0cm9rZS13aWR0aD0iMyIgZmlsbD0iI2ZmZiIvPjwvc3ZnPg==';

function ProductManagement() {
  const [products, setProducts] = useState([]);
  const [productsLoading, setProductsLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [selected, setSelected] = useState(null);
  const [modalPreviewUrl, setModalPreviewUrl] = useState('');
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
  const [catalogCategories, setCatalogCategories] = useState([]);
  const [catalogBrands, setCatalogBrands] = useState([]);
  const [editCategoryId, setEditCategoryId] = useState(null);
  const [editCategoryName, setEditCategoryName] = useState('');
  const [editBrandId, setEditBrandId] = useState(null);
  const [editBrandName, setEditBrandName] = useState('');
  const [searchProduct, setSearchProduct] = useState('');
  const [searchProductId, setSearchProductId] = useState('');
  const [searchCategory, setSearchCategory] = useState('');
  const [searchBrand, setSearchBrand] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [productEditOpen, setProductEditOpen] = useState(false);
  const [filterCategoryId, setFilterCategoryId] = useState('');
  const [filterBrandId, setFilterBrandId] = useState('');
  const [filterMinPrice, setFilterMinPrice] = useState('');
  const [filterMaxPrice, setFilterMaxPrice] = useState('');
  const navigate = useNavigate();
  const adminUserId = localStorage.getItem('adminUserId');

  const [debouncedProductSearch, setDebouncedProductSearch] = useState('');
  useEffect(() => {
    const t = setTimeout(() => setDebouncedProductSearch(searchProduct), 380);
    return () => clearTimeout(t);
  }, [searchProduct]);

  const [debouncedProductIdSearch, setDebouncedProductIdSearch] = useState('');
  useEffect(() => {
    const t = setTimeout(() => setDebouncedProductIdSearch(searchProductId.trim()), 400);
    return () => clearTimeout(t);
  }, [searchProductId]);

  const categorySelectItems = useMemo(
    () =>
      catalogCategories.map((c) => ({
        id: c.categoryId,
        label: c.categoryName || `Категория ${c.categoryId}`,
      })),
    [catalogCategories]
  );

  const brandSelectItems = useMemo(
    () =>
      catalogBrands.map((b) => ({
        id: b.brandId,
        label: b.brandName || `Бренд ${b.brandId}`,
      })),
    [catalogBrands]
  );

  /** Уникальные URL для превью в модалке (главное + галерея) */
  const adminModalThumbnails = useMemo(() => {
    if (!selected) return [];
    const main = selected.mainImageUrl;
    const list = [...(selected.images || [])].sort(
      (a, b) => (a.sortOrder || 0) - (b.sortOrder || 0)
    );
    const items = [];
    const seen = new Set();
    if (main) {
      seen.add(main);
      items.push({ key: 'main', url: main });
    }
    list.forEach((im) => {
      const u = im.imageUrl;
      if (u && !seen.has(u)) {
        seen.add(u);
        items.push({ key: im.imageId ?? u, url: u });
      }
    });
    return items;
  }, [selected]);

  useEffect(() => {
    if (!selected) {
      setModalPreviewUrl('');
      return;
    }
    const main = selected.mainImageUrl;
    const list = [...(selected.images || [])].sort(
      (a, b) => (a.sortOrder || 0) - (b.sortOrder || 0)
    );
    const seen = new Set();
    let firstExtra = '';
    list.forEach((im) => {
      const u = im.imageUrl;
      if (!u || seen.has(u)) return;
      seen.add(u);
      if (!firstExtra) firstExtra = u;
    });
    setModalPreviewUrl(main || firstExtra || PLACEHOLDER_IMAGE);
  }, [selected]);

  const loadProducts = useCallback(async () => {
    setProductsLoading(true);
    setErrorMsg('');
    try {
      const response = await productService.search({
        productId: debouncedProductIdSearch || undefined,
        name: debouncedProductSearch || undefined,
        categoryId: filterCategoryId || undefined,
        brandId: filterBrandId || undefined,
        minPrice: filterMinPrice || undefined,
        maxPrice: filterMaxPrice || undefined,
        adminUserId,
      });
      setProducts(response.data.products || []);
    } catch (error) {
      console.error('Error loading products:', error);
      setErrorMsg(formatAccessDenied(error.response?.data?.error || 'Не удалось загрузить товары'));
    } finally {
      setProductsLoading(false);
    }
  }, [
    adminUserId,
    debouncedProductIdSearch,
    debouncedProductSearch,
    filterCategoryId,
    filterBrandId,
    filterMinPrice,
    filterMaxPrice,
  ]);

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
    setProductEditOpen(false);
  };

  const loadDicts = useCallback(async () => {
    try {
      const [catRes, brandRes] = await Promise.all([
        productService.getCategories(adminUserId),
        productService.getBrands(adminUserId),
      ]);
      let cats = catRes.data || [];
      let brs = brandRes.data || [];
      setCatalogCategories(cats);
      setCatalogBrands(brs);
      if (searchCategory.trim()) {
        const q = searchCategory.trim().toLowerCase();
        cats = cats.filter((c) => c.categoryName?.toLowerCase().includes(q) || String(c.categoryId).includes(q));
      }
      if (searchBrand.trim()) {
        const q = searchBrand.trim().toLowerCase();
        brs = brs.filter((b) => b.brandName?.toLowerCase().includes(q) || String(b.brandId).includes(q));
      }
      setCategories(cats);
      setBrands(brs);
    } catch (e) {
      console.error('Error loading dicts', e);
    }
  }, [adminUserId, searchCategory, searchBrand]);

  useEffect(() => {
    if (!adminUserId) {
      navigate('/login');
      return;
    }
    loadDicts();
  }, [adminUserId, navigate, loadDicts]);

  useEffect(() => {
    if (!adminUserId) return;
    loadProducts();
  }, [adminUserId, loadProducts]);

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
      toastSuccess('Товар успешно создан');
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

  const buildFormFromProduct = (data) => ({
    name: data.name || '',
    description: data.description || '',
    price: data.price ?? '',
    stockQuantity: data.stockQuantity ?? '',
    mainImageUrl: data.mainImageUrl || '',
    categoryId: data.category?.categoryId || '',
    brandId: data.brand?.brandId || '',
  });

  const handleSelectProduct = async (productId) => {
    setDetailError('');
    setErrorMsg('');
    try {
      const res = await productService.getById(productId, adminUserId);
      setSelected(res.data);
      setShowModal(true);
      setProductEditOpen(false);
      setForm(buildFormFromProduct(res.data));
    } catch (e) {
      console.error('Error loading product', e);
      setDetailError(formatAccessDenied(e.response?.data?.error || 'Не удалось загрузить товар'));
    }
  };

  /** Выход из режима редактирования без сохранения — возвращаем поля из сервера */
  const cancelProductEdit = () => {
    if (!selected) return;
    setForm(buildFormFromProduct(selected));
    setProductEditOpen(false);
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
      toastSuccess('Успешно обновлено!');
      setProductEditOpen(false);
      await loadProducts();
      await handleSelectProduct(selected.productId);
      loadDicts();
    } catch (e) {
      console.error('Error updating product', e);
      setErrorMsg(formatAccessDenied(e.response?.data?.error || 'Не удалось обновить товар'));
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
      toastSuccess('Изображение добавлено в галерею');
    } catch (e) {
      console.error('Upload gallery image failed', e);
      alert('Не удалось загрузить фото галереи');
    }
  };

  const handleAddCategory = async () => {
    if (!categoryName.trim()) return;
    try {
      await productService.addCategory(categoryName.trim(), adminUserId);
      toastSuccess('Категория создана');
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
      toastSuccess('Бренд создан');
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
      toastSuccess('Бренд обновлён');
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
      toastSuccess('Категория обновлена');
    } catch (e) {
      console.error('Update category failed', e);
      alert('Не удалось обновить категорию');
    }
  };

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
      <div className="search-row filters-row-layout">
        <AutocompleteSelect
          label="Фильтр: категория"
          placeholder="Название категории или ID…"
          items={categorySelectItems}
          value={filterCategoryId}
          onChange={setFilterCategoryId}
          emptyText="Категорий не найдено — проверьте справочник ниже"
        />
        <AutocompleteSelect
          label="Фильтр: бренд"
          placeholder="Название бренда или ID…"
          items={brandSelectItems}
          value={filterBrandId}
          onChange={setFilterBrandId}
          emptyText="Брендов не найдено — проверьте справочник ниже"
        />
        <div className="price-range-inputs">
          <label className="price-inline">
            Мин. цена
            <input
              placeholder="От"
              value={filterMinPrice}
              onChange={(e) => setFilterMinPrice(e.target.value)}
              type="number"
              step="0.01"
            />
          </label>
          <label className="price-inline">
            Макс. цена
            <input
              placeholder="До"
              value={filterMaxPrice}
              onChange={(e) => setFilterMaxPrice(e.target.value)}
              type="number"
              step="0.01"
            />
          </label>
        </div>
      </div>
      {productsLoading && <div className="products-inline-loading">Обновление списка товаров…</div>}
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
        <div className="admin-product-create-shell">
          <form className="product-form admin-product-create-card" onSubmit={handleCreate}>
            <div className="admin-product-create-card__accent" aria-hidden />
            <div className="admin-product-create-card__head">
              <h2 className="admin-product-create-card__title">Новый товар</h2>
              <p className="admin-product-create-card__subtitle">
                Укажите название, цену и остаток — остальное можно дополнить позже. После создания товар появится в таблице.
              </p>
            </div>
            <div className="form-row">
              <label>Название</label>
              <input name="name" value={form.name} onChange={handleChange} required placeholder="Например, Кроссовки Urban" />
            </div>
            <div className="form-row">
              <label>Описание</label>
              <textarea
                name="description"
                value={form.description}
                onChange={handleChange}
                rows={4}
                placeholder="Краткое описание для каталога..."
              />
            </div>
            <div className="form-row form-row--split">
              <div>
                <label>Цена, р.</label>
                <input
                  type="number"
                  step="0.01"
                  name="price"
                  value={form.price}
                  onChange={handleChange}
                  required
                  placeholder="0"
                />
              </div>
              <div>
                <label>Остаток на складе</label>
                <input
                  type="number"
                  name="stockQuantity"
                  value={form.stockQuantity}
                  onChange={handleChange}
                  required
                  placeholder="0"
                />
              </div>
            </div>
            <div className="form-row">
              <label>Главное изображение — URL</label>
              <input name="mainImageUrl" value={form.mainImageUrl} onChange={handleChange} placeholder="https://…" />
              <span className="admin-product-create-card__file-hint">или загрузите файл</span>
              <input type="file" accept="image/*" onChange={(e) => e.target.files[0] && uploadMainImage(e.target.files[0])} />
            </div>
            <div className="form-row">
              <AutocompleteSelect
                label="Категория"
                placeholder="Выберите или найдите по названию"
                items={categorySelectItems}
                value={form.categoryId ? String(form.categoryId) : ''}
                onChange={(id) =>
                  setForm((prev) => ({ ...prev, categoryId: id ? String(id) : '' }))
                }
              />
            </div>
            <div className="form-row">
              <AutocompleteSelect
                label="Бренд"
                placeholder="Выберите или найдите по названию"
                items={brandSelectItems}
                value={form.brandId ? String(form.brandId) : ''}
                onChange={(id) =>
                  setForm((prev) => ({ ...prev, brandId: id ? String(id) : '' }))
                }
              />
            </div>
            <div className="admin-product-create-card__footer">
              <button type="submit" className="admin-product-create-card__submit" disabled={saving}>
                {saving ? 'Создание…' : 'Создать товар'}
              </button>
            </div>
          </form>
        </div>
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
              <td>{product.price} p.</td>
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
        <div className="modal-backdrop">
          <div
            className="modal-card admin-product-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="admin-product-modal-title"
          >
            <header className="admin-product-modal__header">
              <div className="admin-product-modal__header-top">
                <p className="admin-product-modal__eyebrow">Карточка товара · ID {selected.productId}</p>
                <button
                  type="button"
                  className="admin-product-modal__sheet-close"
                  onClick={closeDetail}
                  aria-label="Закрыть карточку"
                >
                  <span className="admin-product-modal__sheet-close-x" aria-hidden>
                    ×
                  </span>
                  <span className="admin-product-modal__sheet-close-text">Закрыть</span>
                </button>
              </div>
              <h2 id="admin-product-modal-title" className="admin-product-modal__title">
                {form.name || selected.name}
              </h2>
              <div className="admin-product-modal__header-actions">
                {!productEditOpen ? (
                  <button type="button" className="admin-product-edit-toggle" onClick={() => setProductEditOpen(true)}>
                    Редактировать
                  </button>
                ) : (
                  <>
                    <span className="admin-product-edit-toggle admin-product-edit-toggle--active" role="status">
                      Редактирование
                    </span>
                    <button type="button" className="admin-product-edit-cancel" onClick={cancelProductEdit}>
                      Отмена
                    </button>
                  </>
                )}
              </div>
              <div className="admin-product-modal__badges">
                <span className="admin-badge admin-badge--category">
                  {categorySelectItems.find((c) => String(c.id) === String(form.categoryId))?.label ||
                    selected.category?.categoryName ||
                    'Категория не выбрана'}
                </span>
                <span className="admin-badge admin-badge--brand">
                  {brandSelectItems.find((b) => String(b.id) === String(form.brandId))?.label ||
                    selected.brand?.brandName ||
                    'Бренд не выбран'}
                </span>
                <span className="admin-badge admin-badge--meta">На складе: {form.stockQuantity ?? selected.stockQuantity} шт.</span>
                <span className="admin-badge admin-badge--price">{form.price ?? selected.price} р.</span>
              </div>
            </header>

            <div className="admin-product-modal__grid">
              <section className="admin-product-modal__media" aria-label="Изображения">
                <div className="admin-product-modal__preview">
                  <img
                    src={modalPreviewUrl || selected.mainImageUrl || PLACEHOLDER_IMAGE}
                    alt=""
                    className="admin-product-modal__preview-img"
                    onError={(e) => {
                      e.target.onerror = null;
                      e.target.src = PLACEHOLDER_IMAGE;
                    }}
                  />
                </div>
                {adminModalThumbnails.length > 0 && (
                  <>
                    <p className="admin-product-modal__thumb-hint">Галерея — нажмите миниатюру для предпросмотра</p>
                    <div className="admin-product-modal__thumb-row">
                      {adminModalThumbnails.map((t) => (
                        <button
                          key={t.key}
                          type="button"
                          className={
                            modalPreviewUrl === t.url ? 'admin-product-thumb admin-product-thumb--active' : 'admin-product-thumb'
                          }
                          onClick={() => setModalPreviewUrl(t.url)}
                        >
                          <img src={t.url} alt="" onError={(e) => { e.target.onerror = null; e.target.src = PLACEHOLDER_IMAGE; }} />
                        </button>
                      ))}
                    </div>
                  </>
                )}
              </section>

              <section className="admin-product-modal__editor">
                {!productEditOpen ? (
                  <div className="admin-product-modal__readonly">
                    <h3 className="admin-product-modal__section-title">Описание</h3>
                    <p className="admin-product-readonly-text">{selected.description?.trim() || 'Описание не заполнено.'}</p>
                  </div>
                ) : (
                  <form
                    id="admin-product-edit-form"
                    className="admin-product-modal__edit-form"
                    onSubmit={handleUpdate}
                  >
                    <div className="admin-product-modal__edit-block product-form admin-product-form admin-product-form--elevated">
                      <h3 className="admin-product-modal__section-title">Данные товара</h3>
                      <div className="form-row">
                        <label>Название</label>
                        <input name="name" value={form.name} onChange={handleChange} required />
                      </div>
                      <div className="form-row">
                        <label>Описание</label>
                        <textarea name="description" value={form.description} rows={4} onChange={handleChange} />
                      </div>
                      <div className="form-row form-row--split">
                        <div>
                          <label>Цена, р.</label>
                          <input
                            type="number"
                            step="0.01"
                            name="price"
                            value={form.price}
                            onChange={handleChange}
                            required
                          />
                        </div>
                        <div>
                          <label>Остаток</label>
                          <input
                            type="number"
                            name="stockQuantity"
                            value={form.stockQuantity}
                            onChange={handleChange}
                            required
                          />
                        </div>
                      </div>
                      <div className="form-row">
                        <label>URL главного изображения</label>
                        <input name="mainImageUrl" value={form.mainImageUrl} onChange={handleChange} />
                        <input type="file" accept="image/*" onChange={(e) => e.target.files[0] && uploadMainImage(e.target.files[0])} />
                      </div>
                      <div className="form-row">
                        <AutocompleteSelect
                          label="Категория"
                          placeholder="Выберите категорию"
                          items={categorySelectItems}
                          value={form.categoryId ? String(form.categoryId) : ''}
                          onChange={(id) =>
                            setForm((prev) => ({ ...prev, categoryId: id ? String(id) : '' }))
                          }
                        />
                      </div>
                      <div className="form-row">
                        <AutocompleteSelect
                          label="Бренд"
                          placeholder="Выберите бренд"
                          items={brandSelectItems}
                          value={form.brandId ? String(form.brandId) : ''}
                          onChange={(id) =>
                            setForm((prev) => ({ ...prev, brandId: id ? String(id) : '' }))
                          }
                        />
                      </div>
                    </div>

                    <div className="admin-product-modal__edit-block product-form admin-product-gallery-upload admin-product-form--elevated">
                      <h4 className="admin-product-modal__section-sub">Добавить фото в галерею</h4>
                      <div className="form-row-inline">
                        <label className="admin-file-inline">
                          Файл
                          <input type="file" accept="image/*" onChange={(e) => e.target.files[0] && uploadGalleryImage(e.target.files[0])} />
                        </label>
                        <label>
                          Порядок сортировки
                          <input
                            type="number"
                            placeholder="Авто"
                            value={gallerySort}
                            onChange={(e) => setGallerySort(e.target.value)}
                          />
                        </label>
                      </div>
                    </div>

                    <div className="admin-product-modal__save-footer">
                      <button type="submit" className="admin-product-save-btn" disabled={saving}>
                        {saving ? 'Сохранение…' : 'Сохранить'}
                      </button>
                    </div>
                  </form>
                )}
              </section>
            </div>
          </div>
        </div>
      )}

      <div className="product-form">
        <details open>
          <summary><h4 style={{ display: 'inline', margin: 0 }}>Добавить категорию</h4></summary>
          <div className="form-row-inline">
            <input
              placeholder="Уточнить список категории (по имени или id)"
              value={searchCategory}
              onChange={(e) => setSearchCategory(e.target.value)}
            />
            <input
              placeholder="Название новой категории"
              value={categoryName}
              onChange={(e) => setCategoryName(e.target.value)}
            />
            <button type="button" onClick={loadDicts}>Применить фильтр списка</button>
            <button type="button" onClick={() => { setSearchCategory(''); loadDicts(); }}>Очистить фильтр</button>
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
              placeholder="Уточнить список брендов (по имени или id)"
              value={searchBrand}
              onChange={(e) => setSearchBrand(e.target.value)}
            />
            <input
              placeholder="Название нового бренда"
              value={brandName}
              onChange={(e) => setBrandName(e.target.value)}
            />
            <button type="button" onClick={loadDicts}>Применить фильтр списка</button>
            <button type="button" onClick={() => { setSearchBrand(''); loadDicts(); }}>Очистить фильтр</button>
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




import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { cityService, warehouseService } from '../services/api';
import AutocompleteSelect from '../components/AutocompleteSelect';
import './CityManagement.css';
import { formatAccessDenied } from '../utils/enumTranslations';
import { toastSuccess } from '../utils/toastBus';

function CityManagement() {
  const [cities, setCities] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [cityName, setCityName] = useState('');
  const [routePayload, setRoutePayload] = useState({
    cityAId: '',
    cityBId: '',
    distanceKm: '',
  });
  const [editCityId, setEditCityId] = useState(null);
  const [editCityName, setEditCityName] = useState('');
  const [editRouteId, setEditRouteId] = useState(null);
  const [editRoutePayload, setEditRoutePayload] = useState({
    cityAId: '',
    cityBId: '',
    distanceKm: '',
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [searchCity, setSearchCity] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const [warehouses, setWarehouses] = useState([]);
  const [warehouseForm, setWarehouseForm] = useState({ name: '', cityId: '', address: '' });
  const [editWarehouseId, setEditWarehouseId] = useState(null);
  const [editWarehouseForm, setEditWarehouseForm] = useState({ name: '', cityId: '', address: '' });
  const [warehouseSearchName, setWarehouseSearchName] = useState('');
  const [warehouseFilterCityId, setWarehouseFilterCityId] = useState('');
  const [routeFilterCityA, setRouteFilterCityA] = useState('');
  const [routeFilterCityB, setRouteFilterCityB] = useState('');
  const navigate = useNavigate();
  const adminUserId = localStorage.getItem('adminUserId');

  const citySelectItems = useMemo(
    () =>
      cities.map((c) => ({
        id: c.cityId,
        label: (c.cityName || '').trim() || 'Без названия',
      })),
    [cities]
  );

  const filteredWarehouses = useMemo(() => {
    const q = warehouseSearchName.trim().toLowerCase();
    const cityId = warehouseFilterCityId ? String(warehouseFilterCityId) : '';
    return warehouses.filter((w) => {
      const nm = (w.name || w.warehouseName || '').toLowerCase();
      const matchName = !q || nm.includes(q);
      const wCity = w.city?.cityId ?? w.cityId;
      const matchCity = !cityId || String(wCity) === cityId;
      return matchName && matchCity;
    });
  }, [warehouses, warehouseSearchName, warehouseFilterCityId]);

  /** Один город — маршруты, где он на любом конце; два города — только связь между ними (в любую сторону). */
  const filteredRoutes = useMemo(() => {
    const a = routeFilterCityA ? Number(routeFilterCityA) : null;
    const b = routeFilterCityB ? Number(routeFilterCityB) : null;
    return routes.filter((r) => {
      const ra = r.cityA?.cityId;
      const rb = r.cityB?.cityId;
      if (a && b) {
        return (ra === a && rb === b) || (ra === b && rb === a);
      }
      if (a) return ra === a || rb === a;
      if (b) return ra === b || rb === b;
      return true;
    });
  }, [routes, routeFilterCityA, routeFilterCityB]);

  const loadData = useCallback(async (cityQuery = '') => {
    setLoading(true);
    setErrorMsg('');
    try {
      const [c, r, w] = await Promise.all([
        cityQuery.trim()
          ? cityService.search(cityQuery.trim(), adminUserId)
          : cityService.getAll(adminUserId),
        cityService.getRoutes(adminUserId),
        warehouseService.getAll(adminUserId),
      ]);
      setCities(c.data || []);
      setRoutes(r.data || []);
      setWarehouses(Array.isArray(w.data) ? w.data : []);
    } catch (e) {
      console.error('Error loading cities/routes', e);
      setErrorMsg(formatAccessDenied(e.response?.data?.error || 'Нет доступа: требуется отдел PRODUCT_MANAGE или ORDER_MANAGE / неверный adminUserId'));
    } finally {
      setLoading(false);
    }
  }, [adminUserId]);

  useEffect(() => {
    if (!adminUserId) {
      navigate('/login');
      return;
    }
    loadData('');
  }, [adminUserId, navigate, loadData]);

  const addCity = async () => {
    if (!cityName.trim()) return;
    setSaving(true);
    try {
      await cityService.create(cityName.trim(), adminUserId);
      setCityName('');
      await loadData(searchCity);
      toastSuccess('Город добавлен');
    } catch (e) {
      console.error(e);
      alert(formatAccessDenied(e.response?.data?.error || 'Не удалось создать город'));
    } finally {
      setSaving(false);
    }
  };

  const saveCity = async () => {
    if (!editCityId || !editCityName.trim()) return;
    setSaving(true);
    try {
      await cityService.update(editCityId, editCityName.trim(), adminUserId);
      setEditCityId(null);
      setEditCityName('');
      await loadData(searchCity);
      toastSuccess('Город обновлён');
    } catch (e) {
      console.error(e);
      alert(formatAccessDenied(e.response?.data?.error || 'Не удалось обновить город'));
    } finally {
      setSaving(false);
    }
  };

  const addRoute = async () => {
    const cityAId = Number(routePayload.cityAId);
    const cityBId = Number(routePayload.cityBId);
    const distanceKm = Number(routePayload.distanceKm);
    if (!cityAId || !cityBId || !distanceKm) return;
    setSaving(true);
    try {
      await cityService.createRoute(
        {
          cityA: { cityId: cityAId },
          cityB: { cityId: cityBId },
          distanceKm,
        },
        adminUserId
      );
      setRoutePayload({ cityAId: '', cityBId: '', distanceKm: '' });
      await loadData(searchCity);
      toastSuccess('Маршрут добавлен');
    } catch (e) {
      console.error(e);
      alert(formatAccessDenied(e.response?.data?.error || 'Не удалось создать маршрут'));
    } finally {
      setSaving(false);
    }
  };

  const saveRoute = async () => {
    if (!editRouteId) return;
    const cityAId = Number(editRoutePayload.cityAId);
    const cityBId = Number(editRoutePayload.cityBId);
    const distanceKm = Number(editRoutePayload.distanceKm);
    if (!cityAId || !cityBId || !distanceKm) return;
    setSaving(true);
    try {
      await cityService.updateRoute(
        editRouteId,
        { cityA: { cityId: cityAId }, cityB: { cityId: cityBId }, distanceKm },
        adminUserId
      );
      setEditRouteId(null);
      setEditRoutePayload({ cityAId: '', cityBId: '', distanceKm: '' });
      await loadData(searchCity);
      toastSuccess('Маршрут обновлён');
    } catch (e) {
      console.error(e);
      alert(formatAccessDenied(e.response?.data?.error || 'Не удалось обновить маршрут'));
    } finally {
      setSaving(false);
    }
  };

  const addWarehouse = async () => {
    const name = (warehouseForm.name || '').trim();
    if (!name || !warehouseForm.cityId) return;
    setSaving(true);
    try {
      await warehouseService.create(
        {
          name,
          city: { cityId: Number(warehouseForm.cityId) },
          address: warehouseForm.address || '',
        },
        adminUserId
      );
      setWarehouseForm({ name: '', cityId: '', address: '' });
      await loadData(searchCity);
      toastSuccess('Склад добавлен');
    } catch (e) {
      console.error(e);
      alert(formatAccessDenied(e.response?.data?.error || 'Не удалось создать склад'));
    } finally {
      setSaving(false);
    }
  };

  const saveWarehouse = async () => {
    const name = (editWarehouseForm.name || '').trim();
    if (!editWarehouseId || !name || !editWarehouseForm.cityId) return;
    setSaving(true);
    try {
      await warehouseService.update(
        editWarehouseId,
        {
          name,
          city: { cityId: Number(editWarehouseForm.cityId) },
          address: editWarehouseForm.address || '',
        },
        adminUserId
      );
      setEditWarehouseId(null);
      setEditWarehouseForm({ name: '', cityId: '', address: '' });
      await loadData(searchCity);
      toastSuccess('Склад обновлён');
    } catch (e) {
      console.error(e);
      alert(formatAccessDenied(e.response?.data?.error || 'Не удалось обновить склад'));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="city-management">
      <h1>Города, маршруты и склады</h1>
      {loading && <div className="city-soft-loading">Загрузка данных…</div>}
      {errorMsg && <div className="error-msg">{errorMsg}</div>}
      <details open>
        <summary>Города</summary>
        <div className="form-block">
          <h3>Поиск городов</h3>
          <div className="form-row">
            <input
              placeholder="Поиск по названию или id"
              value={searchCity}
              onChange={(e) => setSearchCity(e.target.value)}
            />
            <button onClick={() => loadData(searchCity)}>Искать</button>
          </div>
        </div>

        <div className="form-block">
          <h3>Добавить город</h3>
          <div className="form-row">
            <input
              placeholder="Название города"
              value={cityName}
              onChange={(e) => setCityName(e.target.value)}
            />
            <button onClick={addCity} disabled={saving}>Добавить</button>
          </div>
        </div>

        <div className="list-block">
          <h3>Список городов</h3>
          <div className="list-box">
            {cities.map((c) => (
              <div className="list-row" key={c.cityId}>
                <span>{c.cityId} — {c.cityName}</span>
                <div className="form-row">
                <button onClick={() => { setEditCityId(c.cityId); setEditCityName(c.cityName || ''); }}>Редактировать</button>
                </div>
              </div>
            ))}
          </div>
          {editCityId && (
            <div className="form-row">
              <input
                value={editCityName}
                onChange={(e) => setEditCityName(e.target.value)}
              />
              <button onClick={saveCity} disabled={saving}>Сохранить город</button>
            </div>
          )}
        </div>
      </details>

      <details>
        <summary>Маршруты</summary>
        <div className="form-block">
          <h3>Добавить маршрут</h3>
          <div className="form-row routes-autocomplete-grid">
            <AutocompleteSelect
              label="Начальный город"
              placeholder="Выберите или найдите по названию"
              items={citySelectItems}
              value={routePayload.cityAId ? String(routePayload.cityAId) : ''}
              emptyText={cities.length ? 'Ничего не найдено' : 'Загрузите список городов'}
              onChange={(id) =>
                setRoutePayload((p) => ({ ...p, cityAId: id }))
              }
            />
            <AutocompleteSelect
              label="Конечный город"
              placeholder="Выберите или найдите по названию"
              items={citySelectItems}
              value={routePayload.cityBId ? String(routePayload.cityBId) : ''}
              emptyText={cities.length ? 'Ничего не найдено' : 'Загрузите список городов'}
              onChange={(id) =>
                setRoutePayload((p) => ({ ...p, cityBId: id }))
              }
            />
            <label className="distance-field">
              Расстояние, км
              <input
                type="number"
                placeholder="Например 120"
                value={routePayload.distanceKm}
                onChange={(e) => setRoutePayload((p) => ({ ...p, distanceKm: e.target.value }))}
              />
            </label>
            <button onClick={addRoute} disabled={saving}>Добавить маршрут</button>
          </div>
        </div>

        <div className="list-block">
          <h3>Список маршрутов</h3>
          <div className="form-block city-list-filters">
            <h4 className="city-list-filters__title">Поиск маршрута по городам</h4>
            <p className="city-list-filters__hint">
              Один город — все маршруты, где он указан. Два города — только прямое расстояние между этой парой.
            </p>
            <div className="form-row routes-autocomplete-grid">
              <AutocompleteSelect
                label="Город (любой конец)"
                placeholder="Не фильтровать"
                items={citySelectItems}
                value={routeFilterCityA ? String(routeFilterCityA) : ''}
                emptyText={cities.length ? 'Ничего не найдено' : 'Нет городов'}
                onChange={(id) => setRouteFilterCityA(id || '')}
              />
              <AutocompleteSelect
                label="Второй город (уточнение пары)"
                placeholder="Не обязательно"
                items={citySelectItems}
                value={routeFilterCityB ? String(routeFilterCityB) : ''}
                emptyText={cities.length ? 'Ничего не найдено' : 'Нет городов'}
                onChange={(id) => setRouteFilterCityB(id || '')}
              />
              <button
                type="button"
                className="city-filter-reset"
                onClick={() => {
                  setRouteFilterCityA('');
                  setRouteFilterCityB('');
                }}
              >
                Сбросить фильтр
              </button>
            </div>
            <p className="city-list-filters__meta">
              Показано маршрутов: {filteredRoutes.length}
              {filteredRoutes.length !== routes.length ? ` из ${routes.length}` : ''}
            </p>
          </div>
          <div className="list-box">
            {filteredRoutes.map((r) => (
              <div className="list-row" key={r.routeId}>
                <span>#{r.routeId}: {r.cityA?.cityName || r.cityA?.cityId} → {r.cityB?.cityName || r.cityB?.cityId}</span>
                <span>{r.distanceKm} км</span>
                <div className="form-row">
                <button className="form-row button" onClick={() => {
                  setEditRouteId(r.routeId);
                  setEditRoutePayload({
                    cityAId: r.cityA?.cityId || '',
                    cityBId: r.cityB?.cityId || '',
                    distanceKm: r.distanceKm || '',
                  });
                }}>Редактировать</button>
                
              </div>
              </div>
            ))}
          </div>
          {editRouteId && (
            <div className="form-row routes-autocomplete-grid">
              <AutocompleteSelect
                label="Начальный город"
                items={citySelectItems}
                value={editRoutePayload.cityAId ? String(editRoutePayload.cityAId) : ''}
                onChange={(id) =>
                  setEditRoutePayload((p) => ({ ...p, cityAId: id }))
                }
              />
              <AutocompleteSelect
                label="Конечный город"
                items={citySelectItems}
                value={editRoutePayload.cityBId ? String(editRoutePayload.cityBId) : ''}
                onChange={(id) =>
                  setEditRoutePayload((p) => ({ ...p, cityBId: id }))
                }
              />
              <label className="distance-field">
                Расстояние, км
                <input
                  type="number"
                  value={editRoutePayload.distanceKm}
                  onChange={(e) => setEditRoutePayload((p) => ({ ...p, distanceKm: e.target.value }))}
                />
              </label>
              <button onClick={saveRoute} disabled={saving}>Сохранить маршрут</button>
            </div>
          )}
        </div>
      </details>

      <details>
        <summary>Склады</summary>
        <div className="form-block">
          <h3>Добавить склад</h3>
          <div className="form-row">
            <input
              placeholder="Название склада"
              value={warehouseForm.name}
              onChange={(e) => setWarehouseForm((p) => ({ ...p, name: e.target.value }))}
            />
            <AutocompleteSelect
              label="Город склада"
              placeholder="Выберите город из списка"
              items={citySelectItems}
              value={warehouseForm.cityId ? String(warehouseForm.cityId) : ''}
              onChange={(id) => setWarehouseForm((p) => ({ ...p, cityId: id }))}
            />

            <input
              placeholder="Адрес"
              value={warehouseForm.address}
              onChange={(e) => setWarehouseForm((p) => ({ ...p, address: e.target.value }))}
            />
            <button onClick={addWarehouse} disabled={saving}>Добавить склад</button>
          </div>
        </div>

        <div className="list-block">
          <h3>Список складов</h3>
          <div className="form-block city-list-filters">
            <h4 className="city-list-filters__title">Поиск склада</h4>
            <div className="form-row city-warehouse-search-row">
              <label className="city-warehouse-search-label">
                Название склада
                <input
                  type="search"
                  placeholder="Часть названия…"
                  value={warehouseSearchName}
                  onChange={(e) => setWarehouseSearchName(e.target.value)}
                />
              </label>
              <AutocompleteSelect
                label="Город"
                placeholder="Все города"
                items={citySelectItems}
                value={warehouseFilterCityId ? String(warehouseFilterCityId) : ''}
                emptyText={cities.length ? 'Ничего не найдено' : 'Нет городов'}
                onChange={(id) => setWarehouseFilterCityId(id || '')}
              />
              <button
                type="button"
                className="city-filter-reset"
                onClick={() => {
                  setWarehouseSearchName('');
                  setWarehouseFilterCityId('');
                }}
              >
                Сбросить
              </button>
            </div>
            <p className="city-list-filters__meta">
              Показано складов: {filteredWarehouses.length}
              {filteredWarehouses.length !== warehouses.length ? ` из ${warehouses.length}` : ''}
            </p>
          </div>
          <div className="list-box">
            {filteredWarehouses.map((w) => (
              <div className="list-row" key={w.id || w.warehouseId}>
                <span>
                  {w.id || w.warehouseId} — {w.name || w.warehouseName || 'Склад'} (
                  {w.city?.cityName || w.cityName || 'город не указан'})
                </span>
                <div className="form-row">
                <button className="form-row button" onClick={() => {
                  setEditWarehouseId(w.id || w.warehouseId);
                  setEditWarehouseForm({
                    name: w.name || w.warehouseName || '',
                    cityId: w.city?.cityId || w.cityId || '',
                    address: w.address || '',
                  });
                }}>Редактировать</button>
              </div>
              </div>
            ))}
          </div>
          {editWarehouseId && (
            <div className="form-row">
              <input
                placeholder="Название склада"
              value={editWarehouseForm.name}
              onChange={(e) => setEditWarehouseForm((p) => ({ ...p, name: e.target.value }))}
              />
              <AutocompleteSelect
                label="Город склада"
                items={citySelectItems}
                value={editWarehouseForm.cityId ? String(editWarehouseForm.cityId) : ''}
                onChange={(id) => setEditWarehouseForm((p) => ({ ...p, cityId: id }))}
              />
              <input
                placeholder="Адрес"
                value={editWarehouseForm.address}
                onChange={(e) => setEditWarehouseForm((p) => ({ ...p, address: e.target.value }))}
              />
              <button onClick={saveWarehouse} disabled={saving}>Сохранить склад</button>
            </div>
          )}
        </div>
      </details>
    </div>
  );
}

export default CityManagement;


import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { cityService, warehouseService } from '../services/api';
import './CityManagement.css';

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
  const navigate = useNavigate();
  const adminUserId = localStorage.getItem('adminUserId');

  useEffect(() => {
    if (!adminUserId) {
      navigate('/login');
      return;
    }
    loadData();
  }, [adminUserId, navigate]);

  const loadData = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const [c, r, w] = await Promise.all([
        searchCity.trim()
          ? cityService.search(searchCity.trim(), adminUserId)
          : cityService.getAll(adminUserId),
        cityService.getRoutes(adminUserId),
        warehouseService.getAll(adminUserId),
      ]);
      setCities(c.data || []);
      setRoutes(r.data || []);
      setWarehouses(Array.isArray(w.data) ? w.data : []);
    } catch (e) {
      console.error('Error loading cities/routes', e);
      setErrorMsg(e.response?.data?.error || 'Нет доступа: требуется отдел PRODUCT_MANAGE или ORDER_MANAGE / неверный adminUserId');
    } finally {
      setLoading(false);
    }
  };

  const addCity = async () => {
    if (!cityName.trim()) return;
    setSaving(true);
    try {
      await cityService.create(cityName.trim(), adminUserId);
      setCityName('');
      await loadData();
    } catch (e) {
      console.error('Add city failed', e);
      alert('Не удалось создать город');
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
      await loadData();
    } catch (e) {
      console.error('Update city failed', e);
      alert('Не удалось обновить город');
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
      await loadData();
    } catch (e) {
      console.error('Add route failed', e);
      alert('Не удалось создать маршрут');
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
      await loadData();
    } catch (e) {
      console.error('Update route failed', e);
      alert('Не удалось обновить маршрут');
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
      await loadData();
    } catch (e) {
      console.error('Add warehouse failed', e);
      alert('Не удалось создать склад');
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
      await loadData();
    } catch (e) {
      console.error('Update warehouse failed', e);
      alert('Не удалось обновить склад');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div>Загрузка...</div>;

  return (
    <div className="city-management">
      <h1>Города, маршруты и склады</h1>
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
            <button onClick={loadData}>Искать</button>
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
                <button onClick={() => { setEditCityId(c.cityId); setEditCityName(c.cityName || ''); }}>Редактировать</button>
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
          <div className="form-row">
            <input
              type="number"
              placeholder="cityAId"
              value={routePayload.cityAId}
              onChange={(e) => setRoutePayload((p) => ({ ...p, cityAId: e.target.value }))}
            />
            <input
              type="number"
              placeholder="cityBId"
              value={routePayload.cityBId}
              onChange={(e) => setRoutePayload((p) => ({ ...p, cityBId: e.target.value }))}
            />
            <input
              type="number"
              placeholder="distanceKm"
              value={routePayload.distanceKm}
              onChange={(e) => setRoutePayload((p) => ({ ...p, distanceKm: e.target.value }))}
            />
            <button onClick={addRoute} disabled={saving}>Добавить маршрут</button>
          </div>
        </div>

        <div className="list-block">
          <h3>Список маршрутов</h3>
          <div className="list-box">
            {routes.map((r) => (
              <div className="list-row" key={r.routeId}>
                <span>#{r.routeId}: {r.cityA?.cityName || r.cityA?.cityId} → {r.cityB?.cityName || r.cityB?.cityId}</span>
                <span>{r.distanceKm} км</span>
                <button onClick={() => {
                  setEditRouteId(r.routeId);
                  setEditRoutePayload({
                    cityAId: r.cityA?.cityId || '',
                    cityBId: r.cityB?.cityId || '',
                    distanceKm: r.distanceKm || '',
                  });
                }}>Редактировать</button>
              </div>
            ))}
          </div>
          {editRouteId && (
            <div className="form-row">
              <input
                type="number"
                placeholder="cityAId"
                value={editRoutePayload.cityAId}
                onChange={(e) => setEditRoutePayload((p) => ({ ...p, cityAId: e.target.value }))}
              />
              <input
                type="number"
                placeholder="cityBId"
                value={editRoutePayload.cityBId}
                onChange={(e) => setEditRoutePayload((p) => ({ ...p, cityBId: e.target.value }))}
              />
              <input
                type="number"
                placeholder="distanceKm"
                value={editRoutePayload.distanceKm}
                onChange={(e) => setEditRoutePayload((p) => ({ ...p, distanceKm: e.target.value }))}
              />
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
            <select
              value={warehouseForm.cityId}
              onChange={(e) => setWarehouseForm((p) => ({ ...p, cityId: e.target.value }))}
            >
              <option value="">Город</option>
              {cities.map((c) => (
                <option key={c.cityId} value={c.cityId}>{c.cityName} (ID {c.cityId})</option>
              ))}
            </select>
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
          <div className="list-box">
            {warehouses.map((w) => (
              <div className="list-row" key={w.id || w.warehouseId}>
                <span>
                  #{w.id || w.warehouseId}: {(w.name || w.warehouseName || '—')} — {(w.city?.cityName || w.cityName || '—')}
                </span>
                <button onClick={() => {
                  setEditWarehouseId(w.id || w.warehouseId);
                  setEditWarehouseForm({
                    name: w.name || w.warehouseName || '',
                    cityId: w.city?.cityId || w.cityId || '',
                    address: w.address || '',
                  });
                }}>Редактировать</button>
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
              <select
                value={editWarehouseForm.cityId}
                onChange={(e) => setEditWarehouseForm((p) => ({ ...p, cityId: e.target.value }))}
              >
                <option value="">Город</option>
                {cities.map((c) => (
                  <option key={c.cityId} value={c.cityId}>{c.cityName} (ID {c.cityId})</option>
                ))}
              </select>
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


import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { userService } from '../services/api';
import './UserManagement.css';
import { formatAccessDenied, translateDepartment, translateGender, translateRole } from '../utils/enumTranslations';
import { toastSuccess } from '../utils/toastBus';
import { formatAdminDateTime } from '../utils/formatAdminDateTime';

const USER_PLACEHOLDER = '/placeholder.png';

/** Бэкенд отдаёт полный URL; при относительном пути подставляем origin SPA */
function avatarSrc(url) {
  if (!url || !String(url).trim()) return USER_PLACEHOLDER;
  const s = String(url).trim();
  if (s.startsWith('http://') || s.startsWith('https://')) return s;
  if (s.startsWith('/')) return `${window.location.origin}${s}`;
  return s;
}

function CardAvatar({ picUrl, fallbackLetter }) {
  const letter = fallbackLetter?.toUpperCase() || '?';
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    setFailed(false);
  }, [picUrl]);

  const raw = picUrl?.trim?.();
  if (!raw || failed) {
    return (
      <div className="user-card__thumb-wrap">
        <span className="user-card__thumb-letter visible">{letter}</span>
      </div>
    );
  }

  return (
    <div className="user-card__thumb-wrap">
      <img
        className="user-card__thumb-img"
        src={avatarSrc(raw)}
        alt=""
        onError={() => setFailed(true)}
      />
    </div>
  );
}

function UserManagement() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [mode, setMode] = useState('customers');
  const [detail, setDetail] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [toggling, setToggling] = useState(false);
  const [search, setSearch] = useState('');
  const [searchId, setSearchId] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [cardTogglingId, setCardTogglingId] = useState(null);

  const navigate = useNavigate();
  const adminUserId = localStorage.getItem('adminUserId');

  useEffect(() => {
    if (!adminUserId) navigate('/login');
  }, [adminUserId, navigate]);

  const reloadCurrentList = useCallback(async (currentMode, override = {}) => {
    if (!adminUserId) return;
    setLoading(true);
    setErrorMsg('');
    try {
      const m = currentMode ?? mode;
      if (m === 'admins') {
        const res = await userService.getByRole('ROLE_ADMIN', adminUserId, 0, 80);
        setUsers(res.data.users || []);
      } else {
        const byIdRaw = (override.searchId !== undefined ? override.searchId : searchId ?? '').trim();
        const byTextRaw = (override.search !== undefined ? override.search : search ?? '').trim();
        let list = [];
        if (override.idOnly) {
          if (!byIdRaw) {
            const res = await userService.getByRole('ROLE_CUSTOMER', adminUserId, 0, 120);
            list = res.data.users || [];
          } else {
            const res = await userService.getById(byIdRaw, adminUserId);
            list = [res.data];
          }
        } else if (byTextRaw) {
          const resSearch = await userService.search(byTextRaw, adminUserId, 0, 100);
          list = resSearch.data.users || [];
        } else if (byIdRaw) {
          const res = await userService.getById(byIdRaw, adminUserId);
          list = [res.data];
        } else {
          const res = await userService.getByRole('ROLE_CUSTOMER', adminUserId, 0, 120);
          list = res.data.users || [];
        }
        list.sort((a, b) => Number(b.isActive) - Number(a.isActive));
        setUsers(list);
      }
    } catch (error) {
      console.error('Error loading users:', error);
      setErrorMsg(formatAccessDenied(error.response?.data?.error || 'Нет доступа: отдел USER_MANAGE / неверный adminUserId'));
    } finally {
      setLoading(false);
    }
  }, [adminUserId, mode, searchId, search]);

  useEffect(() => {
    if (!adminUserId) return;
    (async () => {
      setLoading(true);
      setErrorMsg('');
      try {
        if (mode === 'admins') {
          const res = await userService.getByRole('ROLE_ADMIN', adminUserId, 0, 80);
          setUsers(res.data.users || []);
        } else {
          const res = await userService.getByRole('ROLE_CUSTOMER', adminUserId, 0, 120);
          const list = [...(res.data.users || [])].sort((a, b) => Number(b.isActive) - Number(a.isActive));
          setUsers(list);
        }
      } catch (error) {
        console.error('Error loading users:', error);
        setErrorMsg(formatAccessDenied(error.response?.data?.error || 'Нет доступа: отдел USER_MANAGE / неверный adminUserId'));
      } finally {
        setLoading(false);
      }
    })();
    setDetail(null);
    setShowModal(false);
  }, [adminUserId, mode]);

  const loadDetail = async (userId) => {
    setDetailLoading(true);
    try {
      const res = await userService.getFullInfo(userId, adminUserId);
      setDetail(res.data);
      setShowModal(true);
    } catch (error) {
      console.error('Error loading detail:', error);
    } finally {
      setDetailLoading(false);
    }
  };

  const closeDetail = () => {
    setShowModal(false);
    setDetail(null);
  };

  const toggleActive = async () => {
    if (!detail) return;
    const uid = detail.userId;
    const prev = detail.isActive;
    setToggling(true);
    try {
      if (detail.isActive) {
        await userService.deactivate(uid, adminUserId);
      } else {
        await userService.activate(uid, adminUserId);
      }
      const next = !prev;
      setDetail((d) => (d && d.userId === uid ? { ...d, isActive: next } : d));
      setUsers((list) =>
        list.map((u) => (u.userId === uid ? { ...u, isActive: next } : u))
      );
      toastSuccess('Успешно обновлено!');
    } catch (error) {
      console.error('Error toggling user:', error);
    } finally {
      setToggling(false);
    }
  };

  const toggleUserQuick = async (u) => {
    if (!u?.userId || mode !== 'customers') return;
    const uid = u.userId;
    const prev = u.isActive;
    setCardTogglingId(uid);
    try {
      if (u.isActive) {
        await userService.deactivate(uid, adminUserId);
      } else {
        await userService.activate(uid, adminUserId);
      }
      const next = !prev;
      setUsers((list) =>
        list.map((row) => (row.userId === uid ? { ...row, isActive: next } : row))
      );
      setDetail((d) => (d && d.userId === uid ? { ...d, isActive: next } : d));
      toastSuccess('Успешно обновлено!');
    } catch (e) {
      console.error(e);
    } finally {
      setCardTogglingId(null);
    }
  };

  const renderThumb = (u, isAdmin) => {
    const pic = isAdmin ? u.adminProfile?.profilePictureUrl : u.customerProfile?.profilePictureUrl;
    const letter =
      u.email?.[0] ||
      (isAdmin ? u.adminProfile?.firstName?.[0] : u.customerProfile?.firstName?.[0]) ||
      '?';
    return <CardAvatar picUrl={pic} fallbackLetter={letter} />;
  };

  const renderUserCards = () => {
    if (mode === 'admins') {
      return (
        <div className="user-cards-grid">
          {users.map((u) => (
            <article key={u.userId} className="user-card">
              {renderThumb(u, true)}
              <div className="user-card__body">
                <h3 className="user-card__title">{u.email}</h3>
                <p className="user-card__meta">UUID: {String(u.userId).slice(0, 8)}…</p>
                <span className="user-pill user-pill--muted">Администратор</span>
              </div>
              <div className="user-card__actions">
                <button type="button" className="user-card__btn primary" onClick={() => loadDetail(u.userId)}>
                  Подробнее
                </button>
              </div>
            </article>
          ))}
        </div>
      );
    }
    return (
      <div className="user-cards-grid">
        {users.map((u) => (
          <article key={u.userId} className="user-card">
            {renderThumb(u, false)}
            <div className="user-card__body">
              <h3 className="user-card__title">{u.email}</h3>
              <p className="user-card__meta">{String(u.userId).slice(0, 8)}…</p>
              <span className={u.isActive ? 'user-pill user-pill--ok' : 'user-pill user-pill--blocked'}>
                {u.isActive ? 'Активен' : 'Заблокирован'}
              </span>
            </div>
            <div className="user-card__actions">
              <button type="button" className="user-card__btn ghost" onClick={() => loadDetail(u.userId)}>
                Подробнее
              </button>
              <button
                type="button"
                className={`user-card__btn ${u.isActive ? 'danger' : 'success'}`}
                disabled={cardTogglingId === u.userId}
                onClick={() => toggleUserQuick(u)}
              >
                {cardTogglingId === u.userId ? '…' : u.isActive ? 'Заблокировать' : 'Разблокировать'}
              </button>
            </div>
          </article>
        ))}
      </div>
    );
  };

  return (
    <div className="user-management">
      <h1>Управление пользователями</h1>

      {errorMsg && <div className="error-msg">{errorMsg}</div>}
      {mode === 'customers' && (
        <div className="search-row">
          <input placeholder="Поиск по UUID" value={searchId} onChange={(e) => setSearchId(e.target.value)} />
          <button type="button" onClick={() => { setSearch(''); reloadCurrentList('customers', { idOnly: true, searchId }); }}>
            Искать по ID
          </button>
          <input placeholder="Поиск по email" value={search} onChange={(e) => setSearch(e.target.value)} />
          <button type="button" onClick={() => reloadCurrentList('customers')}>Искать</button>
          <button
            type="button"
            onClick={() => {
              setSearch('');
              setSearchId('');
              reloadCurrentList('customers', { search: '', searchId: '' });
            }}
          >
            Очистить
          </button>
        </div>
      )}

      <div className="user-toggle">
        <button type="button" className={mode === 'admins' ? 'active' : ''} onClick={() => setMode('admins')}>
          Админы
        </button>
        <button type="button" className={mode === 'customers' ? 'active' : ''} onClick={() => setMode('customers')}>
          Клиенты
        </button>
      </div>

      {loading ? <div className="users-loading-msg">Загрузка...</div> : renderUserCards()}

      <div className="user-detail">
        {detailLoading && <div>Гружу детали...</div>}
        {detail && !detailLoading && showModal && (
          <div className="modal-backdrop" role="presentation" onClick={closeDetail}>
            <div className="user-modal-card modal-card" onClick={(e) => e.stopPropagation()} role="dialog">
              <button type="button" className="close-btn" onClick={closeDetail}>×</button>
              <div className="user-modal-layout">
                <div className="user-modal-media">
                  <img
                    className="user-modal-avatar"
                    src={avatarSrc(
                      detail.adminProfile?.profilePictureUrl || detail.customerProfile?.profilePictureUrl
                    )}
                    alt=""
                    onError={(e) => { e.target.onerror = null; e.target.src = USER_PLACEHOLDER; }}
                  />
                </div>
                <div className="user-modal-info">
                  <p className="user-modal-kicker">{translateRole(detail.roleName)}</p>
                  <h3 className="user-modal-title">{detail.email}</h3>
                  <p className="user-modal-muted">UUID: {detail.userId}</p>
                  <div className="user-modal-rows">
                    <div><strong>Регистрация</strong> {formatAdminDateTime(detail.registrationDate)}</div>
                    <div><strong>Последний вход</strong> {formatAdminDateTime(detail.lastLogin)}</div>
                    <div><strong>Активен</strong> {detail.isActive ? 'Да' : 'Нет'}</div>
                  </div>
                  {detail.customerProfile && (
                    <button type="button" className="user-modal-block-btn" onClick={toggleActive} disabled={toggling}>
                      {toggling ? '…' : detail.isActive ? 'Заблокировать' : 'Разблокировать'}
                    </button>
                  )}
                  {detail.adminProfile && (
                    <>
                      <h4 className="user-modal-section">Админ-профиль</h4>
                      <p>{detail.adminProfile.firstName} {detail.adminProfile.lastName}</p>
                      <p>Отдел: {translateDepartment(detail.adminProfile.departmentName)}</p>
                      <p>Принят: {formatAdminDateTime(detail.adminProfile.hireDate)}</p>
                    </>
                  )}
                  {detail.customerProfile && (
                    <>
                      <h4 className="user-modal-section">Клиент-профиль</h4>
                      <p>{detail.customerProfile.firstName} {detail.customerProfile.lastName}</p>
                      <p>Телефон: {detail.customerProfile.phoneNumber ?? '—'}</p>
                      <p>Пол: {translateGender(detail.customerProfile.gender || 'N')}</p>
                      <p>Всего потрачено: {detail.customerProfile.totalSpent}</p>
                      <p>Заказов: {detail.customerProfile.ordersCount}</p>
                    </>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default UserManagement;

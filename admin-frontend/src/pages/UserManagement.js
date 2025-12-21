import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { userService } from '../services/api';
import './UserManagement.css';

const USER_PLACEHOLDER = '/placeholder.png';

function UserManagement() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [mode, setMode] = useState('admins'); // 'admins' | 'customers'
  const [detail, setDetail] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [toggling, setToggling] = useState(false);
  const [search, setSearch] = useState('');
  const [searchId, setSearchId] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const navigate = useNavigate();
  const adminUserId = localStorage.getItem('adminUserId'); // из логина

  useEffect(() => {
    if (!adminUserId) {
      navigate('/login');
      return;
    }
    loadUsers(mode);
  }, [adminUserId, navigate, mode]);

  const loadUsers = async (currentMode) => {
    setLoading(true);
    setErrorMsg('');
    try {
      if (currentMode === 'admins') {
        const res = await userService.getByRole('ROLE_ADMIN', adminUserId, 0, 50);
        setUsers(res.data.users || []);
      } else {
        let list = [];
        if (searchId.trim()) {
          const res = await userService.getById(searchId.trim(), adminUserId);
          list = [res.data];
        } else if (search.trim()) {
          const resSearch = await userService.search(search.trim(), adminUserId, 0, 100);
          list = resSearch.data.users || [];
        } else {
          const res = await userService.getByRole('ROLE_CUSTOMER', adminUserId, 0, 100);
          list = res.data.users || [];
        }
        list.sort((a, b) => Number(b.isActive) - Number(a.isActive));
        setUsers(list);
      }
      setDetail(null);
    } catch (error) {
      console.error('Error loading users:', error);
      setErrorMsg(error.response?.data?.error || 'Нет доступа: требуется отдел USER_MANAGE / неверный adminUserId');
    } finally {
      setLoading(false);
    }
  };

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
    setToggling(true);
    try {
      if (detail.isActive) {
        await userService.deactivate(detail.userId, adminUserId);
      } else {
        await userService.activate(detail.userId, adminUserId);
      }
      await loadUsers(mode);
      await loadDetail(detail.userId);
    } catch (error) {
      console.error('Error toggling user:', error);
    } finally {
      setToggling(false);
    }
  };

  const renderTableHeader = () => {
    if (mode === 'admins') {
      return (
        <tr>
          <th>ID</th>
          <th>Email</th>
          <th>Департамент</th>
          <th>Действия</th>
        </tr>
      );
    }
    return (
      <tr>
        <th>ID</th>
        <th>Email</th>
        <th>Активен</th>
        <th>Действия</th>
      </tr>
    );
  };

  const renderTableRows = () => {
    if (mode === 'admins') {
      return users.map((u) => (
        <tr key={u.userId}>
          <td>{u.userId}</td>
          <td>{u.email}</td>
          <td>{u.profileType === 'ADMIN' ? 'Админ' : '-'}</td>
          <td>
            <button onClick={() => loadDetail(u.userId)}>Подробнее</button>
          </td>
        </tr>
      ));
    }
    return users.map((u) => (
      <tr key={u.userId}>
        <td>{u.userId}</td>
        <td>{u.email}</td>
        <td>{u.isActive ? 'Да' : 'Нет'}</td>
        <td>
          <button onClick={() => loadDetail(u.userId)}>Подробная инфа</button>
        </td>
      </tr>
    ));
  };

  return (
    <div className="user-management">
      <h1>Управление пользователями</h1>
      {errorMsg && <div className="error-msg">{errorMsg}</div>}
      {mode === 'customers' && (
        <div className="search-row">
          <input
            placeholder="Поиск по ID"
            value={searchId}
            onChange={(e) => setSearchId(e.target.value)}
          />
          <button onClick={() => { setSearch(''); loadUsers('customers'); }}>Искать по ID</button>
          <input
            placeholder="Поиск по email/id"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <button onClick={() => loadUsers('customers')}>Искать</button>
          <button onClick={() => { setSearch(''); setSearchId(''); loadUsers('customers'); }}>Очистить</button>
        </div>
      )}

      <div className="user-toggle">
        <button
          className={mode === 'admins' ? 'active' : ''}
          onClick={() => setMode('admins')}
        >
          Админы
        </button>
        <button
          className={mode === 'customers' ? 'active' : ''}
          onClick={() => setMode('customers')}
        >
          Пользователи
        </button>
      </div>

      {loading ? (
        <div>Загрузка...</div>
      ) : (
        <table className="users-table">
          <thead>{renderTableHeader()}</thead>
          <tbody>{renderTableRows()}</tbody>
        </table>
      )}

      <div className="user-detail">
        {detailLoading && <div>Гружу детали...</div>}
        {detail && !detailLoading && showModal && (
          <div className="modal-backdrop" onClick={closeDetail}>
            <div className="detail-card modal-card" onClick={(e) => e.stopPropagation()}>
              <button className="close-btn" onClick={closeDetail}>×</button>
              <div className="avatar-circle">
                <img
                  src={
                    detail.adminProfile?.profilePictureUrl ||
                    detail.customerProfile?.profilePictureUrl ||
                    USER_PLACEHOLDER
                  }
                  alt="avatar"
                  onError={(e) => { e.target.onerror = null; e.target.src = USER_PLACEHOLDER; }}
                />
              </div>
              <h3>Детали пользователя</h3>
              <p><strong>ID:</strong> {detail.userId}</p>
              <p><strong>Email:</strong> {detail.email}</p>
              <p><strong>Роль:</strong> {detail.roleName}</p>
              <p><strong>Активен:</strong> {detail.isActive ? 'Да' : 'Нет'}</p>
              <p><strong>Регистрация:</strong> {detail.registrationDate}</p>
              <p><strong>Последний вход:</strong> {detail.lastLogin}</p>
              <button onClick={toggleActive} disabled={toggling}>
                {toggling ? '...' : detail.isActive ? 'Заблокировать' : 'Активировать'}
              </button>
              {detail.adminProfile && (
                <>
                  <h4>Админ-профиль</h4>
                  <p>{detail.adminProfile.firstName} {detail.adminProfile.lastName}</p>
                  <p>Отдел: {detail.adminProfile.departmentName || '—'}</p>
                  <p>Нанят: {detail.adminProfile.hireDate}</p>
                </>
              )}
              {detail.customerProfile && (
                <>
                  <h4>Клиент-профиль</h4>
                  <p>{detail.customerProfile.firstName} {detail.customerProfile.lastName}</p>
                  <p>Телефон: {detail.customerProfile.phoneNumber}</p>
                  <p>Пол: {detail.customerProfile.gender || 'N'}</p>
                  <p>Всего потрачено: {detail.customerProfile.totalSpent}</p>
                  <p>Заказов: {detail.customerProfile.ordersCount}</p>
                </>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default UserManagement;




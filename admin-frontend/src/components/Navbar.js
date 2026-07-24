import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getAdminPermissions } from '../utils/adminPermissions';
import AdminProfileLauncher from './AdminProfileLauncher';
import './Navbar.css';

function Navbar() {
  const navigate = useNavigate();
  const adminUserId = localStorage.getItem('adminUserId');
  const perms = adminUserId ? getAdminPermissions() : {};

  const handleLogout = () => {
    localStorage.removeItem('adminUserId');
    localStorage.removeItem('adminEmail');
    localStorage.removeItem('adminDepartment');
    localStorage.removeItem('adminPermissions');
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">Админ-панель</Link>
      <div className="navbar-links">
        {adminUserId && perms.hasUserManagement && <Link to="/users">Пользователи</Link>}
        {adminUserId && perms.hasProductManagement && <Link to="/products">Товары</Link>}
        {adminUserId && perms.hasOrderManagement && <Link to="/orders">Заказы</Link>}
        {adminUserId && <Link to="/analytics">Аналитика</Link>}
        {adminUserId && (perms.hasProductManagement || perms.hasOrderManagement) && (
          <Link to="/cities">Города/Маршруты</Link>
        )}
        {!adminUserId && <Link to="/login">Войти</Link>}
        {adminUserId && <AdminProfileLauncher />}
        {adminUserId && (
          <button className="logout-btn" onClick={handleLogout}>
            Выйти
          </button>
        )}
      </div>
    </nav>
  );
}

export default Navbar;




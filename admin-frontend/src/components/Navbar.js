import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Navbar.css';

function Navbar() {
  const navigate = useNavigate();
  const adminUserId = localStorage.getItem('adminUserId');

  const handleLogout = () => {
    localStorage.removeItem('adminUserId');
    localStorage.removeItem('adminEmail');
    localStorage.removeItem('adminDepartment');
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">Админ-панель</Link>
      <div className="navbar-links">
        <Link to="/users">Пользователи</Link>
        <Link to="/products">Товары</Link>
        <Link to="/orders">Заказы</Link>
        <Link to="/analytics">Аналитика</Link>
        <Link to="/cities">Города/Маршруты</Link>
        {!adminUserId && <Link to="/login">Войти</Link>}
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




import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { profileService } from '../services/api';
import './Navbar.css';

function Navbar() {
  const userId = localStorage.getItem('userId');
  const userEmail = localStorage.getItem('userEmail') || '';
  const [avatarUrl, setAvatarUrl] = useState(localStorage.getItem('profileAvatar') || '');

  useEffect(() => {
    const loadProfile = async () => {
      if (!userId) return;
      try {
        const res = await profileService.get(userId);
        if (res.data?.profilePictureUrl) {
          setAvatarUrl(res.data.profilePictureUrl);
          localStorage.setItem('profileAvatar', res.data.profilePictureUrl);
        }
      } catch (e) {
        // ignore
      }
    };
    loadProfile();
    const handler = () => {
      const url = localStorage.getItem('profileAvatar') || '';
      setAvatarUrl(url);
    };
    window.addEventListener('storage', handler);
    window.addEventListener('profileAvatarUpdated', handler);
    return () => {
      window.removeEventListener('storage', handler);
      window.removeEventListener('profileAvatarUpdated', handler);
    };
  }, [userId]);

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">Магазин</Link>
      <div className="navbar-links">
        <Link to="/">Каталог</Link>
        <Link to="/cart">Корзина</Link>
        {userId && (
          <>
            <Link to="/profile" className="profile-chip">
              {avatarUrl ? (
                <img
                  src={avatarUrl}
                  alt="avatar"
                  className="avatar-img"
                  onError={(e) => {
                    e.target.onerror = null;
                    e.target.style.display = 'none';
                    setAvatarUrl('');
                  }}
                />
              ) : (
                <span className="avatar-circle">{userEmail.charAt(0).toUpperCase() || 'U'}</span>
              )}
              <span className="email">{userEmail}</span>
            </Link>
          </>
        )}
        {!userId && (
          <>
            <Link to="/login">Вход</Link>
            <Link to="/register">Регистрация</Link>
          </>
        )}
      </div>
    </nav>
  );
}

export default Navbar;






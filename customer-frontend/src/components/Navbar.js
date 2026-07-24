import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { profileService } from '../services/api';
import './Navbar.css';

function Navbar() {
  const [authState, setAuthState] = useState({
    userId: localStorage.getItem('userId'),
    userEmail: localStorage.getItem('userEmail') || '',
  });
  const [avatarUrl, setAvatarUrl] = useState(localStorage.getItem('profileAvatar') || '');

  useEffect(() => {
    const syncAuth = () => {
      setAuthState({
        userId: localStorage.getItem('userId'),
        userEmail: localStorage.getItem('userEmail') || '',
      });
      setAvatarUrl(localStorage.getItem('profileAvatar') || '');
    };

    const loadProfile = async () => {
      if (!authState.userId) return;
      try {
        const res = await profileService.get(authState.userId);
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
      syncAuth();
    };
    window.addEventListener('storage', handler);
    window.addEventListener('profileAvatarUpdated', handler);
    window.addEventListener('authChanged', handler);
    return () => {
      window.removeEventListener('storage', handler);
      window.removeEventListener('profileAvatarUpdated', handler);
      window.removeEventListener('authChanged', handler);
    };
  }, [authState.userId]);

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">Магазин</Link>
      <div className="navbar-links">
        <Link to="/">Каталог</Link>
        <Link to="/cart">Корзина</Link>
        {authState.userId && (
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
                <span className="avatar-circle">{authState.userEmail.charAt(0).toUpperCase() || 'U'}</span>
              )}
              <span className="email">{authState.userEmail}</span>
            </Link>
          </>
        )}
        {!authState.userId && (
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






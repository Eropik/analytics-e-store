import React, { useState, useCallback, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { userService } from '../services/api';
import { toastSuccess } from '../utils/toastBus';
import { formatAccessDenied, translateDepartment } from '../utils/enumTranslations';
import './AdminProfileLauncher.css';

const PLACEHOLDER = '/placeholder.png';

function AdminProfileLauncher() {
  const adminUserId = localStorage.getItem('adminUserId');
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [email, setEmail] = useState('');
  const [form, setForm] = useState({ firstName: '', lastName: '', profilePictureUrl: '' });
  const [error, setError] = useState('');

  const [departmentName, setDepartmentName] = useState('');

  const loadProfile = useCallback(async (showSpinner = false) => {
    if (!adminUserId) return;
    if (showSpinner) setLoading(true);
    setError('');
    try {
      const res = await userService.getFullInfo(adminUserId, adminUserId);
      const ap = res.data?.adminProfile;
      setEmail(res.data?.email || '');
      setDepartmentName(ap?.departmentName || '');
      setForm({
        firstName: ap?.firstName ?? '',
        lastName: ap?.lastName ?? '',
        profilePictureUrl: ap?.profilePictureUrl ?? '',
      });
    } catch (e) {
      console.warn(e);
      setError(formatAccessDenied(e.response?.data?.error || 'Не удалось загрузить профиль'));
    } finally {
      if (showSpinner) setLoading(false);
    }
  }, [adminUserId]);

  useEffect(() => {
    loadProfile(false);
  }, [loadProfile]);

  useEffect(() => {
    if (!open) return;
    loadProfile(true);
  }, [open, loadProfile]);

  const close = () => {
    setOpen(false);
    setError('');
  };

  const handleFile = async (e) => {
    const file = e.target.files?.[0];
    if (!file || !adminUserId) return;
    setUploading(true);
    setError('');
    try {
      const res = await userService.uploadMyAdminProfilePhoto(adminUserId, adminUserId, file);
      const url = res.data.imageUrl;
      if (url) {
        setForm((f) => ({ ...f, profilePictureUrl: url }));
      }
      toastSuccess('Фото загружено');
    } catch (err) {
      console.error(err);
      setError(formatAccessDenied(err.response?.data?.error || 'Не удалось загрузить фото'));
    } finally {
      setUploading(false);
      e.target.value = '';
    }
  };

  const handleSubmit = async (ev) => {
    ev.preventDefault();
    if (!adminUserId) return;
    setSaving(true);
    setError('');
    try {
      const res = await userService.patchMyAdminProfile(adminUserId, adminUserId, form);
      const ap = res.data.adminProfile;
      if (ap) {
        setForm({
          firstName: ap.firstName ?? '',
          lastName: ap.lastName ?? '',
          profilePictureUrl: ap.profilePictureUrl ?? '',
        });
      }
      toastSuccess('Профиль сохранён');
      close();
    } catch (err) {
      console.error(err);
      setError(formatAccessDenied(err.response?.data?.error || 'Не удалось сохранить'));
    } finally {
      setSaving(false);
    }
  };

  if (!adminUserId) return null;

  const avatarSrc = form.profilePictureUrl || PLACEHOLDER;

  return (
    <>
      <button type="button" className="admin-nav-profile" onClick={() => setOpen(true)} title="Личный кабинет">
        <span className="admin-nav-profile__thumb-wrap">
          <img
            className="admin-nav-profile__thumb"
            src={avatarSrc}
            alt=""
            onError={(e) => {
              e.target.onerror = null;
              e.target.src = PLACEHOLDER;
            }}
          />
        </span>
        <span className="admin-nav-profile__label">Личный кабинет</span>
      </button>

      {open &&
        createPortal(
          <div className="admin-profile-modal-backdrop" role="presentation" onClick={close}>
            <div className="admin-profile-modal" role="dialog" onClick={(e) => e.stopPropagation()}>
              <button type="button" className="admin-profile-modal__close" onClick={close} aria-label="Закрыть">
                ×
              </button>
              <h2 className="admin-profile-modal__title">Личный кабинет</h2>
              {loading ? (
                <p className="admin-profile-modal__hint">Загрузка…</p>
              ) : (
                <form className="admin-profile-modal__form" onSubmit={handleSubmit}>
                  {error && <div className="admin-profile-modal__error">{error}</div>}
                  <div className="admin-profile-modal__preview-row">
                    <img
                      className="admin-profile-modal__avatar"
                      src={avatarSrc}
                      alt=""
                      onError={(e) => {
                        e.target.onerror = null;
                        e.target.src = PLACEHOLDER;
                      }}
                    />
                    <label className={`admin-profile-modal__file-btn ${uploading ? 'is-busy' : ''}`}>
                      {uploading ? 'Загрузка…' : 'Выбрать фото'}
                      <input type="file" accept="image/*" hidden onChange={handleFile} disabled={uploading} />
                    </label>
                  </div>
                  <p className="admin-profile-modal__email">{email}</p>
                  {departmentName ? (
                    <p className="admin-profile-modal__dept-muted">{translateDepartment(departmentName)}</p>
                  ) : null}
                  <label>
                    Имя
                    <input
                      value={form.firstName}
                      onChange={(e) => setForm((f) => ({ ...f, firstName: e.target.value }))}
                    />
                  </label>
                  <label>
                    Фамилия
                    <input value={form.lastName} onChange={(e) => setForm((f) => ({ ...f, lastName: e.target.value }))} />
                  </label>
                  <button type="submit" className="admin-profile-modal__submit" disabled={saving}>
                    {saving ? 'Сохранение…' : 'Сохранить'}
                  </button>
                </form>
              )}
            </div>
          </div>,
          document.body
        )}
    </>
  );
}

export default AdminProfileLauncher;

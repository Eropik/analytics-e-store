import React from 'react';
import { Navigate } from 'react-router-dom';
import { getAdminPermissions, PERMISSION_TO_DEPARTMENTS } from '../utils/adminPermissions';
import { translateDepartment } from '../utils/enumTranslations';
import './AccessDenied.css';

/**
 * Доступ, если хотя бы один ключ в permissions истинный.
 */
function ProtectedAdminRoute({ children, permissionKeys }) {
  const adminUserId = localStorage.getItem('adminUserId');

  if (!adminUserId) {
    return <Navigate to="/login" replace />;
  }

  const keys = permissionKeys?.length ? permissionKeys : [];
  if (keys.length === 0) {
    return children;
  }

  const perms = getAdminPermissions();
  const ok = keys.some((k) => perms[k]);

  if (ok) {
    return children;
  }

  const departments = [...new Set(keys.flatMap((k) => PERMISSION_TO_DEPARTMENTS[k] || []))];

  const currentDep = localStorage.getItem('adminDepartment') || 'не указан';
  const currentDepReadable = translateDepartment(currentDep);

  const roleLine =
    departments.length > 0
      ? departments
          .map((d) => `${d} (${translateDepartment(d)})`)
          .join(' или ')
      : 'нет данных о требованиях';

  return (
    <div className="access-denied-panel">
      <h1 className="access-denied-panel__title">Доступ ограничен</h1>
      <p className="access-denied-panel__text">
        Этот раздел доступен только сотрудникам с подходящим{' '}
        <strong>отделом</strong> в профиле администратора (на бэкенде это связано с ролью{' '}
        <code className="access-denied-panel__mono">ROLE_ADMIN</code>
        ).
      </p>
      <div className="access-denied-panel__hint">
        <div>
          <span className="access-denied-panel__muted">Подойдёт отдел:</span>{' '}
          <strong>{roleLine}</strong>
        </div>
        <div>
          <span className="access-denied-panel__muted">Ваш отдел сейчас:</span>{' '}
          <strong>
            {currentDepReadable}{' '}
            {currentDep !== 'не указан' && <>(<code>{currentDep}</code>)</>}
          </strong>
        </div>
      </div>
      <p className="access-denied-panel__sub">
        Для этого раздела используются права входа без отдельного JWT: подставляется ваш{' '}
        <code className="access-denied-panel__mono">adminUserId</code>. Если нужен доступ — измените{' '}
        отдел в профиле администратора в базе данных или выполните повторный вход после правок профиля.
      </p>
    </div>
  );
}

export default ProtectedAdminRoute;

import React, { useMemo, useState, useRef, useEffect } from 'react';
import './AutocompleteSelect.css';

/** Одна строка в списке и в поле: «id — название» */
export function formatAutocompleteOptionLine(item) {
  if (item == null || item.id === undefined || item.id === null) return '';
  const idStr = String(item.id);
  const name = String(item.label ?? '').trim();
  return name ? `${idStr} — ${name}` : idStr;
}

function AutocompleteSelect({
  items,
  value,
  onChange,
  placeholder = 'Выберите…',
  emptyText = 'Нет вариантов',
  disabled = false,
  id,
  label,
}) {
  const [open, setOpen] = useState(false);
  const [inputValue, setInputValue] = useState('');
  const rootRef = useRef(null);

  const selectedLine = useMemo(() => {
    const it = items.find((i) => String(i.id) === String(value));
    return it ? formatAutocompleteOptionLine(it) : '';
  }, [items, value]);

  useEffect(() => {
    if (!open) setInputValue(selectedLine || '');
  }, [selectedLine, open]);

  useEffect(() => {
    const onDoc = (e) => {
      if (rootRef.current && !rootRef.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener('mousedown', onDoc);
    return () => document.removeEventListener('mousedown', onDoc);
  }, []);

  const q = inputValue.trim().toLowerCase();
  const filtered = useMemo(() => {
    if (!q) return items;
    return items.filter((i) => {
      const line = formatAutocompleteOptionLine(i).toLowerCase();
      const idStr = String(i.id).toLowerCase();
      const nameStr = String(i.label ?? '')
        .toLowerCase()
        .trim();
      return line.includes(q) || idStr.includes(q) || nameStr.includes(q);
    });
  }, [items, q]);

  const pick = (idVal) => {
    onChange(idVal !== undefined && idVal !== null ? String(idVal) : '');
    setOpen(false);
  };

  return (
    <div className={`autocomplete-select ${disabled ? 'is-disabled' : ''}`} ref={rootRef}>
      {label && (
        <label className="autocomplete-select__label" htmlFor={id}>
          {label}
        </label>
      )}
      <div className="autocomplete-select__control">
        <input
          id={id}
          type="text"
          autoComplete="off"
          disabled={disabled}
          placeholder={placeholder}
          value={open ? inputValue : selectedLine || inputValue}
          onChange={(e) => {
            setInputValue(e.target.value);
            if (!open) setOpen(true);
            if (value) onChange('');
          }}
          onFocus={() => {
            setOpen(true);
            setInputValue(selectedLine || '');
          }}
        />
        {value !== '' && value !== undefined && !disabled && (
          <button
            type="button"
            className="autocomplete-select__clear"
            aria-label="Сбросить"
            onMouseDown={(e) => e.preventDefault()}
            onClick={() => {
              onChange('');
              setInputValue('');
              setOpen(true);
            }}
          >
            ×
          </button>
        )}
      </div>
      {open && !disabled && (
        <div className="autocomplete-select__dropdown" role="listbox">
          {filtered.length === 0 ? (
            <div className="autocomplete-select__empty">{emptyText}</div>
          ) : (
            filtered.map((i) => (
              <button
                key={`${i.id}`}
                type="button"
                role="option"
                aria-selected={String(value) === String(i.id)}
                className={String(value) === String(i.id) ? 'is-selected' : ''}
                onMouseDown={(e) => e.preventDefault()}
                onClick={() => pick(i.id)}
              >
                <span className="autocomplete-select__line">{formatAutocompleteOptionLine(i)}</span>
              </button>
            ))
          )}
        </div>
      )}
    </div>
  );
}

export default AutocompleteSelect;

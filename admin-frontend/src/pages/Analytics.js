import React, { useMemo, useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { analyticsService } from '../services/api';
import './Analytics.css';
import {
  PieChart, Pie, Cell, Tooltip, Legend,
  BarChart, Bar, XAxis, YAxis, CartesianGrid,
  LineChart, Line, ResponsiveContainer, ReferenceLine
} from 'recharts';
import { formatAccessDenied, translateGender, translateOrderStatus } from '../utils/enumTranslations';
import * as XLSX from 'xlsx';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';

const Section = ({ title, children }) => (
  <details className="analytics-section" open>
    <summary>{title}</summary>
    {children}
  </details>
);

/** Должен совпадать с CASE в AnalyzeRepository для фильтров и analyzeGeneric */
const AGE_BUCKET_OPTIONS = [
  { value: '', label: 'Любой возрастной интервал' },
  { value: 'Unknown', label: 'Неизвестно' },
  { value: '0-17', label: '0–17 лет' },
  { value: '18-24', label: '18–24 года' },
  { value: '25-29', label: '25–29 лет' },
  { value: '30-34', label: '30–34 лет' },
  { value: '35-39', label: '35–39 лет' },
  { value: '40-44', label: '40–44 лет' },
  { value: '45-49', label: '45–49 лет' },
  { value: '50-54', label: '50–54 лет' },
  { value: '55-59', label: '55–59 лет' },
  { value: '60+', label: '60+ лет' },
];

function Analytics() {
  const palette = ['#3498db', '#e67e22', '#9b59b6', '#16a085', '#e74c3c', '#f1c40f', '#2ecc71', '#34495e'];
  const PIE_LIMIT = 5;
  const [loading, setLoading] = useState(true);
  const [productData, setProductData] = useState({});
  const [userData, setUserData] = useState({});
  const [orderData, setOrderData] = useState({});
  const [analyzeData, setAnalyzeData] = useState([]);
  const [productError, setProductError] = useState('');
  const [userError, setUserError] = useState('');
  const [orderError, setOrderError] = useState('');
  const [analyzeError, setAnalyzeError] = useState('');
  const [expandedCharts, setExpandedCharts] = useState({});
  const [turnoverForm, setTurnoverForm] = useState({
    metric: 'revenue',
    startMonth: '',
    endMonth: '',
    plannedValues: [],
  });
  const [turnoverReport, setTurnoverReport] = useState(null);
  const [turnoverLoading, setTurnoverLoading] = useState(false);
  const [turnoverError, setTurnoverError] = useState('');
  const [groupForm, setGroupForm] = useState({
    metric: 'revenue',
    groupBy: 'category',
    startMonth: '',
    endMonth: '',
  });
  const [groupReport, setGroupReport] = useState(null);
  const [groupLoading, setGroupLoading] = useState(false);
  const [groupError, setGroupError] = useState('');
  const [analyzeFilters, setAnalyzeFilters] = useState({
    scope: 'products',
    gender: '',
    ageGroup: '',
    month: ''
  });
  const [orderFilter, setOrderFilter] = useState({
    status: '',
    gender: '',
    ageGroup: ''
  });
  const [groupHoverLineKey, setGroupHoverLineKey] = useState(null);
  const [turnoverTrendHoverSeries, setTurnoverTrendHoverSeries] = useState(null);
  const navigate = useNavigate();
  const adminUserId = localStorage.getItem('adminUserId');
  const department = localStorage.getItem('adminDepartment') || '';
  const canProduct = department === 'PRODUCT_MANAGE' || department === 'ANALYZE';
  const canOrder = department === 'ORDER_MANAGE' || department === 'ANALYZE';
  const canUser = department === 'USER_MANAGE' || department === 'ANALYZE';
  const canAnalyze = department === 'ANALYZE';
  const availableTabs = [
    ...(canProduct ? ['product'] : []),
    ...(canUser ? ['user'] : []),
    ...(canOrder ? ['order'] : []),
    ...(canAnalyze ? ['analyze'] : []),
  ];
  const [activeTab, setActiveTab] = useState(availableTabs[0] || '');

  const plannedMonths = useMemo(() => {
    if (!turnoverForm.startMonth || !turnoverForm.endMonth) return [];
    const start = new Date(`${turnoverForm.startMonth}-01T00:00:00`);
    const end = new Date(`${turnoverForm.endMonth}-01T00:00:00`);
    if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime()) || end < start) return [];
    const arr = [];
    const cursor = new Date(start);
    while (cursor <= end) {
      arr.push(`${cursor.getFullYear()}-${String(cursor.getMonth() + 1).padStart(2, '0')}`);
      cursor.setMonth(cursor.getMonth() + 1);
    }
    return arr;
  }, [turnoverForm.startMonth, turnoverForm.endMonth]);

  useEffect(() => {
    setTurnoverForm((prev) => {
      if (plannedMonths.length === 0) return { ...prev, plannedValues: [] };
      const next = plannedMonths.map((_, idx) => Number(prev.plannedValues[idx] || 0));
      return { ...prev, plannedValues: next };
    });
  }, [plannedMonths]);

  useEffect(() => {
    setTurnoverReport(null);
    setTurnoverError('');
  }, [turnoverForm.metric, turnoverForm.startMonth, turnoverForm.endMonth, turnoverForm.plannedValues]);

  const loadAnalyze = useCallback(async (filters) => {
    try {
      let monthNum;
      const m = filters.month;
      if (m !== '' && m != null && String(m).trim() !== '') {
        const n = Number(m);
        if (Number.isInteger(n) && n >= 1 && n <= 12) monthNum = n;
      }
      const res = await analyticsService.analyzeGeneric(adminUserId, {
        scope: filters.scope,
        gender: filters.gender || undefined,
        ageGroup: filters.ageGroup || undefined,
        month: monthNum
      });
      setAnalyzeData(res.data.result || []);
      setAnalyzeError('');
    } catch (e) {
      console.error('Error analyze', e);
      setAnalyzeData([]);
      setAnalyzeError(formatAccessDenied(e.response?.data?.error || 'Нет доступа: требуется ANALYZE'));
    }
  }, [adminUserId]);

  const loadOrderFilter = useCallback(async (filters) => {
    try {
      const res = await analyticsService.orderFilter(adminUserId, {
        status: filters.status || undefined,
        gender: filters.gender || undefined,
        ageGroup: filters.ageGroup || undefined,
      });
      setOrderData((prev) => ({ ...prev, filtered: res.data }));
    } catch (e) {
      console.error('Error order filter', e);
      setOrderData((prev) => ({ ...prev, filtered: { brands: [], categories: [], products: [] } }));
      setOrderError(formatAccessDenied(e.response?.data?.error || 'Нет доступа: требуется ORDER_MANAGE или ANALYZE'));
    }
  }, [adminUserId]);

  const loadOverviewAnalytics = useCallback(async () => {
    setLoading(true);
    setProductError('');
    setUserError('');
    setOrderError('');
    setAnalyzeError('');

    const tasks = [];

    if (canProduct) {
      tasks.push(
        analyticsService.productOverview(adminUserId)
          .then(res => setProductData(res.data || {}))
          .catch(err => {
            console.error('Product analytics error', err);
            setProductData({});
            setProductError(formatAccessDenied(err.response?.data?.error || 'Нет доступа: требуется PRODUCT_MANAGE или ANALYZE'));
          })
      );
    }

    if (canUser) {
      tasks.push(
        analyticsService.userOverview(adminUserId)
          .then(res => setUserData(res.data || {}))
          .catch(err => {
            console.error('User analytics error', err);
            setUserData({});
            setUserError(formatAccessDenied(err.response?.data?.error || 'Нет доступа: требуется USER_MANAGE или ANALYZE'));
          })
      );
    }

    if (canOrder) {
      tasks.push(
        analyticsService.orderOverview(adminUserId)
          .then(res =>
            setOrderData((prev) => ({
              ...(res.data || {}),
              ...(prev.filtered != null ? { filtered: prev.filtered } : {})
            }))
          )
          .catch(err => {
            console.error('Order analytics error', err);
            setOrderData({});
            setOrderError(formatAccessDenied(err.response?.data?.error || 'Нет доступа: требуется ORDER_MANAGE или ANALYZE'));
          })
      );
    }

    Promise.all(tasks).finally(() => setLoading(false));
  }, [
    adminUserId,
    canProduct,
    canUser,
    canOrder,
  ]);

  useEffect(() => {
    if (!adminUserId || !canOrder) return undefined;
    const snapshot = {
      status: orderFilter.status,
      gender: orderFilter.gender,
      ageGroup: orderFilter.ageGroup,
    };
    const t = window.setTimeout(() => loadOrderFilter(snapshot), 400);
    return () => window.clearTimeout(t);
  }, [adminUserId, canOrder, orderFilter.status, orderFilter.gender, orderFilter.ageGroup, loadOrderFilter]);

  useEffect(() => {
    if (!adminUserId || !canAnalyze) return undefined;
    const snapshot = {
      scope: analyzeFilters.scope,
      gender: analyzeFilters.gender,
      ageGroup: analyzeFilters.ageGroup,
      month: analyzeFilters.month,
    };
    const t = window.setTimeout(() => loadAnalyze(snapshot), 400);
    return () => window.clearTimeout(t);
  }, [
    adminUserId,
    canAnalyze,
    analyzeFilters.scope,
    analyzeFilters.gender,
    analyzeFilters.ageGroup,
    analyzeFilters.month,
    loadAnalyze,
  ]);

  useEffect(() => {
    if (!adminUserId) {
      navigate('/login');
      return;
    }
    loadOverviewAnalytics();
  }, [adminUserId, navigate, loadOverviewAnalytics]);

  const normalizePieData = (data, isFull) => {
    const list = Array.isArray(data) ? data : [];
    if (isFull || list.length <= PIE_LIMIT) return list;
    const top = list.slice(0, PIE_LIMIT);
    const rest = list.slice(PIE_LIMIT);
    const restValue = rest.reduce((sum, item) => sum + Number(item?.value || 0), 0);
    return restValue > 0 ? [...top, { label: 'Остальное', value: restValue, isOther: true }] : top;
  };

  const renderPie = (data, chartKey) => (
    <div className="chart-box">
      <div className="chart-head">
        <button
          type="button"
          className="toggle-chart-btn"
          onClick={() => setExpandedCharts((prev) => ({ ...prev, [chartKey]: !prev[chartKey] }))}
        >
          {expandedCharts[chartKey] ? 'Свернуть до топ-5' : 'Полный формат'}
        </button>
      </div>
      <ResponsiveContainer width="100%" height={260}>
        <PieChart>
          <Pie
            data={normalizePieData(data, Boolean(expandedCharts[chartKey]))}
            dataKey="value"
            nameKey="label"
            cx="50%"
            cy="50%"
            outerRadius={90}
            label
          >
            {normalizePieData(data, Boolean(expandedCharts[chartKey])).map((item, idx) => (
              <Cell key={idx} fill={item.isOther ? '#b9bec8' : palette[idx % palette.length]} />
            ))}
          </Pie>
          <Tooltip />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );

  const renderBuckets = (data) => (
    <div className="chart-box">
      <ResponsiveContainer width="100%" height={260}>
        <BarChart data={data || []}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="label" />
          <YAxis />
          <Tooltip />
          <Bar dataKey="value" fill={palette[0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );

  const renderTimeSeries = (data, hoverKey = 'time-series-single') => (
    <div className="chart-box">
      <ResponsiveContainer width="100%" height={260}>
        <LineChart
          data={data || []}
          onMouseLeave={() => setTurnoverTrendHoverSeries((k) => (k === hoverKey ? null : k))}
        >
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="label" />
          <YAxis />
          <Tooltip />
          <Line
            type="monotone"
            dataKey="value"
            stroke={palette[1]}
            strokeWidth={turnoverTrendHoverSeries === hoverKey ? 3.2 : 2}
            dot={false}
            activeDot={{ r: 6 }}
            onMouseEnter={() => setTurnoverTrendHoverSeries(hoverKey)}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );

  const renderTurnoverTrend = ({ chartId, data, dataKey, valueName, absValue, absLabel, yAxisLabel }) => (
    <div className="chart-box">
      <ResponsiveContainer width="100%" height={280}>
        <LineChart
          data={data || []}
          onMouseLeave={() => setTurnoverTrendHoverSeries((k) => (k === chartId ? null : k))}
        >
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="label" />
          <YAxis label={{ value: yAxisLabel, angle: -90, position: 'insideLeft' }} />
          <Tooltip />
          <Legend />
          <Line
            type="monotone"
            dataKey={dataKey}
            name={valueName}
            stroke={palette[1]}
            strokeWidth={turnoverTrendHoverSeries === chartId ? 3.5 : 2}
            dot={false}
            activeDot={{ r: 6 }}
            onMouseEnter={() => setTurnoverTrendHoverSeries(chartId)}
          />
          <ReferenceLine
            y={absValue}
            name={absLabel}
            stroke={palette[4]}
            strokeDasharray="6 4"
            ifOverflow="extendDomain"
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );

  const exportRowsToExcel = (rows, filename) => {
    const ws = XLSX.utils.json_to_sheet(rows);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'report');
    XLSX.writeFile(wb, `${filename}.xlsx`);
  };

  const exportRowsToPdf = (rows, headers, filename) => {
    const doc = new jsPDF();
    autoTable(doc, {
      head: [headers],
      body: rows.map((r) => headers.map((h) => r[h] ?? '')),
      styles: { fontSize: 9 },
    });
    doc.save(`${filename}.pdf`);
  };

  const handleGenerateTurnover = async () => {
    setTurnoverError('');
    setTurnoverReport(null);
    if (plannedMonths.length === 0) {
      setTurnoverError('Выберите корректный период месяцев.');
      return;
    }
    if (turnoverForm.plannedValues.some((v) => Number(v) <= 0)) {
      setTurnoverError('Заполните плановые значения для каждого месяца (больше 0).');
      return;
    }
    setTurnoverLoading(true);
    try {
      const res = await analyticsService.turnoverPlanReport(adminUserId, {
        metric: turnoverForm.metric,
        startMonth: turnoverForm.startMonth,
        endMonth: turnoverForm.endMonth,
        plannedValues: turnoverForm.plannedValues.map(Number),
      });
      setTurnoverReport(res.data);
    } catch (e) {
      setTurnoverError(formatAccessDenied(e.response?.data?.error || 'Не удалось построить отчет'));
    } finally {
      setTurnoverLoading(false);
    }
  };

  const handleGenerateGroup = async () => {
    setGroupError('');
    setGroupReport(null);
    if (!groupForm.startMonth || !groupForm.endMonth) {
      setGroupError('Выберите период месяцев.');
      return;
    }
    setGroupLoading(true);
    try {
      const res = await analyticsService.groupShareReport(adminUserId, groupForm);
      setGroupReport(res.data);
    } catch (e) {
      setGroupError(formatAccessDenied(e.response?.data?.error || 'Не удалось построить групповой анализ'));
    } finally {
      setGroupLoading(false);
    }
  };

  const groupDynamicKeys = useMemo(() => {
    const first = groupReport?.dynamics?.[0];
    if (!first) return [];
    return Object.keys(first).filter((k) => k !== 'month');
  }, [groupReport]);

  const toSafeNumber = (value) => {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : 0;
  };

  const computeSigma = (values) => {
    if (!values.length) return 0;
    const mean = values.reduce((sum, v) => sum + v, 0) / values.length;
    const variance = values.reduce((sum, v) => {
      const diff = v - mean;
      return sum + diff * diff;
    }, 0) / values.length;
    return Math.sqrt(variance);
  };

  const roundForView = (value) => Number(value || 0).toFixed(2);

  const turnoverCalculated = useMemo(() => {
    const sourceRows = turnoverReport?.rows || [];
    if (!sourceRows.length) {
      return {
        rows: [],
        sigmaAbs: 0,
        variationAbs: 0,
      };
    }

    const actualValues = sourceRows.map((r) => toSafeNumber(r.actualTurnover));
    const sigmaAbs = computeSigma(actualValues);
    const avgActual = actualValues.reduce((sum, v) => sum + v, 0) / actualValues.length;
    const variationAbs = avgActual === 0 ? 0 : (sigmaAbs / avgActual) * 100;

    const rows = sourceRows.map((row, idx) => {
      const prefix = actualValues.slice(0, idx + 1);
      const cumulativeSigma = computeSigma(prefix);
      const cumulativeAvg = prefix.reduce((sum, v) => sum + v, 0) / prefix.length;
      const cumulativeVariation = cumulativeAvg === 0 ? 0 : (cumulativeSigma / cumulativeAvg) * 100;
      return {
        ...row,
        cumulativeSigma,
        cumulativeVariationPercent: cumulativeVariation,
      };
    });

    return {
      rows,
      sigmaAbs,
      variationAbs,
    };
  }, [turnoverReport]);

  const turnoverRowsWithTotal = useMemo(() => {
    if (!turnoverCalculated.rows.length) return [];
    const monthRows = turnoverCalculated.rows.map((r) => ({
      month: r.month,
      actualTurnover: roundForView(r.actualTurnover),
      plannedTurnover: roundForView(r.plannedTurnover),
      planExecutionPercent: roundForView(r.planExecutionPercent),
      cumulativeSigma: roundForView(r.cumulativeSigma),
      cumulativeVariationPercent: roundForView(r.cumulativeVariationPercent),
      isTotal: false,
    }));

    monthRows.push({
      month: 'ИТОГО за период',
      actualTurnover: roundForView(turnoverReport.totalActual),
      plannedTurnover: roundForView(turnoverReport.totalPlanned),
      planExecutionPercent: '',
      cumulativeSigma: roundForView(turnoverCalculated.sigmaAbs),
      cumulativeVariationPercent: roundForView(turnoverCalculated.variationAbs),
      isTotal: true,
    });
    return monthRows;
  }, [turnoverReport, turnoverCalculated]);

  const turnoverExportRows = useMemo(
    () => turnoverRowsWithTotal.map((r) => ({
      month: r.month,
      actualTurnover: r.actualTurnover,
      plannedTurnover: r.plannedTurnover,
      planExecutionPercent: r.planExecutionPercent,
      cumulativeSigma: r.cumulativeSigma,
      cumulativeVariationPercent: r.cumulativeVariationPercent,
    })),
    [turnoverRowsWithTotal]
  );

  const turnoverSigmaTrendData = useMemo(
    () => turnoverCalculated.rows.map((row) => ({
      label: row.month,
      value: toSafeNumber(row.cumulativeSigma),
    })),
    [turnoverCalculated]
  );

  const turnoverVariationTrendData = useMemo(
    () => turnoverCalculated.rows.map((row) => ({
      label: row.month,
      value: toSafeNumber(row.cumulativeVariationPercent),
    })),
    [turnoverCalculated]
  );

  const hasMissingTurnoverMonths = useMemo(
    () => Boolean(turnoverReport?.rows?.some((r) => Number(r.actualTurnover || 0) === 0)),
    [turnoverReport]
  );

  if (loading) return <div>Загрузка...</div>;

  return (
    <div className="analytics">
      <h1>Аналитика</h1>

      {canAnalyze && (
        <Section title="Анализ товарооборота (ВВПотн, σ, v)">
          <form className="analytics-no-nav-form" onSubmit={(e) => e.preventDefault()}>
          <div className="filters">
            <select
              value={turnoverForm.metric}
              onChange={(e) => setTurnoverForm((p) => ({ ...p, metric: e.target.value }))}
            >
              <option value="revenue">По выручке</option>
              <option value="volume">По объему продаж</option>
            </select>
            <input
              type="month"
              value={turnoverForm.startMonth}
              onChange={(e) => setTurnoverForm((p) => ({ ...p, startMonth: e.target.value }))}
            />
            <input
              type="month"
              value={turnoverForm.endMonth}
              onChange={(e) => setTurnoverForm((p) => ({ ...p, endMonth: e.target.value }))}
            />
            <button type="button" onClick={handleGenerateTurnover} disabled={turnoverLoading}>
              {turnoverLoading ? 'Формируем...' : 'Сгенерировать отчет'}
            </button>
          </div>

          {plannedMonths.length > 0 && (
            <div className="plan-inputs">
              {plannedMonths.map((month, idx) => (
                <label key={month} className="plan-item">
                  {month}
                  <input
                    type="number"
                    min="0"
                    value={turnoverForm.plannedValues[idx] ?? ''}
                    onChange={(e) => {
                      const value = e.target.value;
                      setTurnoverForm((p) => {
                        const next = [...p.plannedValues];
                        next[idx] = value;
                        return { ...p, plannedValues: next };
                      });
                    }}
                  />
                </label>
              ))}
            </div>
          )}
          </form>
          {turnoverError && <div className="error-msg">{turnoverError}</div>}

          {turnoverReport?.rows?.length > 0 && (
            <>
              <table className="analytics-table">
                <thead>
                  <tr>
                    <th>Месяц</th>
                    <th>Фактический товарооборот</th>
                    <th>Плановый товарооборот</th>
                    <th>Выполнение плана, %</th>
                    <th>Нарастающее σ</th>
                    <th>Нарастающий v, %</th>
                  </tr>
                </thead>
                <tbody>
                  {turnoverRowsWithTotal.map((r) => (
                    <tr key={r.month} className={r.isTotal ? 'analytics-total-row' : ''}>
                      <td>{r.month}</td>
                      <td>{r.actualTurnover}</td>
                      <td>{r.plannedTurnover}</td>
                      <td>{r.planExecutionPercent}</td>
                      <td>{r.cumulativeSigma}</td>
                      <td>{r.cumulativeVariationPercent}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {hasMissingTurnoverMonths && (
                <div className="hint-msg">
                  Для части месяцев фактические данные отсутствуют в БД, в расчётах использовано значение 0.
                </div>
              )}
              <div className="filters">
                <button
                  type="button"
                  onClick={() => exportRowsToExcel(turnoverExportRows, 'turnover-plan-report')}
                >
                  Экспорт Excel
                </button>
                <button
                  type="button"
                  onClick={() => exportRowsToPdf(
                    turnoverExportRows,
                    ['month', 'actualTurnover', 'plannedTurnover', 'planExecutionPercent', 'cumulativeSigma', 'cumulativeVariationPercent'],
                    'turnover-plan-report'
                  )}
                >
                  Экспорт PDF
                </button>
              </div>
              <h4>График ВВПотн</h4>
              {renderTimeSeries(turnoverReport.executionSeries, 'turnover-exec')}
              <h4>Тенденция нарастающего σ</h4>
              {renderTurnoverTrend({
                chartId: 'turnover-sigma',
                data: turnoverSigmaTrendData,
                dataKey: 'value',
                valueName: 'Нарастающее σ',
                absValue: toSafeNumber(turnoverCalculated.sigmaAbs),
                absLabel: 'Итоговое σ за период',
                yAxisLabel: turnoverReport.metric === 'volume' ? 'Ед.' : 'Рубли',
              })}
              <h4>Тенденция нарастающего v, %</h4>
              {renderTurnoverTrend({
                chartId: 'turnover-variation',
                data: turnoverVariationTrendData,
                dataKey: 'value',
                valueName: 'Нарастающий v, %',
                absValue: toSafeNumber(turnoverCalculated.variationAbs),
                absLabel: 'Итоговое v за период',
                yAxisLabel: '%',
              })}
            </>
          )}
        </Section>
      )}

      {canAnalyze && (
        <Section title="Вес групп товаров в товарообороте (ВГР)">
          <form className="analytics-no-nav-form" onSubmit={(e) => e.preventDefault()}>
          <div className="filters">
            <select
              value={groupForm.metric}
              onChange={(e) => setGroupForm((p) => ({ ...p, metric: e.target.value }))}
            >
              <option value="revenue">По выручке</option>
              <option value="volume">По объему продаж</option>
            </select>
            <select
              value={groupForm.groupBy}
              onChange={(e) => setGroupForm((p) => ({ ...p, groupBy: e.target.value }))}
            >
              <option value="category">Группы по категориям</option>
              <option value="brand">Группы по брендам</option>
            </select>
            <input
              type="month"
              value={groupForm.startMonth}
              onChange={(e) => setGroupForm((p) => ({ ...p, startMonth: e.target.value }))}
            />
            <input
              type="month"
              value={groupForm.endMonth}
              onChange={(e) => setGroupForm((p) => ({ ...p, endMonth: e.target.value }))}
            />
            <button type="button" onClick={handleGenerateGroup} disabled={groupLoading}>
              {groupLoading ? 'Формируем...' : 'Сгенерировать отчет'}
            </button>
          </div>
          </form>
          {groupError && <div className="error-msg">{groupError}</div>}

          {groupReport?.rows?.length > 0 && (
            <>
              <table className="analytics-table">
                <thead>
                  <tr>
                    <th>Группа</th>
                    <th>Тгр</th>
                    <th>Тф</th>
                    <th>Вгр, %</th>
                  </tr>
                </thead>
                <tbody>
                  {groupReport.rows.map((r) => (
                    <tr key={r.groupName}>
                      <td>{r.groupName}</td>
                      <td>{r.groupTurnover}</td>
                      <td>{r.totalTurnover}</td>
                      <td>{r.sharePercent}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <div className="filters">
                <button type="button" onClick={() => exportRowsToExcel(groupReport.rows, 'group-share-report')}>
                  Экспорт Excel
                </button>
                <button
                  type="button"
                  onClick={() => exportRowsToPdf(groupReport.rows, ['groupName', 'groupTurnover', 'totalTurnover', 'sharePercent'], 'group-share-report')}
                >
                  Экспорт PDF
                </button>
              </div>
              {groupReport?.dynamics?.length > 0 && (
                <>
                  <h4>Динамика Вгр по месяцам</h4>
                  <div className="chart-box">
                    <ResponsiveContainer width="100%" height={300}>
                      <LineChart
                        data={groupReport.dynamics}
                        onMouseLeave={() => setGroupHoverLineKey(null)}
                      >
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="month" />
                        <YAxis />
                        <Tooltip />
                        <Legend />
                        {groupDynamicKeys.map((key, idx) => (
                          <Line
                            key={key}
                            type="monotone"
                            dataKey={key}
                            stroke={palette[idx % palette.length]}
                            strokeWidth={groupHoverLineKey === key ? 3.5 : 2}
                            dot={false}
                            activeDot={{ r: 5 }}
                            onMouseEnter={() => setGroupHoverLineKey(key)}
                          />
                        ))}
                      </LineChart>
                    </ResponsiveContainer>
                  </div>
                </>
              )}
            </>
          )}
        </Section>
      )}

      <div className="tabs">
  {availableTabs.map((tab) => (
    <button
      key={tab}
      className={activeTab === tab ? 'active' : ''}
      onClick={() => setActiveTab(tab)}
    >
      {tab === 'product' && 'Товары'}
      {tab === 'user' && 'Пользователи'}
      {tab === 'order' && 'Заказы'}
      {tab === 'analyze' && 'Анализ'}
    </button>
  ))}
</div>

      {!availableTabs.length && <div className="error-msg">Нет доступа ни к одному разделу аналитики</div>}

      {activeTab === 'product' && (
        <Section title="Товарная аналитика (категории/бренды/цены/маршруты)">
          {productError && <div className="error-msg">{productError}</div>}
          <h4>Категории</h4>
          {renderPie(productData.categoryShare, 'product-category')}
          <h4>Бренды</h4>
          {renderPie(productData.brandShare, 'product-brand')}
          <h4>Ценовые диапазоны</h4>
          {renderBuckets(productData.priceBuckets, 'Диапазон')}
          <h4>Топ городов маршрутов</h4>
          {renderPie(productData.topCitiesRoutes, 'product-routes')}
          <h4>Дистанции маршрутов</h4>
          {renderBuckets(productData.routeDistanceBuckets, 'Дистанция')}
        </Section>
      )}

      {activeTab === 'user' && (
        <Section title="Пользователи (возраст, логины по часам)">
          {userError && <div className="error-msg">{userError}</div>}
          <h4>Возраст 5-летние</h4>
          {renderBuckets(
  userData.ageBuckets?.map(item => ({
    label: item.bucket,
    value: item.count
  })), 
  'Возраст'
        )}
          
          <h4>Логины по часам (30 дней)</h4>
          {renderBuckets(userData.loginByHour, 'Час')}
        </Section>
      )}

      {activeTab === 'order' && (
        <Section title="Заказы (топы, выручка)">
          {orderError && <div className="error-msg">{orderError}</div>}
          <h4>Топ бренды</h4>
          {renderPie(orderData.topBrands, 'order-brand')}
          <h4>Топ категории</h4>
          {renderPie(orderData.topCategories, 'order-category')}
          <h4>Топ товары</h4>
          {renderPie(orderData.topProducts, 'order-products')}
          <h4>Выручка по месяцам (12м)</h4>
          {renderTimeSeries(orderData.revenueByMonth, 'orders-revenue')}
          <h4>Бестселлеры по месяцам</h4>
          {renderTimeSeries(orderData.bestsellersByMonth, 'orders-best')}
          <h4>Расширенный анализ заказов</h4>
          <p className="analytics-filter-hint">Уточнение по статусу, полу и возрастной группе (категория и бренд уже на диаграммах выше).</p>
          <div className="filters">
            <select
              value={orderFilter.status}
              onChange={(e) => setOrderFilter((prev) => ({ ...prev, status: e.target.value }))}
            >
              <option value="">Все статусы</option>
              <option value="PROCESSING">{translateOrderStatus('PROCESSING')}</option>
              <option value="IN_TRANSIT">{translateOrderStatus('IN_TRANSIT')}</option>
              <option value="DELIVERED">{translateOrderStatus('DELIVERED')}</option>
              <option value="CANCELLED">{translateOrderStatus('CANCELLED')}</option>
            </select>
            <select
              value={orderFilter.gender}
              onChange={(e) => setOrderFilter((prev) => ({ ...prev, gender: e.target.value }))}
            >
              <option value="">Пол: любой</option>
              <option value="M">{translateGender('M')}</option>
              <option value="F">{translateGender('F')}</option>
              <option value="N">{translateGender('N')}</option>
            </select>
            <select
              value={orderFilter.ageGroup}
              onChange={(e) => setOrderFilter((prev) => ({ ...prev, ageGroup: e.target.value }))}
            >
              {AGE_BUCKET_OPTIONS.map((opt) => (
                <option key={opt.value || 'any'} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
            <button
              type="button"
              onClick={() => setOrderFilter({ status: '', gender: '', ageGroup: '' })}
            >
              Очистить фильтр
            </button>
          </div>
          <h4>Бренды (фильтр)</h4>
          {renderPie(orderData.filtered?.brands, 'filtered-brand')}
          <h4>Категории (фильтр)</h4>
          {renderPie(orderData.filtered?.categories, 'filtered-category')}
          <h4>Товары (фильтр)</h4>
          {renderPie(orderData.filtered?.products, 'filtered-products')}
        </Section>
      )}

      {activeTab === 'analyze' && (
        <Section title="Глубокий анализ (товары / категории / бренды)">
          {analyzeError && <div className="error-msg">{analyzeError}</div>}
          <div className="filters">
            <select
              value={analyzeFilters.scope}
              onChange={(e) => setAnalyzeFilters((prev) => ({ ...prev, scope: e.target.value }))}
            >
              <option value="products">Товары</option>
              <option value="categories">Категории</option>
              <option value="brands">Бренды</option>
            </select>
            <select
              value={analyzeFilters.gender}
              onChange={(e) => setAnalyzeFilters((prev) => ({ ...prev, gender: e.target.value }))}
            >
              <option value="">Пол: любой</option>
              <option value="M">{translateGender('M')}</option>
              <option value="F">{translateGender('F')}</option>
              <option value="N">{translateGender('N')}</option>
            </select>
            <select
              value={analyzeFilters.ageGroup}
              onChange={(e) => setAnalyzeFilters((prev) => ({ ...prev, ageGroup: e.target.value }))}
            >
              {AGE_BUCKET_OPTIONS.map((opt) => (
                <option key={`a-${opt.value || 'any'}`} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
            <select
              value={analyzeFilters.month}
              onChange={(e) => setAnalyzeFilters((prev) => ({ ...prev, month: e.target.value }))}
            >
              <option value="">Месяц заказа: любой</option>
              {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
                <option key={m} value={String(m)}>
                  {m}
                </option>
              ))}
            </select>
            <button
              type="button"
              onClick={() =>
                setAnalyzeFilters({ scope: 'products', gender: '', ageGroup: '', month: '' })
              }
            >
              Очистить фильтр
            </button>
          </div>
          {renderPie(analyzeData, 'analyze')}
        </Section>
      )}
    </div>
  );
}

export default Analytics;




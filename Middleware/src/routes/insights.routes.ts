import { Router, Request, Response } from 'express';
import { BackendClient } from '../services/backendClient';
import { config } from '../config/env';

export const insightsRouter = Router();

const invoices = [
  {
    id: 1,
    invoiceNumber: 'INV-2026-8801',
    vendorName: 'Apex Cloud Solutions',
    counterpartyEmail: 'billing@apexcloud.io',
    issueDate: '2026-09-01',
    dueDate: '2026-10-01',
    amount: 45000.0,
    currency: 'USD',
    status: 'OVERDUE',
    agingDays: 14,
  },
  {
    id: 2,
    invoiceNumber: 'INV-2026-8802',
    vendorName: 'Nordic Supply Chain Logistics',
    counterpartyEmail: 'ap@nordiclogistics.com',
    issueDate: '2026-09-15',
    dueDate: '2026-10-15',
    amount: 67200.0,
    currency: 'USD',
    status: 'PENDING',
    agingDays: 0,
  },
  {
    id: 3,
    invoiceNumber: 'INV-2026-8803',
    vendorName: 'Global Telecom Fiber',
    counterpartyEmail: 'finance@globaltelecom.net',
    issueDate: '2026-08-01',
    dueDate: '2026-08-31',
    amount: 18400.0,
    currency: 'USD',
    status: 'OVERDUE',
    agingDays: 45,
  },
];

const cashFlowData = [
  { date: '2026-09-01', inflow: 180000, outflow: 95000, projectedBalance: 1200000, isForecast: false },
  { date: '2026-09-15', inflow: 220000, outflow: 140000, projectedBalance: 1280000, isForecast: false },
  { date: '2026-10-01', inflow: 250000, outflow: 160000, projectedBalance: 1370000, isForecast: false },
  { date: '2026-10-14', inflow: 210000, outflow: 95000, projectedBalance: 1485230, isForecast: false },
  { date: '2026-11-01', inflow: 260000, outflow: 150000, projectedBalance: 1595230, isForecast: true, confidenceUpper: 1650000, confidenceLower: 1540000 },
  { date: '2026-11-15', inflow: 280000, outflow: 170000, projectedBalance: 1705230, isForecast: true, confidenceUpper: 1780000, confidenceLower: 1630000 },
  { date: '2026-12-01', inflow: 310000, outflow: 180000, projectedBalance: 1835230, isForecast: true, confidenceUpper: 1940000, confidenceLower: 1720000 },
];

/**
 * GET /api/v1/insights/invoices
 */
insightsRouter.get('/invoices', async (req: Request, res: Response) => {
  const result = await BackendClient.requestWithFallback(
    '/api/v1/insights/invoices',
    {
      method: 'GET',
      correlationId: req.correlationId,
      directServiceUrl: config.services.financialInsights,
    },
    () => invoices
  );

  return res.json({ success: true, data: result.data, source: result.source });
});

/**
 * GET /api/v1/insights/analytics/cash-flow
 */
insightsRouter.get('/analytics/cash-flow', async (req: Request, res: Response) => {
  const result = await BackendClient.requestWithFallback(
    '/api/v1/insights/analytics/cash-flow',
    {
      method: 'GET',
      correlationId: req.correlationId,
      directServiceUrl: config.services.financialInsights,
    },
    () => cashFlowData
  );

  return res.json({ success: true, data: result.data, source: result.source });
});

/**
 * GET /api/v1/insights/aging
 */
insightsRouter.get('/aging', (req: Request, res: Response) => {
  const agingBuckets = {
    current: { count: 1, amount: 67200.0, label: '0-30 Days (Current)' },
    overdue30: { count: 1, amount: 45000.0, label: '31-60 Days Overdue' },
    overdue60: { count: 1, amount: 18400.0, label: '61-90 Days Overdue' },
    overdue90Plus: { count: 0, amount: 0.0, label: '90+ Days Overdue' },
    totalReceivables: 130600.0,
  };
  return res.json({ success: true, data: agingBuckets });
});

/**
 * GET /api/v1/insights/expenses
 */
insightsRouter.get('/expenses', (req: Request, res: Response) => {
  const expenses = [
    { category: 'Cloud Infrastructure', amount: 84500, percentage: 38 },
    { category: 'Payroll & Compensation', amount: 68200, percentage: 31 },
    { category: 'Professional Advisory', amount: 39400, percentage: 18 },
    { category: 'Software Subscriptions', amount: 28900, percentage: 13 },
  ];
  return res.json({ success: true, data: expenses });
});

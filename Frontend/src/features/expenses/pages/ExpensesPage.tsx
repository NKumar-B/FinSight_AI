import React, { useState } from 'react';
import { TrendingDown, AlertCircle, ArrowUpRight, Filter, DollarSign, Calendar } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Select } from '@/components/ui/Select';
import { DataTable, Column } from '@/components/ui/DataTable';
import { formatCurrency, formatDate } from '@/utils/formatters';

interface ExpenseRecord {
  id: number;
  vendor: string;
  category: string;
  amount: number;
  date: string;
  anomalyFlag?: boolean;
  status: 'APPROVED' | 'PENDING_AUDIT' | 'FLAGGED';
}

const EXPENSES_LIST: ExpenseRecord[] = [
  { id: 1, vendor: 'Amazon Web Services', category: 'Cloud Infrastructure', amount: 34500, date: '2026-10-14', status: 'APPROVED' },
  { id: 2, vendor: 'Baker & McKinsey LLP', category: 'Professional Services', amount: 12400, date: '2026-10-13', status: 'PENDING_AUDIT' },
  { id: 3, vendor: 'Zenith Holdings Ltd', category: 'Wire Settlement', amount: 98500, date: '2026-10-14', anomalyFlag: true, status: 'FLAGGED' },
  { id: 4, vendor: 'Datadog & Snowflake Inc', category: 'Software & Monitoring', amount: 8900, date: '2026-10-10', status: 'APPROVED' },
  { id: 5, vendor: 'WeWork Global Enterprise', category: 'Facilities & Rent', amount: 24000, date: '2026-10-01', status: 'APPROVED' },
];

export const ExpensesPage: React.FC = () => {
  const [period, setPeriod] = useState('CURRENT_MONTH');

  const columns: Column<ExpenseRecord>[] = [
    {
      key: 'vendor',
      header: 'Vendor / Payee',
      render: (e) => (
        <div>
          <div className="font-semibold text-slate-900 dark:text-white text-xs">{e.vendor}</div>
          <div className="text-[11px] text-slate-400 dark:text-slate-500">{e.category}</div>
        </div>
      ),
    },
    {
      key: 'date',
      header: 'Expense Date',
      render: (e) => <span className="text-slate-500 dark:text-slate-400 text-xs">{formatDate(e.date)}</span>,
    },
    {
      key: 'amount',
      header: 'Amount',
      className: 'text-right font-bold table-cell-num text-slate-900 dark:text-slate-100',
      render: (e) => <span>{formatCurrency(e.amount)}</span>,
    },
    {
      key: 'anomalyFlag',
      header: 'AI Intelligence Flag',
      render: (e) =>
        e.anomalyFlag ? (
          <span className="inline-flex items-center gap-1 text-[11px] font-bold text-rose-700 dark:text-rose-300 bg-rose-50 dark:bg-rose-950/40 px-2 py-0.5 rounded border border-rose-200 dark:border-rose-800">
            <AlertCircle className="w-3.5 h-3.5 text-rose-600 dark:text-rose-400" />
            +420% Velocity Spike
          </span>
        ) : (
          <span className="text-xs text-slate-400 dark:text-slate-500">Within Budget Normal</span>
        ),
    },
    {
      key: 'status',
      header: 'Approval Status',
      render: (e) => (
        <Badge
          variant={e.status === 'APPROVED' ? 'success' : e.status === 'FLAGGED' ? 'danger' : 'warning'}
          size="sm"
        >
          {e.status}
        </Badge>
      ),
    },
  ];

  return (
    <div className="space-y-6 pb-12">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-2 border-b border-slate-200 dark:border-slate-800">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 dark:text-white">
            Expense Intelligence & Spend Analytics
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Automated cost categorization, vendor leakage identification, and spending anomaly detection.
          </p>
        </div>

        <div className="w-48">
          <Select
            value={period}
            onChange={(e) => setPeriod(e.target.value)}
            options={[
              { label: 'Current Month (Oct 2026)', value: 'CURRENT_MONTH' },
              { label: 'Previous Quarter (Q3 2026)', value: 'Q3_2026' },
              { label: 'Year-to-Date (FY 2026)', value: 'YTD_2026' },
            ]}
          />
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card className="border-l-4 border-l-brand-600 glow-hover-brand transition-all">
          <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase">Total Period Spend</div>
          <div className="text-2xl font-bold text-slate-900 dark:text-white mt-1 table-cell-num">{formatCurrency(178300)}</div>
          <span className="text-[11px] text-slate-400 dark:text-slate-500 mt-1 block">Across 5 primary vendor contracts</span>
        </Card>

        <Card className="border-l-4 border-l-rose-500 glow-hover-rose transition-all">
          <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase">Unusual Spend Flagged</div>
          <div className="text-2xl font-bold text-rose-600 dark:text-rose-400 mt-1 table-cell-num">{formatCurrency(98500)}</div>
          <span className="text-[11px] text-rose-700 dark:text-rose-400 font-medium mt-1 block">1 wire flagged for offshore review</span>
        </Card>

        <Card className="border-l-4 border-l-emerald-600 glow-hover-emerald transition-all">
          <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase">Projected Savings Identified</div>
          <div className="text-2xl font-bold text-emerald-600 dark:text-emerald-400 mt-1 table-cell-num">{formatCurrency(14200)}</div>
          <span className="text-[11px] text-slate-400 dark:text-slate-500 mt-1 block">SaaS redundancy elimination</span>
        </Card>
      </div>

      {/* Expense Table */}
      <Card title="Corporate Expenditures Ledger" subtitle="Granular line-item spending with anomaly markers">
        <DataTable
          data={EXPENSES_LIST}
          columns={columns}
          searchPlaceholder="Search vendor, category, or status..."
        />
      </Card>
    </div>
  );
};

import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Receipt, AlertCircle, Clock, CheckCircle2, DollarSign, Calendar } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { DataTable, Column } from '@/components/ui/DataTable';
import { finSightApi } from '@/services/api/finSightApi';
import { Invoice } from '@/types/financial';
import { formatCurrency, formatDate } from '@/utils/formatters';

export const InvoicesReceivablesPage: React.FC = () => {
  const { data: invoices, isLoading } = useQuery({
    queryKey: ['invoices'],
    queryFn: finSightApi.getInvoices,
  });

  const totalOutstanding = invoices?.reduce((acc, inv) => acc + (inv.status !== 'PAID' ? inv.amount : 0), 0) || 130600;
  const overdueTotal = invoices?.reduce((acc, inv) => acc + (inv.status === 'OVERDUE' ? inv.amount : 0), 0) || 63400;

  const columns: Column<Invoice>[] = [
    {
      key: 'invoiceNumber',
      header: 'Invoice #',
      sortable: true,
      render: (inv) => <span className="font-mono text-xs font-bold text-brand-700">{inv.invoiceNumber}</span>,
    },
    {
      key: 'vendorName',
      header: 'Counterparty / Client',
      render: (inv) => (
        <div>
          <div className="font-semibold text-slate-900 text-xs">{inv.vendorName}</div>
          <div className="text-[11px] text-slate-400">{inv.counterpartyEmail}</div>
        </div>
      ),
    },
    {
      key: 'dueDate',
      header: 'Due Date',
      sortable: true,
      render: (inv) => <span className="text-slate-600 text-xs">{formatDate(inv.dueDate)}</span>,
    },
    {
      key: 'amount',
      header: 'Invoice Amount',
      sortable: true,
      className: 'text-right font-bold table-cell-num text-slate-900',
      render: (inv) => <span>{formatCurrency(inv.amount, inv.currency)}</span>,
    },
    {
      key: 'agingDays',
      header: 'Aging Metric',
      sortable: true,
      className: 'text-center',
      render: (inv) => {
        if (inv.agingDays > 30) {
          return (
            <span className="font-bold text-xs text-rose-700 bg-rose-50 px-2 py-0.5 rounded border border-rose-200">
              +{inv.agingDays}d Critical
            </span>
          );
        }
        if (inv.agingDays > 0) {
          return (
            <span className="font-bold text-xs text-amber-700 bg-amber-50 px-2 py-0.5 rounded border border-amber-200">
              +{inv.agingDays}d Overdue
            </span>
          );
        }
        return <span className="text-xs text-slate-500 font-medium">Due in {Math.abs(inv.agingDays)}d</span>;
      },
    },
    {
      key: 'status',
      header: 'Status',
      sortable: true,
      render: (inv) => (
        <Badge
          variant={inv.status === 'PAID' ? 'success' : inv.status === 'OVERDUE' ? 'danger' : 'warning'}
          size="sm"
        >
          {inv.status}
        </Badge>
      ),
    },
  ];

  return (
    <div className="space-y-6 pb-12">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900">
            Invoices & Accounts Receivable (AR)
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Track customer invoicing, monitor DSO aging buckets, and forecast collection liquidity.
          </p>
        </div>
      </div>

      {/* AR Aging Buckets Grid */}
      <Card title="Receivables Aging Matrix (DSO Analysis)" subtitle="Distribution of outstanding receivables by delinquency window">
        <div className="grid grid-cols-2 sm:grid-cols-5 gap-3 py-2">
          <div className="p-3.5 rounded-xl border border-slate-200 bg-slate-50/50">
            <span className="text-[10px] font-bold uppercase tracking-wider text-slate-500">Current (Not Due)</span>
            <div className="text-base font-extrabold text-slate-900 mt-1 table-cell-num">{formatCurrency(67200)}</div>
            <span className="text-[10px] text-emerald-600 font-semibold block mt-1">1 Account</span>
          </div>
          <div className="p-3.5 rounded-xl border border-amber-200 bg-amber-50/50">
            <span className="text-[10px] font-bold uppercase tracking-wider text-amber-700">1 - 30 Days</span>
            <div className="text-base font-extrabold text-amber-800 mt-1 table-cell-num">{formatCurrency(45000)}</div>
            <span className="text-[10px] text-amber-700 font-semibold block mt-1">1 Account (Apex)</span>
          </div>
          <div className="p-3.5 rounded-xl border border-rose-200 bg-rose-50/50">
            <span className="text-[10px] font-bold uppercase tracking-wider text-rose-700">31 - 60 Days</span>
            <div className="text-base font-extrabold text-rose-800 mt-1 table-cell-num">{formatCurrency(18400)}</div>
            <span className="text-[10px] text-rose-700 font-semibold block mt-1">1 Account (Telecom)</span>
          </div>
          <div className="p-3.5 rounded-xl border border-slate-200 bg-slate-50/50">
            <span className="text-[10px] font-bold uppercase tracking-wider text-slate-500">61 - 90 Days</span>
            <div className="text-base font-extrabold text-slate-500 mt-1 table-cell-num">{formatCurrency(0)}</div>
            <span className="text-[10px] text-slate-400 block mt-1">0 Accounts</span>
          </div>
          <div className="p-3.5 rounded-xl border border-slate-200 bg-slate-50/50">
            <span className="text-[10px] font-bold uppercase tracking-wider text-slate-500">90+ Days</span>
            <div className="text-base font-extrabold text-slate-500 mt-1 table-cell-num">{formatCurrency(0)}</div>
            <span className="text-[10px] text-slate-400 block mt-1">0 Accounts</span>
          </div>
        </div>
      </Card>

      {/* Invoices Table */}
      <Card title="Corporate Receivables Ledger" subtitle="Itemized invoices with customer tracking and aging status">
        <DataTable
          data={invoices || []}
          columns={columns}
          searchPlaceholder="Search invoice #, client name..."
          isLoading={isLoading}
        />
      </Card>
    </div>
  );
};

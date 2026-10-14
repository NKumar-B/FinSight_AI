import React, { useState } from 'react';
import { FileSpreadsheet, Download, FileText, CheckCircle, Calendar, Filter } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Select } from '@/components/ui/Select';
import { DataTable, Column } from '@/components/ui/DataTable';

interface ReportTemplate {
  id: string;
  name: string;
  category: 'RECONCILIATION' | 'FRAUD' | 'FINANCIAL_INSIGHTS' | 'AUDIT';
  frequency: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'ON_DEMAND';
  lastGenerated: string;
  format: 'PDF' | 'CSV' | 'XLSX';
  status: 'READY' | 'GENERATING';
}

const REPORT_CATALOG: ReportTemplate[] = [
  {
    id: 'RPT-001',
    name: 'Executive Daily Reconciliation Summary',
    category: 'RECONCILIATION',
    frequency: 'DAILY',
    lastGenerated: '2026-10-14 08:05:00',
    format: 'PDF',
    status: 'READY',
  },
  {
    id: 'RPT-002',
    name: 'AML Surveillance & Critical Alert Dossier',
    category: 'FRAUD',
    frequency: 'ON_DEMAND',
    lastGenerated: '2026-10-14 11:45:00',
    format: 'PDF',
    status: 'READY',
  },
  {
    id: 'RPT-003',
    name: 'Receivables Aging & Cash-Flow Runway Forecast',
    category: 'FINANCIAL_INSIGHTS',
    frequency: 'WEEKLY',
    lastGenerated: '2026-10-13 18:00:00',
    format: 'XLSX',
    status: 'READY',
  },
  {
    id: 'RPT-004',
    name: 'SOC-2 Immutable Audit Log & Hash Export',
    category: 'AUDIT',
    frequency: 'MONTHLY',
    lastGenerated: '2026-10-01 00:00:00',
    format: 'CSV',
    status: 'READY',
  },
];

export const ReportsPage: React.FC = () => {
  const [downloadSuccess, setDownloadSuccess] = useState<string | null>(null);

  const handleDownload = (report: ReportTemplate) => {
    setDownloadSuccess(`Downloading ${report.name} (${report.format})...`);
    setTimeout(() => setDownloadSuccess(null), 3000);
  };

  const columns: Column<ReportTemplate>[] = [
    {
      key: 'id',
      header: 'Report Code',
      sortable: true,
      render: (r) => <span className="font-mono text-xs font-bold text-brand-700">{r.id}</span>,
    },
    {
      key: 'name',
      header: 'Report Title',
      render: (r) => (
        <div>
          <div className="font-semibold text-slate-900 text-xs">{r.name}</div>
          <div className="text-[11px] text-slate-400">Category: {r.category}</div>
        </div>
      ),
    },
    {
      key: 'frequency',
      header: 'Cadence',
      render: (r) => (
        <span className="text-[11px] font-semibold text-slate-600 bg-slate-100 px-2 py-0.5 rounded">
          {r.frequency}
        </span>
      ),
    },
    {
      key: 'lastGenerated',
      header: 'Last Generated',
      render: (r) => <span className="text-slate-500 text-xs">{r.lastGenerated}</span>,
    },
    {
      key: 'format',
      header: 'Format',
      render: (r) => (
        <span className="font-mono text-xs font-extrabold text-indigo-700 bg-indigo-50 px-2 py-0.5 rounded border border-indigo-200">
          {r.format}
        </span>
      ),
    },
    {
      key: 'action',
      header: 'Action',
      className: 'text-right',
      render: (r) => (
        <Button
          size="sm"
          variant="outline"
          leftIcon={<Download className="w-3.5 h-3.5" />}
          onClick={() => handleDownload(r)}
        >
          Export
        </Button>
      ),
    },
  ];

  return (
    <div className="space-y-6 pb-12">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900">
            Reports & Compliance Exports
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Automated regulatory, financial, and forensic report generation with verified audit trails.
          </p>
        </div>
      </div>

      {downloadSuccess && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-xs text-emerald-800 font-semibold flex items-center gap-2">
          <CheckCircle className="w-4 h-4 text-emerald-600" />
          {downloadSuccess}
        </div>
      )}

      {/* Reports Table */}
      <Card title="Executive Compliance & Financial Catalog" subtitle="Download official signed financial statements and reconciliation packages">
        <DataTable
          data={REPORT_CATALOG}
          columns={columns}
          searchPlaceholder="Search reports by title or code..."
        />
      </Card>
    </div>
  );
};

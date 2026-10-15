import React, { useState } from 'react';
import {
  FileSpreadsheet,
  Download,
  FileText,
  CheckCircle,
  Calendar,
  Filter,
  ShieldCheck,
  Lock,
  Sparkles,
  FileDown,
  KeyRound,
} from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { DataTable, Column } from '@/components/ui/DataTable';
import {
  SecureExportModal,
  ReportTemplate,
} from '../components/SecurePdfExportModal';
import {
  generateSecureReport,
} from '../utils/reportExporter';

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
    format: 'XLSX',
    status: 'READY',
  },
];

export const ReportsPage: React.FC = () => {
  const [selectedReport, setSelectedReport] = useState<ReportTemplate | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isExporting, setIsExporting] = useState(false);

  const [notification, setNotification] = useState<{
    message: string;
    filename: string;
    password?: string;
  } | null>(null);

  // Trigger secure export flow for PDF or Excel
  const handleExportClick = (report: ReportTemplate) => {
    setSelectedReport(report);
    setIsModalOpen(true);
  };

  // Execute password-encrypted or standard document generation
  const handleExecuteExport = async (password?: string) => {
    if (!selectedReport) return;
    setIsExporting(true);

    try {
      const result = await generateSecureReport({
        reportId: selectedReport.id,
        reportName: selectedReport.name,
        format: selectedReport.format,
        password,
      });

      setNotification({
        message: password
          ? `Password-protected ${selectedReport.format} generated and downloaded successfully!`
          : `Official ${selectedReport.format} document generated and downloaded successfully!`,
        filename: result.filename,
        password,
      });
      setTimeout(() => setNotification(null), 10000);
    } catch (err: any) {
      console.error('Document export failed:', err);
    } finally {
      setIsExporting(false);
    }
  };

  const columns: Column<ReportTemplate>[] = [
    {
      key: 'id',
      header: 'Report Code',
      sortable: true,
      render: (r) => (
        <span className="font-mono text-xs font-bold text-brand-600 dark:text-brand-400">
          {r.id}
        </span>
      ),
    },
    {
      key: 'name',
      header: 'Report Title',
      render: (r) => (
        <div>
          <div className="font-semibold text-slate-900 dark:text-white text-xs">
            {r.name}
          </div>
          <div className="text-[11px] text-slate-400 dark:text-slate-500">
            Category: {r.category}
          </div>
        </div>
      ),
    },
    {
      key: 'frequency',
      header: 'Cadence',
      render: (r) => (
        <span className="text-[11px] font-semibold text-slate-600 dark:text-slate-300 bg-slate-100 dark:bg-slate-800 px-2 py-0.5 rounded">
          {r.frequency}
        </span>
      ),
    },
    {
      key: 'lastGenerated',
      header: 'Last Generated',
      render: (r) => (
        <span className="text-slate-500 dark:text-slate-400 text-xs">
          {r.lastGenerated}
        </span>
      ),
    },
    {
      key: 'format',
      header: 'Format',
      render: (r) => (
        <div className="flex items-center gap-1.5">
          <span
            className={`font-mono text-xs font-extrabold px-2 py-0.5 rounded border ${
              r.format === 'PDF'
                ? 'bg-rose-50 dark:bg-rose-950/40 border-rose-200 dark:border-rose-800 text-rose-700 dark:text-rose-400'
                : 'bg-emerald-50 dark:bg-emerald-950/40 border-emerald-200 dark:border-emerald-800 text-emerald-700 dark:text-emerald-400'
            }`}
          >
            {r.format}
          </span>
          <span title="Password Encryption Supported">
            <Lock className="w-3 h-3 text-emerald-600 dark:text-emerald-400" />
          </span>
        </div>
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
          leftIcon={<Lock className="w-3.5 h-3.5 text-brand-600 dark:text-brand-400" />}
          onClick={() => handleExportClick(r)}
          className="hover:border-brand-500 font-semibold"
        >
          Secure Export
        </Button>
      ),
    },
  ];

  return (
    <div className="space-y-6 pb-12">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-2 border-b border-slate-200 dark:border-slate-800">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 dark:text-white flex items-center gap-2">
            <FileSpreadsheet className="w-6 h-6 text-brand-600" />
            Reports & Compliance Exports
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Automated regulatory, financial, and forensic report generation with verified audit trails & AES-128/OOXML password encryption.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Badge variant="success" size="md">
            SOC-2 Type II Certified
          </Badge>
          <div className="hidden sm:flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-brand-50 dark:bg-brand-950/40 border border-brand-200 dark:border-brand-800 text-brand-700 dark:text-brand-300 text-xs font-semibold">
            <Lock className="w-3.5 h-3.5" />
            <span>PDF & Excel Password Encryption Active</span>
          </div>
        </div>
      </div>

      {/* Dynamic Notification Toast */}
      {notification && (
        <div className="p-4 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 rounded-2xl text-xs text-emerald-900 dark:text-emerald-200 shadow-sm animate-fadeIn flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-start gap-3">
            <div className="p-1.5 rounded-lg bg-emerald-100 dark:bg-emerald-900 text-emerald-700 dark:text-emerald-300 shrink-0 mt-0.5">
              <CheckCircle className="w-4 h-4" />
            </div>
            <div>
              <span className="font-bold block text-sm">{notification.message}</span>
              <span className="font-mono text-[11px] text-emerald-700 dark:text-emerald-400">
                Downloaded: {notification.filename}
              </span>
            </div>
          </div>

          {notification.password && (
            <div className="flex items-center gap-2 bg-white dark:bg-slate-900 px-3 py-1.5 rounded-xl border border-emerald-300 dark:border-emerald-800 self-start sm:self-auto">
              <KeyRound className="w-4 h-4 text-emerald-600 dark:text-emerald-400 shrink-0" />
              <span className="text-[11px] font-semibold text-slate-600 dark:text-slate-400">
                Document Password:
              </span>
              <span className="font-mono text-xs font-extrabold text-emerald-700 dark:text-emerald-300 bg-emerald-50 dark:bg-emerald-950/60 px-2 py-0.5 rounded border border-emerald-200 dark:border-emerald-700">
                {notification.password}
              </span>
            </div>
          )}
        </div>
      )}

      {/* Reports Catalog Table */}
      <Card
        title="Executive Compliance & Financial Catalog"
        subtitle="Download official signed financial statements, forensic dossiers, and reconciliation packages with custom password protection"
      >
        <DataTable
          data={REPORT_CATALOG}
          columns={columns}
          searchPlaceholder="Search reports by title, category, or code..."
        />
      </Card>

      {/* Modal: Configure PDF & Excel Security & Password Encryption */}
      <SecureExportModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        report={selectedReport}
        onExport={handleExecuteExport}
        isExporting={isExporting}
      />
    </div>
  );
};


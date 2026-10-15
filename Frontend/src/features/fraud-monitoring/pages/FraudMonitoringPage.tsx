import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { ShieldAlert, AlertTriangle, FileText, CheckCircle, Search, ShieldX } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { DataTable, Column } from '@/components/ui/DataTable';
import { finSightApi } from '@/services/api/finSightApi';
import { FraudAlert } from '@/types/financial';
import { formatCurrency, formatDateTime, getSeverityBadgeVariant } from '@/utils/formatters';

export const FraudMonitoringPage: React.FC = () => {
  const [selectedAlert, setSelectedAlert] = useState<FraudAlert | null>(null);
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);

  const { data: alerts, isLoading } = useQuery({
    queryKey: ['fraudAlerts'],
    queryFn: finSightApi.getFraudAlerts,
  });

  const handleAcknowledge = () => {
    setActionSuccess('Alert acknowledged and assigned to current compliance investigator.');
    setTimeout(() => setActionSuccess(null), 3000);
  };

  const handleFileSar = () => {
    setActionSuccess('Suspicious Activity Report (SAR-2026-0041) filed with regulatory compliance audit trail.');
    setTimeout(() => {
      setActionSuccess(null);
      setSelectedAlert(null);
    }, 2500);
  };

  const columns: Column<FraudAlert>[] = [
    {
      key: 'alertReference',
      header: 'Alert ID',
      sortable: true,
      render: (a) => <span className="font-mono text-xs font-bold text-rose-700 dark:text-rose-400">{a.alertReference}</span>,
    },
    {
      key: 'detectedAt',
      header: 'Detection Time',
      sortable: true,
      render: (a) => <span className="text-slate-500 dark:text-slate-400 text-xs">{formatDateTime(a.detectedAt)}</span>,
    },
    {
      key: 'detectionRule',
      header: 'AML Rule Violated',
      render: (a) => (
        <div>
          <div className="font-semibold text-slate-800 dark:text-slate-100 text-xs">{a.detectionRule}</div>
          <div className="text-[11px] text-slate-400 dark:text-slate-500 font-mono">Account: {a.accountNumber}</div>
        </div>
      ),
    },
    {
      key: 'amount',
      header: 'Transaction Amount',
      sortable: true,
      className: 'text-right font-bold table-cell-num text-slate-900 dark:text-slate-100',
      render: (a) => <span>{formatCurrency(a.amount, a.currency)}</span>,
    },
    {
      key: 'riskScore',
      header: 'Risk Score',
      sortable: true,
      className: 'text-center',
      render: (a) => {
        const isHigh = a.riskScore >= 80;
        return (
          <span
            className={`inline-block px-2.5 py-0.5 rounded-full font-black text-xs ${
              isHigh
                ? 'bg-rose-100 dark:bg-rose-950/50 text-rose-800 dark:text-rose-300 border border-rose-200 dark:border-rose-800'
                : 'bg-amber-100 dark:bg-amber-950/50 text-amber-800 dark:text-amber-300 border border-amber-200 dark:border-amber-800'
            }`}
          >
            {a.riskScore} / 100
          </span>
        );
      },
    },
    {
      key: 'severity',
      header: 'Severity',
      sortable: true,
      render: (a) => (
        <Badge variant={getSeverityBadgeVariant(a.severity)} size="sm">
          {a.severity}
        </Badge>
      ),
    },
    {
      key: 'status',
      header: 'Investigation Status',
      sortable: true,
      render: (a) => (
        <Badge variant={a.status === 'CONFIRMED_FRAUD' ? 'danger' : a.status === 'UNDER_REVIEW' ? 'warning' : 'neutral'} size="sm">
          {a.status}
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
            Fraud Monitoring & AML Surveillance
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Real-time velocity scoring, suspicious pattern identification, and regulatory SAR workflow.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Badge variant="danger" size="md" dot>
            Surveillance Active
          </Badge>
        </div>
      </div>

      {actionSuccess && (
        <div className="p-3 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 rounded-lg text-xs text-emerald-800 dark:text-emerald-300 font-semibold flex items-center gap-2">
          <CheckCircle className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
          {actionSuccess}
        </div>
      )}

      {/* KPI Overview */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card className="border-l-4 border-l-rose-600 glow-hover-rose transition-all">
          <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase">Critical Severity Alerts</div>
          <div className="text-2xl font-extrabold text-rose-600 dark:text-rose-400 mt-1">1 Case</div>
          <p className="text-[11px] text-slate-400 dark:text-slate-500 mt-1">FATF Grey List Outbound Wire</p>
        </Card>

        <Card className="border-l-4 border-l-amber-500 glow-hover-amber transition-all">
          <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase">Medium Severity Warnings</div>
          <div className="text-2xl font-extrabold text-amber-600 dark:text-amber-400 mt-1">1 Warning</div>
          <p className="text-[11px] text-slate-400 dark:text-slate-500 mt-1">Structuring / Smurfing Threshold</p>
        </Card>

        <Card className="border-l-4 border-l-emerald-600 glow-hover-emerald transition-all">
          <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase">Automated Clearing Rate</div>
          <div className="text-2xl font-extrabold text-emerald-600 dark:text-emerald-400 mt-1">99.88%</div>
          <p className="text-[11px] text-slate-400 dark:text-slate-500 mt-1">False-positive suppression enabled</p>
        </Card>
      </div>

      {/* Alerts Table */}
      <Card title="Active Surveillance Alert Queue" subtitle="Click any alert to inspect risk factors, evidence provenance, or file a SAR">
        <DataTable
          data={alerts || []}
          columns={columns}
          searchPlaceholder="Search alerts by reference or account..."
          isLoading={isLoading}
          onRowClick={(alert) => setSelectedAlert(alert)}
        />
      </Card>

      {/* Alert Detail Modal */}
      {selectedAlert && (
        <Modal
          isOpen={!!selectedAlert}
          onClose={() => setSelectedAlert(null)}
          title={`Investigate ${selectedAlert.alertReference}`}
          description={`Risk Score: ${selectedAlert.riskScore}/100 • Evaluated on ${formatDateTime(selectedAlert.detectedAt)}`}
          maxWidth="lg"
        >
          <div className="space-y-4 text-xs">
            {/* Metric Banner */}
            <div className="grid grid-cols-3 gap-3 p-3 bg-slate-50 dark:bg-slate-800/60 rounded-xl border border-slate-200 dark:border-slate-700">
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-bold">Severity</span>
                <div className="mt-1">
                  <Badge variant={getSeverityBadgeVariant(selectedAlert.severity)} size="sm">
                    {selectedAlert.severity}
                  </Badge>
                </div>
              </div>
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-bold">Amount</span>
                <span className="text-base font-extrabold text-slate-900 dark:text-white table-cell-num">
                  {formatCurrency(selectedAlert.amount, selectedAlert.currency)}
                </span>
              </div>
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-bold">Account</span>
                <span className="font-mono text-xs font-semibold text-slate-800 dark:text-slate-200">{selectedAlert.accountNumber}</span>
              </div>
            </div>

            {/* Reasons / Signals */}
            <div>
              <span className="text-xs font-bold text-slate-900 dark:text-white block mb-2">
                Triggered Risk Indicators & Anomaly Reasons:
              </span>
              <ul className="space-y-2">
                {selectedAlert.reasons.map((reason, idx) => (
                  <li key={idx} className="p-2.5 rounded-lg bg-rose-50/60 dark:bg-rose-950/30 border border-rose-100 dark:border-rose-900/60 text-rose-800 dark:text-rose-200 text-xs flex items-start gap-2">
                    <AlertTriangle className="w-4 h-4 text-rose-600 dark:text-rose-400 shrink-0 mt-0.5" />
                    <span>{reason}</span>
                  </li>
                ))}
              </ul>
            </div>

            {/* Actions */}
            <div className="pt-3 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between">
              <Button size="sm" variant="ghost" onClick={handleAcknowledge}>
                Acknowledge Alert
              </Button>
              <div className="flex gap-2">
                <Button size="sm" variant="outline" onClick={() => setSelectedAlert(null)}>
                  Close
                </Button>
                <Button size="sm" variant="danger" leftIcon={<FileText className="w-3.5 h-3.5" />} onClick={handleFileSar}>
                  File Regulatory SAR
                </Button>
              </div>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};

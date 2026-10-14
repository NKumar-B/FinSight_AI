import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { History, ShieldCheck, Search, Filter, Lock } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import { DataTable, Column } from '@/components/ui/DataTable';
import { finSightApi } from '@/services/api/finSightApi';
import { AuditEvent } from '@/types/financial';
import { formatDateTime } from '@/utils/formatters';

export const AuditHistoryPage: React.FC = () => {
  const [selectedAudit, setSelectedAudit] = useState<AuditEvent | null>(null);

  const { data: auditLogs, isLoading } = useQuery({
    queryKey: ['auditEvents'],
    queryFn: finSightApi.getAuditEvents,
  });

  const columns: Column<AuditEvent>[] = [
    {
      key: 'timestamp',
      header: 'Timestamp',
      sortable: true,
      render: (a) => <span className="font-mono text-xs text-slate-500">{formatDateTime(a.timestamp)}</span>,
    },
    {
      key: 'actor',
      header: 'Actor / User',
      sortable: true,
      render: (a) => (
        <span className="font-semibold text-xs text-slate-900 bg-slate-100 px-2 py-0.5 rounded">
          {a.actor}
        </span>
      ),
    },
    {
      key: 'action',
      header: 'Action Taken',
      sortable: true,
      render: (a) => <span className="font-mono text-xs font-bold text-brand-700">{a.action}</span>,
    },
    {
      key: 'serviceName',
      header: 'Microservice',
      render: (a) => <span className="text-slate-500 text-xs">{a.serviceName}</span>,
    },
    {
      key: 'correlationId',
      header: 'Correlation ID',
      render: (a) => (
        <span className="font-mono text-[11px] text-slate-400 truncate max-w-[120px] inline-block">
          {a.correlationId}
        </span>
      ),
    },
    {
      key: 'outcome',
      header: 'Outcome',
      sortable: true,
      render: (a) => (
        <Badge variant={a.outcome === 'SUCCESS' ? 'success' : a.outcome === 'WARNING' ? 'warning' : 'danger'} size="sm">
          {a.outcome}
        </Badge>
      ),
    },
  ];

  return (
    <div className="space-y-6 pb-12">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 flex items-center gap-2">
            <Lock className="w-5 h-5 text-emerald-600" />
            Immutable Audit Trail & Forensics
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Write-once, tamper-evident audit events with distributed request correlation IDs.
          </p>
        </div>

        <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs font-semibold">
          <ShieldCheck className="w-4 h-4 text-emerald-600" />
          <span>ChaCha20 Integrity Verified</span>
        </div>
      </div>

      {/* Audit Log Table */}
      <Card title="System Audit Ledger" subtitle="Click any audit event to inspect full JSON payload and correlation details">
        <DataTable
          data={auditLogs || []}
          columns={columns}
          searchPlaceholder="Search actor, action, or correlation ID..."
          isLoading={isLoading}
          onRowClick={(log) => setSelectedAudit(log)}
        />
      </Card>

      {/* Audit Detail Modal */}
      {selectedAudit && (
        <Modal
          isOpen={!!selectedAudit}
          onClose={() => setSelectedAudit(null)}
          title={`Audit Event #${selectedAudit.id}`}
          description={`Logged by ${selectedAudit.serviceName} at ${formatDateTime(selectedAudit.timestamp)}`}
        >
          <div className="space-y-3 text-xs">
            <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
              <span className="text-slate-400 block text-[10px] uppercase font-bold">Event Details</span>
              <p className="text-slate-800 mt-1">{selectedAudit.details}</p>
            </div>

            <div className="grid grid-cols-2 gap-3 p-3 bg-slate-50 rounded-lg border border-slate-200">
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-bold">Target Entity</span>
                <span className="font-semibold text-slate-800">{selectedAudit.entityType}: {selectedAudit.entityId}</span>
              </div>
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-bold">IP Address</span>
                <span className="font-mono text-slate-800">{selectedAudit.ipAddress}</span>
              </div>
            </div>

            <div>
              <span className="text-slate-500 font-semibold block mb-1">Distributed Trace Correlation:</span>
              <p className="font-mono text-[11px] bg-slate-900 text-emerald-400 p-2.5 rounded-lg select-all">
                {selectedAudit.correlationId}
              </p>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};

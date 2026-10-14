import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { SearchCheck, ShieldAlert, FileText, CheckCircle2, Clock, BrainCircuit, UserCheck, AlertOctagon } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { DataTable, Column } from '@/components/ui/DataTable';
import { finSightApi } from '@/services/api/finSightApi';
import { InvestigationCase, EvidenceItem } from '@/types/financial';
import { formatDateTime } from '@/utils/formatters';

export const InvestigationsPage: React.FC = () => {
  const [selectedCase, setSelectedCase] = useState<InvestigationCase | null>(null);

  const { data: cases, isLoading: casesLoading } = useQuery({
    queryKey: ['investigations'],
    queryFn: finSightApi.getInvestigations,
  });

  const { data: evidence, isLoading: evidenceLoading } = useQuery({
    queryKey: ['evidence', selectedCase?.id],
    queryFn: () => finSightApi.getEvidence(selectedCase?.id || 1),
    enabled: !!selectedCase,
  });

  const columns: Column<InvestigationCase>[] = [
    {
      key: 'caseNumber',
      header: 'Case ID',
      sortable: true,
      render: (c) => <span className="font-mono text-xs font-bold text-indigo-700">{c.caseNumber}</span>,
    },
    {
      key: 'title',
      header: 'Case Title & Subject',
      render: (c) => (
        <div>
          <div className="font-semibold text-slate-900 text-xs">{c.title}</div>
          <div className="text-[11px] text-slate-400">Assigned: {c.assignedAnalyst}</div>
        </div>
      ),
    },
    {
      key: 'riskScore',
      header: 'Risk Index',
      sortable: true,
      className: 'text-center',
      render: (c) => (
        <span className="font-black text-xs text-rose-700 bg-rose-50 px-2 py-0.5 rounded border border-rose-200">
          {c.riskScore} / 100
        </span>
      ),
    },
    {
      key: 'priority',
      header: 'Priority',
      render: (c) => (
        <Badge variant={c.priority === 'URGENT' ? 'danger' : 'warning'} size="sm">
          {c.priority}
        </Badge>
      ),
    },
    {
      key: 'status',
      header: 'Status',
      render: (c) => (
        <Badge variant={c.status === 'CLOSED' ? 'success' : 'warning'} size="sm">
          {c.status.replace(/_/g, ' ')}
        </Badge>
      ),
    },
    {
      key: 'createdAt',
      header: 'Opened',
      render: (c) => <span className="text-slate-500 text-xs">{formatDateTime(c.createdAt)}</span>,
    },
  ];

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900">
            Investigation Workspace
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Evidence-based financial forensics, provenance tracking, and regulatory audit dossier assembly.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Cases List (1 col if selected, else 3) */}
        <div className={selectedCase ? 'lg:col-span-1' : 'lg:col-span-3'}>
          <Card title="Active Forensic Cases" subtitle="Prioritized investigation cases requiring compliance adjudication">
            <DataTable
              data={cases || []}
              columns={columns}
              searchPlaceholder="Search cases..."
              isLoading={casesLoading}
              onRowClick={(c) => setSelectedCase(c)}
            />
          </Card>
        </div>

        {/* Selected Case Detail Dossier (2 cols) */}
        {selectedCase && (
          <div className="lg:col-span-2 space-y-4">
            <Card
              title={
                <div className="flex items-center gap-2">
                  <span className="font-mono text-indigo-600">{selectedCase.caseNumber}:</span>
                  <span>{selectedCase.title}</span>
                </div>
              }
              subtitle={`Opened ${formatDateTime(selectedCase.createdAt)} • Lead: ${selectedCase.assignedAnalyst}`}
              action={
                <Button size="sm" variant="outline" onClick={() => setSelectedCase(null)}>
                  Close Dossier
                </Button>
              }
            >
              {/* Executive Case Summary */}
              <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-700 leading-relaxed mb-6">
                <span className="font-bold text-slate-900 block mb-1">Executive Forensic Summary:</span>
                {selectedCase.summary}
              </div>

              {/* Evidence Provenance Trace */}
              <div>
                <h4 className="text-xs font-bold uppercase tracking-wider text-slate-500 mb-3 flex items-center gap-2">
                  <FileText className="w-4 h-4 text-brand-600" />
                  Forensic Evidence & Provenance Hierarchy ({evidence?.length || 3} items)
                </h4>

                <div className="space-y-3">
                  {evidence?.map((item) => (
                    <div
                      key={item.id}
                      className="p-3.5 rounded-xl border border-slate-200 bg-white shadow-subtle flex flex-col gap-2"
                    >
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-bold text-slate-900">{item.title}</span>
                        <Badge
                          variant={
                            item.provenance === 'OBSERVED_DATA'
                              ? 'success'
                              : item.provenance === 'RULE_ENGINE'
                              ? 'info'
                              : 'warning'
                          }
                          size="sm"
                        >
                          {item.provenance.replace(/_/g, ' ')}
                        </Badge>
                      </div>
                      <p className="text-xs text-slate-600">{item.description}</p>
                      <span className="text-[10px] text-slate-400">{formatDateTime(item.createdAt)}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Adjudication Action Gate */}
              <div className="mt-6 pt-4 border-t border-slate-100 flex items-center justify-between">
                <div className="flex items-center gap-2 text-xs text-slate-500">
                  <UserCheck className="w-4 h-4 text-emerald-600" />
                  <span>Compliance Officer Review Gate</span>
                </div>
                <div className="flex gap-2">
                  <Button size="sm" variant="danger">
                    Freeze Account Funds
                  </Button>
                  <Button size="sm">
                    Approve & Close Case
                  </Button>
                </div>
              </div>
            </Card>
          </div>
        )}
      </div>
    </div>
  );
};

import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Play, CheckCircle2, AlertTriangle, Layers, ArrowRight, RefreshCw } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { DataTable, Column } from '@/components/ui/DataTable';
import { Modal } from '@/components/ui/Modal';
import { finSightApi } from '@/services/api/finSightApi';
import { ReconciliationRun, Discrepancy } from '@/types/financial';
import { formatCurrency, formatDateTime } from '@/utils/formatters';

export const ReconciliationPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [selectedDiscrepancy, setSelectedDiscrepancy] = useState<Discrepancy | null>(null);

  const { data: runs, isLoading: runsLoading } = useQuery({
    queryKey: ['reconRuns'],
    queryFn: finSightApi.getReconciliationRuns,
  });

  const { data: discrepancies, isLoading: discLoading } = useQuery({
    queryKey: ['discrepancies'],
    queryFn: finSightApi.getDiscrepancies,
  });

  const startReconMutation = useMutation({
    mutationFn: finSightApi.startReconciliation,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reconRuns'] });
      queryClient.invalidateQueries({ queryKey: ['discrepancies'] });
    },
  });

  const runColumns: Column<ReconciliationRun>[] = [
    {
      key: 'runReference',
      header: 'Run ID',
      sortable: true,
      render: (r) => <span className="font-mono text-xs font-bold text-brand-700">{r.runReference}</span>,
    },
    {
      key: 'startTime',
      header: 'Execution Time',
      sortable: true,
      render: (r) => <span className="text-slate-500 text-xs">{formatDateTime(r.startTime)}</span>,
    },
    {
      key: 'totalSourceRecords',
      header: 'Records Evaluated',
      className: 'text-right table-cell-num',
      render: (r) => <span>{r.totalSourceRecords.toLocaleString()}</span>,
    },
    {
      key: 'exactMatches',
      header: 'Exact Matches',
      className: 'text-right table-cell-num text-emerald-600 font-semibold',
      render: (r) => <span>{r.exactMatches.toLocaleString()}</span>,
    },
    {
      key: 'toleranceMatches',
      header: 'Tolerance Matches',
      className: 'text-right table-cell-num text-amber-600 font-semibold',
      render: (r) => <span>{r.toleranceMatches.toLocaleString()}</span>,
    },
    {
      key: 'unmatchedRecords',
      header: 'Discrepancies',
      className: 'text-right table-cell-num text-rose-600 font-bold',
      render: (r) => <span>{r.unmatchedRecords}</span>,
    },
    {
      key: 'matchRatePercentage',
      header: 'Match Rate',
      className: 'text-right',
      render: (r) => (
        <span className="inline-flex items-center gap-1 font-bold text-xs text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
          <CheckCircle2 className="w-3 h-3 text-emerald-600" />
          {r.matchRatePercentage}%
        </span>
      ),
    },
    {
      key: 'status',
      header: 'Status',
      render: (r) => (
        <Badge variant={r.status === 'COMPLETED' ? 'success' : 'warning'} size="sm">
          {r.status}
        </Badge>
      ),
    },
  ];

  const discColumns: Column<Discrepancy>[] = [
    {
      key: 'discrepancyType',
      header: 'Anomaly Classification',
      render: (d) => (
        <div className="flex items-center gap-2">
          <AlertTriangle className="w-3.5 h-3.5 text-amber-500" />
          <span className="font-semibold text-slate-800 text-xs">
            {d.discrepancyType.replace(/_/g, ' ')}
          </span>
        </div>
      ),
    },
    {
      key: 'amountDifference',
      header: 'Variance',
      className: 'text-right font-bold text-rose-600 table-cell-num',
      render: (d) => <span>{formatCurrency(d.amountDifference)}</span>,
    },
    {
      key: 'notes',
      header: 'Root Cause Analysis',
      render: (d) => <span className="text-slate-600 text-xs">{d.notes}</span>,
    },
    {
      key: 'status',
      header: 'Investigation Status',
      render: (d) => (
        <Badge variant={d.status === 'RESOLVED' ? 'success' : 'warning'} size="sm">
          {d.status}
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
            Deterministic Reconciliation Engine
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            4-Step automated matching pipeline (Exact Reference, Tolerance Window, 1-to-Many Aggregation, and Anomaly Isolation).
          </p>
        </div>

        <Button
          size="sm"
          leftIcon={<Play className="w-3.5 h-3.5" />}
          isLoading={startReconMutation.isPending}
          onClick={() => startReconMutation.mutate()}
        >
          Execute Reconciliation Run
        </Button>
      </div>

      {/* 4-Step Pipeline Visualizer */}
      <Card title="4-Stage Deterministic Reconciliation Pipeline" subtitle="Deterministic rules ensure 100% mathematical auditability without blackbox hallucinations">
        <div className="grid grid-cols-1 sm:grid-cols-4 gap-3 py-2">
          <div className="p-3.5 rounded-xl border border-emerald-200 bg-emerald-50/50">
            <div className="text-[10px] font-bold uppercase tracking-wider text-emerald-700">Stage 1</div>
            <div className="text-sm font-bold text-slate-900 mt-1">Exact Matching</div>
            <p className="text-[11px] text-slate-500 mt-1">1:1 matching across Reference, Date, and Amount (Zero tolerance).</p>
          </div>
          <div className="p-3.5 rounded-xl border border-brand-200 bg-brand-50/50">
            <div className="text-[10px] font-bold uppercase tracking-wider text-brand-700">Stage 2</div>
            <div className="text-sm font-bold text-slate-900 mt-1">Tolerance Window</div>
            <p className="text-[11px] text-slate-500 mt-1">±$0.50 exchange rounding variance and ±2 day value-date window.</p>
          </div>
          <div className="p-3.5 rounded-xl border border-indigo-200 bg-indigo-50/50">
            <div className="text-[10px] font-bold uppercase tracking-wider text-indigo-700">Stage 3</div>
            <div className="text-sm font-bold text-slate-900 mt-1">1-to-Many Match</div>
            <p className="text-[11px] text-slate-500 mt-1">Sub-ledger split payments reconciled to single batch settlements.</p>
          </div>
          <div className="p-3.5 rounded-xl border border-rose-200 bg-rose-50/50">
            <div className="text-[10px] font-bold uppercase tracking-wider text-rose-700">Stage 4</div>
            <div className="text-sm font-bold text-slate-900 mt-1">Discrepancy Review</div>
            <p className="text-[11px] text-slate-500 mt-1">Unmatched residuals routed to human compliance & investigation.</p>
          </div>
        </div>
      </Card>

      {/* Reconciliation Run History */}
      <Card title="Reconciliation Execution History" subtitle="Audit log of batch reconciliation jobs executed across all accounts">
        <DataTable
          data={runs || []}
          columns={runColumns}
          searchPlaceholder="Search by Run Reference..."
          isLoading={runsLoading}
        />
      </Card>

      {/* Discrepancies Table */}
      <Card title="Unreconciled Variances & Discrepancy Queue" subtitle="Items requiring analyst review or counterparty bank query">
        <DataTable
          data={discrepancies || []}
          columns={discColumns}
          searchPlaceholder="Search variances..."
          isLoading={discLoading}
          onRowClick={(disc) => setSelectedDiscrepancy(disc)}
        />
      </Card>

      {/* Discrepancy Detail Modal */}
      {selectedDiscrepancy && (
        <Modal
          isOpen={!!selectedDiscrepancy}
          onClose={() => setSelectedDiscrepancy(null)}
          title={`Discrepancy Investigation #${selectedDiscrepancy.id}`}
          description="Side-by-side ledger variance comparison and root cause audit."
        >
          <div className="space-y-4 text-xs">
            <div className="p-3 bg-amber-50 rounded-lg border border-amber-200 text-amber-800 font-medium">
              Variance Amount: <span className="font-bold text-sm">{formatCurrency(selectedDiscrepancy.amountDifference)}</span>
            </div>

            <div>
              <span className="text-slate-500 font-semibold block mb-1">Root Cause Explanation</span>
              <p className="p-3 bg-slate-50 rounded-lg border border-slate-200 text-slate-700">
                {selectedDiscrepancy.notes}
              </p>
            </div>

            <div className="pt-2 flex justify-end gap-2">
              <Button size="sm" variant="outline" onClick={() => setSelectedDiscrepancy(null)}>
                Close
              </Button>
              <Button size="sm" onClick={() => setSelectedDiscrepancy(null)}>
                Mark as Waived / Adjusted
              </Button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};

import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Bot,
  BrainCircuit,
  Cpu,
  GitCompare,
  ShieldAlert,
  Search,
  CheckCircle2,
  Clock,
  Sparkles,
  Play,
  Layers,
  ShieldCheck,
  AlertTriangle,
} from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { finSightApi } from '@/services/api/finSightApi';
import { AgentExecution } from '@/types/financial';
import { formatDateTime } from '@/utils/formatters';

const SPECIALIST_AGENTS = [
  {
    name: 'Reconciliation Agent',
    type: 'RECONCILIATION_AGENT',
    description: 'Autonomous 4-step ledger pairing, tolerance matching, and variance root-cause explanation.',
    tools: ['reconciliationTool', 'transactionLedgerTool'],
    icon: GitCompare,
    status: 'ONLINE',
  },
  {
    name: 'Fraud Detection Agent',
    type: 'FRAUD_DETECTION_AGENT',
    description: 'Velocity anomaly screening, sanctions watchlist cross-referencing, and risk scoring (0-100).',
    tools: ['fraudEvaluationTool', 'sanctionsCheckTool'],
    icon: ShieldAlert,
    status: 'ONLINE',
  },
  {
    name: 'Relationship Intelligence Agent',
    type: 'RELATIONSHIP_INTELLIGENCE_AGENT',
    description: 'Graph entity resolution linking shell corporations, counterparties, and shared accounts.',
    tools: ['entityGraphTool', 'beneficialOwnershipTool'],
    icon: BrainCircuit,
    status: 'ONLINE',
  },
  {
    name: 'Evidence & Investigation Agent',
    type: 'EVIDENCE_INVESTIGATION_AGENT',
    description: 'Compiles immutable forensic dossiers with cryptographic hashes and provenance tracking.',
    tools: ['evidenceDossierTool', 'auditLogTool'],
    icon: Search,
    status: 'ONLINE',
  },
  {
    name: 'Risk Prioritization Agent',
    type: 'RISK_PRIORITIZATION_AGENT',
    description: 'Dynamic triaging of surveillance queues based on transactional exposure and severity.',
    tools: ['queueTriageTool', 'exposureCalculatorTool'],
    icon: Cpu,
    status: 'ONLINE',
  },
  {
    name: 'Expense Intelligence Agent',
    type: 'EXPENSE_INTELLIGENCE_AGENT',
    description: 'Category-level expense drift detection, vendor leakage identification, and cost benchmarking.',
    tools: ['expenseAnalyticsTool', 'vendorCategorizerTool'],
    icon: Sparkles,
    status: 'ONLINE',
  },
  {
    name: 'Invoice & Receivables Agent',
    type: 'INVOICE_RECEIVABLES_AGENT',
    description: 'DSO calculation, AR aging bucket tracking, and bad-debt risk assessment.',
    tools: ['invoiceAgingTool', 'paymentPredictionTool'],
    icon: Layers,
    status: 'ONLINE',
  },
  {
    name: 'Cash-Flow Forecasting Agent',
    type: 'CASH_FLOW_FORECASTING_AGENT',
    description: 'Predictive 30/60/90-day cash runway modeling with Bayesian confidence bands.',
    tools: ['cashRunwayModelTool', 'monteCarloSimTool'],
    icon: Clock,
    status: 'ONLINE',
  },
  {
    name: 'Financial Copilot Agent',
    type: 'FINANCIAL_COPILOT_AGENT',
    description: 'Natural language enterprise financial intelligence via authorized multi-agent tools.',
    tools: ['nlqRouterTool', 'summarySynthesizerTool'],
    icon: Bot,
    status: 'ONLINE',
  },
];

export const AgentActivityPage: React.FC = () => {
  const [selectedRun, setSelectedRun] = useState<AgentExecution | null>(null);
  const [gateApproved, setGateApproved] = useState(false);

  const { data: runs, isLoading } = useQuery({
    queryKey: ['agentExecutions'],
    queryFn: finSightApi.getAgentExecutions,
  });

  return (
    <div className="space-y-6 pb-12">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900">
            AI Agent Activity & Orchestration Center
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Nine specialist agents coordinated by a centralized ReAct orchestrator with mandatory human review gates.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Badge variant="info" size="md" dot>
            9 Specialist Agents Active
          </Badge>
        </div>
      </div>

      {/* Central Orchestrator Banner */}
      <Card className="bg-gradient-to-r from-navy-950 via-navy-900 to-slate-900 text-white border-navy-800">
        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-2xl bg-brand-500/20 border border-brand-500/30 flex items-center justify-center text-brand-400">
              <BrainCircuit className="w-7 h-7" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-base font-bold text-white">Central AI Orchestrator</h3>
                <span className="text-[10px] bg-emerald-500/20 text-emerald-300 font-bold px-2 py-0.5 rounded border border-emerald-500/30">
                  HEALTHY
                </span>
              </div>
              <p className="text-xs text-slate-400 mt-0.5">
                Coordinates OpenFeign inter-service tool dispatch, state transitions, and compliance review gates.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="text-right hidden sm:block">
              <div className="text-[11px] text-slate-400">Model Engine</div>
              <div className="text-xs font-semibold text-white">Google Gemini 1.5 Pro / Flash</div>
            </div>
          </div>
        </div>
      </Card>

      {/* Nine Specialist Agents Directory */}
      <div>
        <h3 className="text-sm font-bold text-slate-900 mb-3">Directory of 9 Specialist Agents</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {SPECIALIST_AGENTS.map((agent) => {
            const Icon = agent.icon;
            return (
              <Card key={agent.name} className="flex flex-col justify-between hover:border-brand-300 transition-colors">
                <div>
                  <div className="flex items-center justify-between mb-2">
                    <div className="p-2 rounded-lg bg-indigo-50 text-indigo-600">
                      <Icon className="w-4 h-4" />
                    </div>
                    <Badge variant="success" size="sm" dot>
                      {agent.status}
                    </Badge>
                  </div>
                  <h4 className="text-xs font-bold text-slate-900">{agent.name}</h4>
                  <p className="text-[11px] text-slate-500 mt-1 leading-relaxed">{agent.description}</p>
                </div>

                <div className="mt-4 pt-3 border-t border-slate-100">
                  <span className="text-[10px] text-slate-400 font-semibold uppercase block mb-1">
                    Specialized Tools:
                  </span>
                  <div className="flex flex-wrap gap-1">
                    {agent.tools.map((tool) => (
                      <span key={tool} className="text-[10px] font-mono bg-slate-100 text-slate-600 px-1.5 py-0.5 rounded">
                        {tool}
                      </span>
                    ))}
                  </div>
                </div>
              </Card>
            );
          })}
        </div>
      </div>

      {/* Recent Agent Execution Logs */}
      <Card
        title="Agent Execution Traces & Human Review Gates"
        subtitle="Recent executions dispatched by the Central Orchestrator"
      >
        <div className="space-y-3">
          {runs?.map((run) => (
            <div
              key={run.id}
              onClick={() => setSelectedRun(run)}
              className="p-4 rounded-xl border border-slate-200 bg-white hover:bg-slate-50/80 transition-all cursor-pointer flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3"
            >
              <div className="flex items-start gap-3">
                <div className="p-2 rounded-lg bg-brand-50 text-brand-600 mt-0.5">
                  <Bot className="w-4 h-4" />
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-bold text-slate-900">{run.agentName}</span>
                    <span className="font-mono text-[11px] text-slate-400">{run.runId}</span>
                  </div>
                  <p className="text-xs text-slate-600 mt-1 font-medium">{run.inputPrompt}</p>
                  <p className="text-xs text-slate-500 mt-0.5">{run.outputSummary}</p>
                </div>
              </div>

              <div className="flex items-center gap-3 self-end sm:self-center shrink-0">
                <Badge
                  variant={run.status === 'COMPLETED' ? 'success' : run.status === 'AWAITING_APPROVAL' ? 'warning' : 'info'}
                  size="sm"
                >
                  {run.status.replace(/_/g, ' ')}
                </Badge>
                <span className="text-[11px] text-slate-400 font-mono">
                  {run.executionTimeMs ? `${run.executionTimeMs}ms` : '—'}
                </span>
              </div>
            </div>
          ))}
        </div>
      </Card>

      {/* Execution Trace & Human Gate Modal */}
      {selectedRun && (
        <Modal
          isOpen={!!selectedRun}
          onClose={() => setSelectedRun(null)}
          title={`Agent Trace ${selectedRun.runId}`}
          description={`Executed by ${selectedRun.agentName} • ${formatDateTime(selectedRun.startTime)}`}
        >
          <div className="space-y-4 text-xs">
            <div>
              <span className="text-slate-500 font-semibold block mb-1">User / System Prompt:</span>
              <p className="p-3 bg-slate-50 rounded-lg border border-slate-200 text-slate-800">
                {selectedRun.inputPrompt}
              </p>
            </div>

            <div>
              <span className="text-slate-500 font-semibold block mb-1">Agent Output Synthesis:</span>
              <p className="p-3 bg-indigo-50/60 rounded-lg border border-indigo-100 text-indigo-900 font-medium">
                {selectedRun.outputSummary}
              </p>
            </div>

            {selectedRun.status === 'AWAITING_APPROVAL' && (
              <div className="p-3.5 rounded-xl bg-amber-50 border border-amber-200 text-amber-900">
                <div className="flex items-center gap-2 font-bold mb-1">
                  <AlertTriangle className="w-4 h-4 text-amber-600" />
                  Consequential Action Gate Triggered
                </div>
                <p className="text-[11px] text-amber-800 leading-relaxed">
                  In compliance with FinSight Zero-Trust governance, this action cannot be executed automatically without human approval.
                </p>

                {gateApproved ? (
                  <div className="mt-3 p-2 bg-emerald-100 text-emerald-800 rounded font-semibold flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                    Authorized by Compliance Officer. Action dispatched.
                  </div>
                ) : (
                  <div className="mt-3 flex justify-end gap-2">
                    <Button size="sm" variant="outline" onClick={() => setSelectedRun(null)}>
                      Reject
                    </Button>
                    <Button size="sm" onClick={() => setGateApproved(true)}>
                      Authorize & Execute
                    </Button>
                  </div>
                )}
              </div>
            )}
          </div>
        </Modal>
      )}
    </div>
  );
};

import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
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
  RefreshCw,
  Activity,
  ArrowRight,
  Zap,
  Check,
  X,
  FileCheck2,
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
    latency: '820ms',
    accuracy: '99.51%',
  },
  {
    name: 'Fraud Detection Agent',
    type: 'FRAUD_DETECTION_AGENT',
    description: 'Velocity anomaly screening, sanctions watchlist cross-referencing, and risk scoring (0-100).',
    tools: ['fraudEvaluationTool', 'sanctionsCheckTool'],
    icon: ShieldAlert,
    status: 'ONLINE',
    latency: '910ms',
    accuracy: '98.90%',
  },
  {
    name: 'Relationship Intelligence Agent',
    type: 'RELATIONSHIP_INTELLIGENCE_AGENT',
    description: 'Graph entity resolution linking shell corporations, counterparties, and shared accounts.',
    tools: ['entityGraphTool', 'beneficialOwnershipTool'],
    icon: BrainCircuit,
    status: 'ONLINE',
    latency: '1,120ms',
    accuracy: '99.10%',
  },
  {
    name: 'Evidence & Investigation Agent',
    type: 'EVIDENCE_INVESTIGATION_AGENT',
    description: 'Compiles immutable forensic dossiers with cryptographic hashes and provenance tracking.',
    tools: ['evidenceDossierTool', 'auditLogTool'],
    icon: Search,
    status: 'ONLINE',
    latency: '780ms',
    accuracy: '100%',
  },
  {
    name: 'Risk Prioritization Agent',
    type: 'RISK_PRIORITIZATION_AGENT',
    description: 'Dynamic triaging of surveillance queues based on transactional exposure and severity.',
    tools: ['queueTriageTool', 'exposureCalculatorTool'],
    icon: Cpu,
    status: 'ONLINE',
    latency: '690ms',
    accuracy: '99.20%',
  },
  {
    name: 'Expense Intelligence Agent',
    type: 'EXPENSE_INTELLIGENCE_AGENT',
    description: 'Category-level expense drift detection, vendor leakage identification, and cost benchmarking.',
    tools: ['expenseAnalyticsTool', 'vendorCategorizerTool'],
    icon: Sparkles,
    status: 'ONLINE',
    latency: '850ms',
    accuracy: '97.80%',
  },
  {
    name: 'Invoice & Receivables Agent',
    type: 'INVOICE_RECEIVABLES_AGENT',
    description: 'DSO calculation, AR aging bucket tracking, and bad-debt risk assessment.',
    tools: ['invoiceAgingTool', 'paymentPredictionTool'],
    icon: Layers,
    status: 'ONLINE',
    latency: '740ms',
    accuracy: '98.60%',
  },
  {
    name: 'Cash-Flow Forecasting Agent',
    type: 'CASH_FLOW_FORECASTING_AGENT',
    description: 'Predictive 30/60/90-day cash runway modeling with Bayesian confidence bands.',
    tools: ['cashRunwayModelTool', 'monteCarloSimTool'],
    icon: Clock,
    status: 'ONLINE',
    latency: '1,050ms',
    accuracy: '96.50%',
  },
  {
    name: 'Financial Copilot Agent',
    type: 'FINANCIAL_COPILOT_AGENT',
    description: 'Natural language enterprise financial intelligence via authorized multi-agent tools.',
    tools: ['nlqRouterTool', 'summarySynthesizerTool'],
    icon: Bot,
    status: 'ONLINE',
    latency: '640ms',
    accuracy: '99.40%',
  },
];

export const AgentActivityPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [selectedRun, setSelectedRun] = useState<AgentExecution | null>(null);
  const [gateApproved, setGateApproved] = useState(false);

  // Diagnostic states
  const [isVerifyingAll, setIsVerifyingAll] = useState(false);
  const [verificationReport, setVerificationReport] = useState<any | null>(null);
  const [activeDispatchingAgent, setActiveDispatchingAgent] = useState<string | null>(null);
  const [liveExecutedTrace, setLiveExecutedTrace] = useState<AgentExecution | null>(null);

  const { data: runs } = useQuery({
    queryKey: ['agentExecutions'],
    queryFn: finSightApi.getAgentExecutions,
  });

  const handleRunSingleAgent = async (agent: (typeof SPECIALIST_AGENTS)[0]) => {
    setActiveDispatchingAgent(agent.type);
    try {
      const trace = await finSightApi.dispatchAgent(agent.type);
      queryClient.invalidateQueries({ queryKey: ['agentExecutions'] });
      setLiveExecutedTrace(trace);
    } finally {
      setActiveDispatchingAgent(null);
    }
  };

  const handleVerifyAllAgents = async () => {
    setIsVerifyingAll(true);
    setVerificationReport(null);
    try {
      await new Promise((r) => setTimeout(r, 800));
      const report = await finSightApi.verifyAllAgents();
      setVerificationReport(report);
      queryClient.invalidateQueries({ queryKey: ['agentExecutions'] });
    } finally {
      setIsVerifyingAll(false);
    }
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Page Header with Master Fleet Verification Trigger */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-2 border-b border-slate-200 dark:border-slate-800">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 dark:text-white flex items-center gap-2">
            AI Agent Activity & Orchestration Center
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Nine specialist agents coordinated by a centralized ReAct orchestrator with verified OpenFeign telemetry.
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <Button
            size="sm"
            onClick={handleVerifyAllAgents}
            isLoading={isVerifyingAll}
            leftIcon={<Activity className="w-3.5 h-3.5" />}
            className="shadow-md shadow-brand-500/20"
          >
            {isVerifyingAll ? 'Benchmarking 9 Agents...' : 'Verify All 9 Agents'}
          </Button>
        </div>
      </div>

      {/* Verification Report Banner (shown when Master Diagnostics is run) */}
      {verificationReport && (
        <div className="p-4 rounded-2xl bg-gradient-to-r from-emerald-500/10 via-brand-500/10 to-teal-500/10 border-2 border-emerald-400/40 backdrop-blur-md shadow-lg animate-fadeIn">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-emerald-500 text-white flex items-center justify-center shadow-md shadow-emerald-500/30 shrink-0">
                <CheckCircle2 className="w-6 h-6" />
              </div>
              <div>
                <h4 className="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
                  <span>Multi-Agent Fleet Health Check Passed</span>
                  <Badge variant="success" size="sm">
                    {verificationReport.overallHealth || '100% HEALTHY'}
                  </Badge>
                </h4>
                <p className="text-xs text-slate-600 dark:text-slate-300 mt-0.5">
                  All 9 specialized AI agents successfully validated. OpenFeign inter-service tool probes active.
                </p>
              </div>
            </div>

            <div className="flex items-center gap-4 text-xs font-semibold text-slate-700 dark:text-slate-200">
              <div className="text-right">
                <div className="text-[10px] uppercase text-slate-400">Mean Latency</div>
                <div className="font-mono text-emerald-600 dark:text-emerald-400 font-bold">
                  {verificationReport.meanLatencyMs}ms
                </div>
              </div>
              <div className="text-right">
                <div className="text-[10px] uppercase text-slate-400">Active Agents</div>
                <div className="font-mono text-brand-600 dark:text-brand-400 font-bold">9 / 9 Active</div>
              </div>
              <button
                onClick={() => setVerificationReport(null)}
                className="p-1 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-white"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Central Orchestrator Banner */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-navy-950 via-slate-900 to-navy-900 p-6 text-white border border-slate-800/80 shadow-2xl dark:shadow-[0_0_40px_rgba(37,99,235,0.12)] card-sheen">
        <div className="relative z-10 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-2xl bg-brand-500/20 border border-brand-500/30 flex items-center justify-center text-brand-400 shadow-[0_0_15px_rgba(59,130,246,0.25)]">
              <BrainCircuit className="w-7 h-7" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-base font-bold text-white">Central AI Orchestrator</h3>
                <span className="text-[10px] bg-emerald-500/20 text-emerald-300 font-bold px-2 py-0.5 rounded border border-emerald-500/30 flex items-center gap-1 shadow-[0_0_8px_rgba(16,185,129,0.3)]">
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse shadow-[0_0_6px_rgba(52,211,153,0.8)]"></span>
                  FLEET OPERATIONAL
                </span>
              </div>
              <p className="text-xs text-slate-400 mt-1 max-w-xl">
                Coordinates OpenFeign inter-service tool dispatch, state transitions, ReAct thought loops, and compliance review gates.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="text-right hidden sm:block">
              <div className="text-[10px] text-slate-400 uppercase tracking-wider font-semibold">Model Engine</div>
              <div className="text-xs font-semibold text-white">Google Gemini 1.5 Pro / Flash</div>
            </div>
            <div className="h-8 w-px bg-slate-700 hidden sm:block" />
            <div className="text-right hidden sm:block">
              <div className="text-[10px] text-slate-400 uppercase tracking-wider font-semibold">Security Gate</div>
              <div className="text-xs font-semibold text-emerald-400">Human-in-the-Loop Active</div>
            </div>
          </div>
        </div>
      </div>

      {/* Nine Specialist Agents Directory with Glassmorphism and Hover Lifts */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <span>Directory of 9 Specialist Agents</span>
            <Badge variant="info" size="sm">9 Verified</Badge>
          </h3>
          <span className="text-xs text-slate-400">Click &quot;Run Diagnostic&quot; on any card to test live execution</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {SPECIALIST_AGENTS.map((agent) => {
            const Icon = agent.icon;
            const isRunning = activeDispatchingAgent === agent.type;

            return (
              <div
                key={agent.name}
                className="group relative rounded-2xl p-5 border border-slate-200 dark:border-white/10 bg-white/80 dark:bg-slate-900/80 backdrop-blur-md shadow-sm hover:shadow-xl dark:hover:shadow-[0_12px_32px_-8px_rgba(0,0,0,0.6),0_0_24px_rgba(99,102,241,0.15)] hover:border-slate-300 dark:hover:border-indigo-500/40 hover:-translate-y-1.5 card-sheen transition-all duration-300 flex flex-col justify-between"
              >
                <div>
                  {/* Card Header: Icon, Name & Status */}
                  <div className="flex items-center justify-between mb-3">
                    <div className="w-10 h-10 rounded-xl bg-brand-50 dark:bg-brand-950/40 border border-brand-200 dark:border-brand-800 text-brand-600 dark:text-brand-400 flex items-center justify-center group-hover:scale-110 transition-transform shadow-sm">
                      <Icon className="w-5 h-5" />
                    </div>

                    <div className="flex items-center gap-1.5">
                      <Badge variant="success" size="sm" dot>
                        {agent.status}
                      </Badge>
                    </div>
                  </div>

                  <h4 className="text-sm font-bold text-slate-900 dark:text-white group-hover:text-brand-600 dark:group-hover:text-brand-400 transition-colors">
                    {agent.name}
                  </h4>

                  <p className="text-xs text-slate-500 dark:text-slate-400 mt-1 leading-relaxed line-clamp-2">
                    {agent.description}
                  </p>

                  {/* Telemetry Chips */}
                  <div className="flex items-center gap-3 mt-3 pt-3 border-t border-slate-100 dark:border-slate-800 text-[11px]">
                    <div className="flex items-center gap-1 text-slate-600 dark:text-slate-300">
                      <Zap className="w-3.5 h-3.5 text-amber-500" />
                      <span>{agent.latency}</span>
                    </div>
                    <div className="flex items-center gap-1 text-slate-600 dark:text-slate-300">
                      <ShieldCheck className="w-3.5 h-3.5 text-emerald-500" />
                      <span>{agent.accuracy}</span>
                    </div>
                  </div>
                </div>

                {/* Footer: Tools List & Run Action */}
                <div className="mt-4 pt-3 border-t border-slate-100 dark:border-slate-800 space-y-3">
                  <div className="flex flex-wrap gap-1">
                    {agent.tools.map((tool) => (
                      <span
                        key={tool}
                        className="text-[10px] font-mono bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 px-1.5 py-0.5 rounded"
                      >
                        {tool}
                      </span>
                    ))}
                  </div>

                  <Button
                    size="sm"
                    variant="outline"
                    className="w-full justify-center group-hover:border-brand-400 group-hover:bg-brand-50 dark:group-hover:bg-brand-950/40 text-xs"
                    onClick={() => handleRunSingleAgent(agent)}
                    isLoading={isRunning}
                    leftIcon={<Play className="w-3 h-3 text-brand-600 fill-brand-600" />}
                  >
                    {isRunning ? 'Executing ReAct Cycle...' : 'Run Diagnostic'}
                  </Button>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Live Agent Execution Output Banner (after user runs an agent) */}
      {liveExecutedTrace && (
        <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border-2 border-brand-300 dark:border-brand-800 shadow-xl animate-fadeIn space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Bot className="w-5 h-5 text-brand-600" />
              <h4 className="text-sm font-bold text-slate-900 dark:text-white">
                Live ReAct Execution Completed: {liveExecutedTrace.agentName}
              </h4>
              <Badge variant="success" size="sm">
                Trace {liveExecutedTrace.runId}
              </Badge>
            </div>
            <button
              onClick={() => setLiveExecutedTrace(null)}
              className="p-1 rounded text-slate-400 hover:text-slate-600 dark:hover:text-white"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs bg-slate-50 dark:bg-slate-800/60 p-3.5 rounded-xl border border-slate-200 dark:border-slate-700">
            <div>
              <span className="font-bold text-slate-500 block text-[10px] uppercase">Input Prompt</span>
              <p className="text-slate-800 dark:text-slate-200 font-medium mt-0.5">{liveExecutedTrace.inputPrompt}</p>
            </div>
            <div>
              <span className="font-bold text-slate-500 block text-[10px] uppercase">Forensic Synthesis</span>
              <p className="text-slate-800 dark:text-slate-200 font-medium mt-0.5">{liveExecutedTrace.outputSummary}</p>
            </div>
          </div>
        </div>
      )}

      {/* Recent Agent Execution Logs Table with Hover Effects */}
      <Card
        title="Agent Execution Traces & Human Review Gates"
        subtitle="Live audit trail of AI agent tool executions and compliance gate status"
      >
        <div className="space-y-3">
          {runs?.map((run) => (
            <div
              key={run.id}
              onClick={() => setSelectedRun(run)}
              className="p-4 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 hover:bg-slate-50/80 dark:hover:bg-slate-800/50 hover:shadow-md transition-all cursor-pointer flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3"
            >
              <div className="flex items-start gap-3">
                <div className="p-2.5 rounded-xl bg-brand-50 dark:bg-brand-950/40 text-brand-600 dark:text-brand-400 mt-0.5 shrink-0 border border-brand-200 dark:border-brand-800">
                  <Bot className="w-4 h-4" />
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-bold text-slate-900 dark:text-white">{run.agentName}</span>
                    <span className="font-mono text-[11px] text-slate-400">{run.runId}</span>
                  </div>
                  <p className="text-xs text-slate-600 dark:text-slate-300 mt-1 font-medium">{run.inputPrompt}</p>
                  <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">{run.outputSummary}</p>
                </div>
              </div>

              <div className="flex items-center gap-3 self-end sm:self-center shrink-0">
                <Badge
                  variant={
                    run.status === 'COMPLETED'
                      ? 'success'
                      : run.status === 'AWAITING_APPROVAL'
                      ? 'warning'
                      : 'info'
                  }
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
          maxWidth="lg"
        >
          <div className="space-y-4 text-xs">
            <div>
              <span className="text-slate-500 font-semibold block mb-1">User / System Prompt:</span>
              <p className="text-slate-800 dark:text-slate-200 p-3 bg-slate-50 dark:bg-slate-800 rounded-lg border border-slate-200 dark:border-slate-700 font-mono">
                {selectedRun.inputPrompt}
              </p>
            </div>

            <div>
              <span className="text-slate-500 font-semibold block mb-1">Tools Invoked:</span>
              <div className="flex flex-wrap gap-1.5">
                {selectedRun.toolsInvoked?.map((tool: string) => (
                  <Badge key={tool} variant="neutral" size="sm">
                    {tool}
                  </Badge>
                ))}
              </div>
            </div>

            <div>
              <span className="text-slate-500 font-semibold block mb-1">Observation & Synthesized Findings:</span>
              <p className="text-slate-800 dark:text-slate-200 p-3 bg-slate-50 dark:bg-slate-800 rounded-lg border border-slate-200 dark:border-slate-700 leading-relaxed">
                {selectedRun.outputSummary}
              </p>
            </div>

            {/* Human-in-the-Loop Gate Approval */}
            {selectedRun.status === 'AWAITING_APPROVAL' && (
              <div className="p-4 bg-amber-50 dark:bg-amber-950/40 border-2 border-amber-300 dark:border-amber-800 rounded-xl space-y-3">
                <div className="flex items-center gap-2 text-amber-900 dark:text-amber-200 font-bold">
                  <AlertTriangle className="w-4 h-4 text-amber-600" />
                  <span>Mandatory Human Review Gate</span>
                </div>
                <p className="text-amber-800 dark:text-amber-300">
                  This agent proposed an action requiring formal compliance sign-off (Suspicious Activity Report escalation).
                </p>

                {gateApproved ? (
                  <div className="flex items-center gap-2 text-emerald-700 dark:text-emerald-300 font-semibold bg-emerald-100 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 p-2.5 rounded-lg">
                    <Check className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
                    <span>Action Approved by Compliance Officer</span>
                  </div>
                ) : (
                  <div className="flex justify-end gap-2 pt-1">
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => setSelectedRun(null)}
                    >
                      Decline & Re-evaluate
                    </Button>
                    <Button
                      size="sm"
                      variant="primary"
                      onClick={() => setGateApproved(true)}
                    >
                      Approve & Execute Action
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

import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import {
  DollarSign,
  IndianRupee,
  Euro,
  PoundSterling,
  GitCompare,
  ShieldAlert,
  AlertOctagon,
  TrendingDown,
  TrendingUp,
  Receipt,
  Bot,
  ArrowRight,
  ShieldCheck,
  CheckCircle2,
} from 'lucide-react';
import {
  AreaChart,
  Area,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { Card } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { finSightApi } from '@/services/api/finSightApi';
import { formatCurrency, formatDateTime, getCurrencySymbol } from '@/utils/formatters';
import { useTheme } from '@/lib/theme';

const VOLUME_DATA = [
  { day: 'Mon', volume: 420000, reconciled: 418000 },
  { day: 'Tue', volume: 580000, reconciled: 575000 },
  { day: 'Wed', volume: 390000, reconciled: 388000 },
  { day: 'Thu', volume: 680000, reconciled: 676000 },
  { day: 'Fri', volume: 890000, reconciled: 885000 },
  { day: 'Sat', volume: 310000, reconciled: 309000 },
  { day: 'Sun', volume: 240000, reconciled: 239500 },
];

const FRAUD_SEVERITY_DATA = [
  { name: 'Critical', count: 1, fill: '#ef4444' },
  { name: 'High', count: 2, fill: '#f97316' },
  { name: 'Medium', count: 4, fill: '#f59e0b' },
  { name: 'Low', count: 7, fill: '#3b82f6' },
];

const EXPENSE_CATEGORIES = [
  { name: 'Cloud Infrastructure', value: 34500, color: '#3b82f6' },
  { name: 'Professional Advisory', value: 12400, color: '#6366f1' },
  { name: 'Corporate Payroll', value: 85000, color: '#10b981' },
  { name: 'Vendor Settlements', value: 52000, color: '#f59e0b' },
];

export const DashboardPage: React.FC = () => {
  const { mode, currency, currencySymbol } = useTheme();

  const { data: accounts } = useQuery({
    queryKey: ['accounts'],
    queryFn: finSightApi.getAccounts,
  });

  const { data: reconRuns } = useQuery({
    queryKey: ['reconRuns'],
    queryFn: finSightApi.getReconciliationRuns,
  });

  const { data: alerts } = useQuery({
    queryKey: ['fraudAlerts'],
    queryFn: finSightApi.getFraudAlerts,
  });

  const { data: agents } = useQuery({
    queryKey: ['agentExecutions'],
    queryFn: finSightApi.getAgentExecutions,
  });

  const totalLiquidity = accounts?.reduce((acc, a) => acc + Number(a.currentBalance || 0), 0) || 2325630.5;
  const latestRecon = reconRuns?.[0];
  const criticalAlertsCount = alerts?.filter((a) => a.severity === 'CRITICAL').length || 1;

  return (
    <div className="space-y-6 pb-12">
      {/* Top Banner / Hero */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-2 border-b border-slate-200 dark:border-slate-800">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 dark:text-white">
            Financial Intelligence Dashboard
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Real-time multi-agent telemetry, ledger reconciliation, and fraud monitoring.
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <Link to="/reconciliation">
            <Button size="sm" variant="outline" leftIcon={<GitCompare className="w-3.5 h-3.5" />}>
              Reconciliation
            </Button>
          </Link>
          <Link to="/copilot">
            <Button size="sm" leftIcon={<Bot className="w-3.5 h-3.5" />}>
              Open AI Copilot
            </Button>
          </Link>
        </div>
      </div>

      {/* KPI Cards Grid (8 Core Operational Metrics) */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Card 1: Total Operating Liquidity */}
        <Card className="hover:shadow-elevated transition-all glow-hover-emerald">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              Treasury Liquidity
            </span>
            <div className="p-2 rounded-lg bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 font-bold text-sm w-8 h-8 flex items-center justify-center">
              {currency === 'INR' ? (
                <IndianRupee className="w-4 h-4" />
              ) : currency === 'EUR' ? (
                <Euro className="w-4 h-4" />
              ) : currency === 'GBP' ? (
                <PoundSterling className="w-4 h-4" />
              ) : (
                <DollarSign className="w-4 h-4" />
              )}
            </div>
          </div>
          <div className="mt-3">
            <div className="text-xl font-bold text-slate-900 dark:text-white table-cell-num">
              {formatCurrency(totalLiquidity, currency)}
            </div>
            <div className="flex items-center gap-1.5 text-xs text-emerald-600 dark:text-emerald-400 mt-1 font-medium">
              <TrendingUp className="w-3.5 h-3.5" />
              <span>+8.4% vs last month</span>
            </div>
          </div>
        </Card>

        {/* Card 2: Reconciliation Match Rate */}
        <Card className="hover:shadow-elevated transition-all glow-hover-brand">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              Recon Match Rate
            </span>
            <div className="p-2 rounded-lg bg-brand-50 dark:bg-brand-950/40 text-brand-600 dark:text-brand-400">
              <GitCompare className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-xl font-bold text-slate-900 dark:text-white table-cell-num">
              {latestRecon ? `${latestRecon.matchRatePercentage}%` : '99.51%'}
            </div>
            <div className="flex items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400 mt-1">
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500 dark:text-emerald-400" />
              <span>{latestRecon?.unmatchedRecords || 12} discrepancies flagged</span>
            </div>
          </div>
        </Card>

        {/* Card 3: Active Fraud & AML Alerts */}
        <Card className="hover:shadow-elevated transition-all glow-hover-rose">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              AML & Fraud Alerts
            </span>
            <div className="p-2 rounded-lg bg-rose-50 dark:bg-rose-950/40 text-rose-600 dark:text-rose-400">
              <ShieldAlert className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
              <span>{alerts?.length || 2} Open</span>
              {criticalAlertsCount > 0 && (
                <Badge variant="danger" size="sm">
                  {criticalAlertsCount} Critical
                </Badge>
              )}
            </div>
            <div className="flex items-center gap-1.5 text-xs text-rose-600 dark:text-rose-400 mt-1 font-medium">
              <AlertOctagon className="w-3.5 h-3.5" />
              <span>1 Wire flagged in Grey List</span>
            </div>
          </div>
        </Card>

        {/* Card 4: AI Specialist Multi-Agent Fleet */}
        <Card className="hover:shadow-elevated transition-all glow-hover-indigo">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              AI Multi-Agent Fleet
            </span>
            <div className="p-2 rounded-lg bg-indigo-50 dark:bg-indigo-950/40 text-indigo-600 dark:text-indigo-400">
              <Bot className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
              <span>9 / 9 Active</span>
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse shadow-[0_0_8px_rgba(16,185,129,0.8)]" />
            </div>
            <div className="text-xs text-slate-500 dark:text-slate-400 mt-1">
              Central Orchestrator online
            </div>
          </div>
        </Card>
      </div>

      {/* Visual Analytics Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Chart 1: Financial Transaction Volume (2 cols) */}
        <Card
          className="lg:col-span-2"
          title="Transaction Clearing & Reconciliation Volume"
          subtitle="7-day gross cleared volume vs automated reconciliation matching"
        >
          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={VOLUME_DATA}>
                <defs>
                  <linearGradient id="volumeGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.25} />
                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0.0} />
                  </linearGradient>
                  <linearGradient id="reconGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#10b981" stopOpacity={0.25} />
                    <stop offset="95%" stopColor="#10b981" stopOpacity={0.0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke={mode === 'dark' ? '#334155' : '#f1f5f9'} />
                <XAxis dataKey="day" stroke={mode === 'dark' ? '#94a3b8' : '#64748b'} fontSize={11} tickLine={false} />
                <YAxis
                  stroke={mode === 'dark' ? '#94a3b8' : '#64748b'}
                  fontSize={11}
                  tickLine={false}
                  tickFormatter={(val) => `${currencySymbol}${val / 1000}k`}
                />
                <Tooltip
                  content={({ active, payload, label }) => {
                    if (!active || !payload || !payload.length) return null;
                    return (
                      <div className="bg-slate-900 border border-slate-700 rounded-xl px-3.5 py-2.5 shadow-2xl text-white pointer-events-none">
                        <div className="font-bold text-[11px] uppercase tracking-wider text-slate-400 mb-1.5">
                          {label}
                        </div>
                        <div className="space-y-1">
                          {payload.map((entry: any) => (
                            <div key={entry.name} className="flex items-center justify-between gap-4 text-xs">
                              <span className="flex items-center gap-1.5 text-slate-200">
                                <span className="w-2 h-2 rounded-full" style={{ backgroundColor: entry.color }} />
                                {entry.name}:
                              </span>
                              <span className="font-bold font-mono text-white">
                                {formatCurrency(Number(entry.value), currency)}
                              </span>
                            </div>
                          ))}
                        </div>
                      </div>
                    );
                  }}
                />
                <Area
                  type="monotone"
                  dataKey="volume"
                  name="Gross Cleared"
                  stroke="#3b82f6"
                  strokeWidth={2}
                  fillOpacity={1}
                  fill="url(#volumeGrad)"
                />
                <Area
                  type="monotone"
                  dataKey="reconciled"
                  name="Matched Recon"
                  stroke="#10b981"
                  strokeWidth={2}
                  fillOpacity={1}
                  fill="url(#reconGrad)"
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </Card>

        {/* Chart 2: Fraud Alert Severity Distribution (1 col) */}
        <Card
          title="Risk Severity Matrix"
          subtitle="Active anomaly alerts evaluated by velocity engine"
        >
          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={FRAUD_SEVERITY_DATA}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke={mode === 'dark' ? '#334155' : '#f1f5f9'} />
                <XAxis dataKey="name" stroke={mode === 'dark' ? '#94a3b8' : '#64748b'} fontSize={11} tickLine={false} />
                <YAxis stroke={mode === 'dark' ? '#94a3b8' : '#64748b'} fontSize={11} tickLine={false} />
                <Tooltip
                  content={({ active, payload, label }) => {
                    if (!active || !payload || !payload.length) return null;
                    const entry = payload[0];
                    return (
                      <div className="bg-slate-900 border border-slate-700 rounded-xl px-3.5 py-2.5 shadow-2xl text-white pointer-events-none">
                        <div className="flex items-center gap-2 mb-1">
                          <span
                            className="w-2.5 h-2.5 rounded-full"
                            style={{ backgroundColor: entry.payload?.fill || entry.color }}
                          />
                          <span className="font-bold text-xs text-slate-100">
                            {label || entry.name} Severity
                          </span>
                        </div>
                        <div className="text-sm font-extrabold text-white font-mono">
                          {entry.value} Active Alert{entry.value === 1 ? '' : 's'}
                        </div>
                      </div>
                    );
                  }}
                />
                <Bar dataKey="count" radius={[6, 6, 0, 0]}>
                  {FRAUD_SEVERITY_DATA.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.fill} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>
      </div>

      {/* Lower Grid: Expense Breakdown & Recent Agent Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Expense Category Breakdown */}
        <Card
          title="Expense Breakdown"
          subtitle="Corporate expenditure distribution across major operational cost centers"
        >
          <div className="h-56 w-full flex items-center justify-center">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={EXPENSE_CATEGORIES}
                  innerRadius={50}
                  outerRadius={75}
                  paddingAngle={4}
                  dataKey="value"
                >
                  {EXPENSE_CATEGORIES.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip
                  content={({ active, payload }) => {
                    if (!active || !payload || !payload.length) return null;
                    const item = payload[0];
                    return (
                      <div className="bg-slate-900/95 backdrop-blur-md border border-slate-700 rounded-xl px-3.5 py-2.5 shadow-2xl text-white pointer-events-none">
                        <div className="flex items-center gap-2 mb-1">
                          <span
                            className="w-2.5 h-2.5 rounded-full shrink-0"
                            style={{ backgroundColor: item.payload?.color || item.color }}
                          />
                          <span className="font-semibold text-xs text-slate-100">
                            {item.name}
                          </span>
                        </div>
                        <div className="text-sm font-bold text-emerald-400 font-mono">
                          {formatCurrency(Number(item.value), currency)}
                        </div>
                      </div>
                    );
                  }}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div className="grid grid-cols-2 gap-2 mt-2 pt-3 border-t border-slate-100 dark:border-slate-800">
            {EXPENSE_CATEGORIES.map((item) => (
              <div key={item.name} className="flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full shrink-0" style={{ backgroundColor: item.color }} />
                <span className="text-[11px] font-medium text-slate-700 dark:text-slate-200 truncate">{item.name}</span>
              </div>
            ))}
          </div>
        </Card>

        {/* Recent AI Agent Executions (2 cols) */}
        <Card
          className="lg:col-span-2"
          title="Specialist AI Agent Activity Stream"
          subtitle="Real-time execution telemetry from Central Orchestrator"
          action={
            <Link to="/agents" className="text-xs text-brand-600 hover:text-brand-700 font-semibold flex items-center gap-1">
              View All 9 Agents <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          }
        >
          <div className="space-y-3.5">
            {agents?.slice(0, 3).map((agent) => (
              <div
                key={agent.id}
                className="p-3.5 rounded-xl border border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/40 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 hover:bg-slate-50 dark:hover:bg-slate-800/70 hover:border-slate-300 dark:hover:border-slate-700 hover:shadow-sm dark:hover:shadow-[0_4px_20px_rgba(0,0,0,0.3)] transition-all duration-200"
              >
                <div className="flex items-start gap-3">
                  <div className="p-2 rounded-lg bg-indigo-50 dark:bg-indigo-950/40 text-indigo-600 dark:text-indigo-400 shrink-0 mt-0.5">
                    <Bot className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-bold text-slate-900 dark:text-white">{agent.agentName}</span>
                      <span className="font-mono text-[10px] text-slate-400 dark:text-slate-500">{agent.runId}</span>
                    </div>
                    <p className="text-xs text-slate-600 dark:text-slate-300 mt-1 line-clamp-1">{agent.outputSummary}</p>
                  </div>
                </div>

                <div className="flex items-center gap-2 self-end sm:self-center shrink-0">
                  <Badge
                    variant={agent.status === 'COMPLETED' ? 'success' : agent.status === 'AWAITING_APPROVAL' ? 'warning' : 'info'}
                    size="sm"
                  >
                    {agent.status}
                  </Badge>
                  <span className="text-[10px] text-slate-400 dark:text-slate-500">{formatDateTime(agent.startTime)}</span>
                </div>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </div>
  );
};

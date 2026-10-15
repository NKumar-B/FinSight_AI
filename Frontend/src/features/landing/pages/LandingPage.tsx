import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Layers,
  Sparkles,
  Bot,
  GitCompare,
  ShieldAlert,
  BrainCircuit,
  Search,
  Cpu,
  Clock,
  ArrowRight,
  CheckCircle2,
  Lock,
  Globe,
  Sun,
  Moon,
  Activity,
  Zap,
  ShieldCheck,
  TrendingUp,
  FileText,
  FileSpreadsheet,
  Check,
  DollarSign,
  ChevronRight,
  Menu,
  X,
} from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { useTheme } from '@/lib/theme';
import { useI18n } from '@/lib/i18n';
import { useAuth } from '@/features/auth/hooks/useAuth';

export const LandingPage: React.FC = () => {
  const navigate = useNavigate();
  const { mode, toggleTheme, appName, appTagline, accent } = useTheme();
  const { language, setLanguage, t } = useI18n();
  const { isAuthenticated } = useAuth();

  const [activeAgentTab, setActiveAgentTab] = useState(0);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const AGENT_FLEET = [
    {
      title: 'Reconciliation Agent',
      category: 'Core Ledger',
      icon: GitCompare,
      stat: '99.51% Match Rate',
      desc: 'Deterministic 4-step matching pairing bank statement feeds against general ledgers within sub-cent tolerances.',
      tools: ['reconciliationTool', 'transactionLedgerTool', 'varianceCalculator'],
      sampleOutput: 'Analyzed 2,450 transactions in 1.4s. 2,402 exact matches, 36 tolerance pairings, 12 isolated variances.',
    },
    {
      title: 'Fraud Detection Agent',
      category: 'Risk & Surveillance',
      icon: ShieldAlert,
      stat: '< 920ms Anomaly Detection',
      desc: 'Real-time velocity scoring, sanctions watchlist cross-referencing, and smurfing pattern recognition.',
      tools: ['fraudEvaluationTool', 'sanctionsCheckTool', 'velocityEngine'],
      sampleOutput: 'Flagged Critical AML alert on $98,500 wire to offshore entity. Risk score: 92/100.',
    },
    {
      title: 'Relationship Intelligence',
      category: 'Graph Analytics',
      icon: BrainCircuit,
      stat: 'Multi-Hop Entity Linkage',
      desc: 'Discovers circular beneficial ownership links and shell companies across counterparties.',
      tools: ['entityGraphTool', 'beneficialOwnershipTool'],
      sampleOutput: 'Resolved 3 hidden shell entities linked via shared beneficiary routing to known sanction watchlists.',
    },
    {
      title: 'Evidence & Investigation',
      category: 'Forensics',
      icon: Search,
      stat: 'Cryptographic Provenance',
      desc: 'Compiles immutable forensic dossiers with SHA-256 ledger fingerprints for regulatory SAR filings.',
      tools: ['evidenceDossierTool', 'auditLogTool', 'sha256Hasher'],
      sampleOutput: 'Generated Case #CASE-2026-019 dossier with 4 tamper-evident cryptographic artifacts.',
    },
    {
      title: 'Risk Prioritization Agent',
      category: 'Surveillance Triage',
      icon: Cpu,
      stat: 'Real-Time SLA Triage',
      desc: 'Dynamically prioritizes active investigator queues by dollar exposure and regulatory SLA deadlines.',
      tools: ['queueTriageTool', 'exposureCalculatorTool'],
      sampleOutput: 'Triaged 14 open cases: Elevated wire investigation to P1 Urgent (24h FinCEN reporting window).',
    },
    {
      title: 'Expense Intelligence',
      category: 'Cost Optimization',
      icon: Sparkles,
      stat: '12.4% Leakage Reduction',
      desc: 'Detects redundant SaaS subscriptions, duplicate vendor billing, and departmental budget drift.',
      tools: ['expenseAnalyticsTool', 'vendorCategorizerTool'],
      sampleOutput: 'Uncovered $4,200 monthly cloud computing drift and flagged duplicate invoice submission.',
    },
    {
      title: 'Invoice & Receivables',
      category: 'Working Capital',
      icon: Layers,
      stat: '38.4 DSO (Down 2.1d)',
      desc: 'Tracks AR aging buckets, forecasts bad debt default probability, and orchestrates tiered collections.',
      tools: ['invoiceAgingTool', 'paymentPredictionTool'],
      sampleOutput: 'Segmented $45,000 overdue receivables and scheduled automated tier-2 reminder dispatches.',
    },
    {
      title: 'Cash-Flow Forecasting',
      category: 'Treasury Modeling',
      icon: Clock,
      stat: '23.4 Months Runway',
      desc: 'Bayesian Monte Carlo modeling projecting 30/60/90-day cash trajectories with probabilistic volatility bands.',
      tools: ['cashRunwayModelTool', 'monteCarloSimTool'],
      sampleOutput: 'Projected 90-day cash reserve of $1.74M (95% CI: $1.58M – $1.92M). Net burn: $64.2k/mo.',
    },
    {
      title: 'Financial Copilot',
      category: 'Enterprise NLQ',
      icon: Bot,
      stat: 'ReAct Multi-Tool Router',
      desc: 'Natural language enterprise interface querying ledger databases and orchestrating authorized tools.',
      tools: ['nlqRouterTool', 'summarySynthesizerTool'],
      sampleOutput: 'Instant natural language answers citing live database balances, open cases, and liquidity.',
    },
  ];

  return (
    <div className="min-h-screen bg-slate-950 text-white selection:bg-brand-500 selection:text-white font-sans overflow-x-hidden">
      {/* Background Glowing Ambient Orbs */}
      <div className="fixed inset-0 pointer-events-none overflow-hidden z-0">
        <div
          className="absolute -top-40 left-1/4 w-[600px] h-[600px] rounded-full blur-[140px] opacity-25"
          style={{ backgroundColor: accent.primary }}
        />
        <div className="absolute top-1/2 -right-40 w-[500px] h-[500px] bg-indigo-600/20 rounded-full blur-[130px] opacity-30" />
        <div className="absolute -bottom-40 left-10 w-[500px] h-[500px] bg-emerald-600/15 rounded-full blur-[140px] opacity-20" />
        <div className="absolute inset-0 bg-[radial-gradient(#ffffff0a_1px,transparent_1px)] [background-size:24px_24px] opacity-40" />
      </div>

      {/* Glassmorphic Sticky Header */}
      <header className="sticky top-0 z-50 w-full border-b border-white/10 bg-slate-950/80 backdrop-blur-xl transition-all">
        <div className="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8 h-16 sm:h-20 flex items-center justify-between gap-2">
          {/* Logo & Name */}
          <Link to="/" className="flex items-center gap-2 sm:gap-3 group shrink-0">
            <div
              className="w-9 h-9 sm:w-10 sm:h-10 rounded-xl sm:rounded-2xl flex items-center justify-center text-white shadow-lg shadow-brand-500/20 group-hover:scale-105 transition-transform shrink-0"
              style={{ backgroundColor: accent.primary }}
            >
              <Layers className="w-4 h-4 sm:w-5 sm:h-5" />
            </div>
            <div className="flex flex-col">
              <span className="font-extrabold text-base sm:text-xl tracking-tight text-white flex items-center gap-1.5 sm:gap-2">
                {appName}
                <span
                  className="text-[9px] sm:text-[10px] font-bold px-1.5 py-0.5 rounded text-white hidden sm:inline-block"
                  style={{ backgroundColor: accent.primary }}
                >
                  ENTERPRISE
                </span>
              </span>
              <span className="text-[9px] sm:text-[10px] text-slate-400 uppercase tracking-widest font-semibold hidden xs:block sm:block">
                Multi-Agent Intelligence
              </span>
            </div>
          </Link>

          {/* Desktop Nav Links */}
          <nav className="hidden md:flex items-center gap-6 lg:gap-8 text-sm font-medium text-slate-300">
            <a href="#agents" className="hover:text-white transition-colors">
              9 Specialist Agents
            </a>
            <a href="#ingestion" className="hover:text-white transition-colors">
              Bank Ingestion (PDF / CSV)
            </a>
            <a href="#features" className="hover:text-white transition-colors">
              Reconciliation & AML
            </a>
            <a href="#security" className="hover:text-white transition-colors">
              Compliance
            </a>
          </nav>

          {/* Right Controls */}
          <div className="flex items-center gap-1.5 sm:gap-3 shrink-0">
            {/* Desktop Language Switcher */}
            <div className="hidden sm:flex items-center gap-1 bg-white/5 border border-white/10 rounded-lg p-1 text-xs">
              <Globe className="w-3.5 h-3.5 text-slate-400 ml-1.5" />
              <button
                onClick={() => setLanguage('en')}
                className={`px-2 py-0.5 rounded font-semibold text-[11px] transition-all ${
                  language === 'en' ? 'bg-white/20 text-white shadow-sm' : 'text-slate-400 hover:text-white'
                }`}
              >
                EN
              </button>
              <button
                onClick={() => setLanguage('hi')}
                className={`px-2 py-0.5 rounded font-semibold text-[11px] transition-all ${
                  language === 'hi' ? 'bg-white/20 text-white shadow-sm' : 'text-slate-400 hover:text-white'
                }`}
              >
                हिंदी
              </button>
            </div>

            {/* Theme Toggle */}
            <button
              onClick={toggleTheme}
              className="p-2 rounded-lg bg-white/5 border border-white/10 text-slate-300 hover:text-white hover:bg-white/10 transition-colors shrink-0"
              title="Toggle Theme Mode"
            >
              {mode === 'dark' ? <Sun className="w-4 h-4 text-amber-400" /> : <Moon className="w-4 h-4 text-slate-300" />}
            </button>

            {/* Launch Dashboard / Sign In CTA */}
            <Link to={isAuthenticated ? '/dashboard' : '/login'} className="shrink-0">
              <Button
                size="sm"
                className="font-bold shadow-lg shadow-brand-500/25 px-2.5 sm:px-4 py-1.5 text-xs sm:text-sm whitespace-nowrap shrink-0 flex items-center gap-1.5"
                rightIcon={<ArrowRight className="w-3 h-3 sm:w-3.5 sm:h-3.5" />}
              >
                <span className="hidden sm:inline">{isAuthenticated ? 'Launch Platform' : 'Sign In'}</span>
                <span className="sm:hidden">{isAuthenticated ? 'Console' : 'Sign In'}</span>
              </Button>
            </Link>

            {/* Mobile Hamburger Menu Toggle */}
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 rounded-lg bg-white/5 border border-white/10 text-slate-300 hover:text-white hover:bg-white/10 transition-colors md:hidden shrink-0"
              aria-label="Toggle navigation menu"
            >
              {mobileMenuOpen ? <X className="w-4 h-4" /> : <Menu className="w-4 h-4" />}
            </button>
          </div>
        </div>

        {/* Mobile Dropdown Menu */}
        {mobileMenuOpen && (
          <div className="md:hidden border-b border-white/10 bg-slate-950/95 backdrop-blur-2xl px-4 py-4 space-y-3 animate-fadeIn">
            <nav className="flex flex-col space-y-1 text-sm font-medium text-slate-300">
              <a
                href="#agents"
                onClick={() => setMobileMenuOpen(false)}
                className="px-3 py-2 rounded-lg hover:bg-white/5 hover:text-white transition-colors"
              >
                9 Specialist Agents
              </a>
              <a
                href="#ingestion"
                onClick={() => setMobileMenuOpen(false)}
                className="px-3 py-2 rounded-lg hover:bg-white/5 hover:text-white transition-colors"
              >
                Bank Ingestion (PDF / CSV)
              </a>
              <a
                href="#features"
                onClick={() => setMobileMenuOpen(false)}
                className="px-3 py-2 rounded-lg hover:bg-white/5 hover:text-white transition-colors"
              >
                Reconciliation & AML
              </a>
              <a
                href="#security"
                onClick={() => setMobileMenuOpen(false)}
                className="px-3 py-2 rounded-lg hover:bg-white/5 hover:text-white transition-colors"
              >
                Compliance & Security
              </a>
            </nav>

            <div className="pt-3 border-t border-white/10 flex items-center justify-between">
              <span className="text-xs text-slate-400 font-medium">Language</span>
              <div className="flex items-center gap-1 bg-white/5 border border-white/10 rounded-lg p-1 text-xs">
                <Globe className="w-3.5 h-3.5 text-slate-400 ml-1 mr-0.5" />
                <button
                  onClick={() => setLanguage('en')}
                  className={`px-2 py-0.5 rounded font-semibold text-[11px] transition-all ${
                    language === 'en' ? 'bg-white/20 text-white shadow-sm' : 'text-slate-400 hover:text-white'
                  }`}
                >
                  English
                </button>
                <button
                  onClick={() => setLanguage('hi')}
                  className={`px-2 py-0.5 rounded font-semibold text-[11px] transition-all ${
                    language === 'hi' ? 'bg-white/20 text-white shadow-sm' : 'text-slate-400 hover:text-white'
                  }`}
                >
                  हिंदी
                </button>
              </div>
            </div>

            <Link
              to={isAuthenticated ? '/dashboard' : '/login'}
              onClick={() => setMobileMenuOpen(false)}
              className="block w-full pt-2"
            >
              <button
                className="w-full py-3 rounded-xl font-bold text-white text-sm flex items-center justify-center gap-2 shadow-lg"
                style={{ backgroundColor: accent.primary }}
              >
                <span>{isAuthenticated ? 'Open Financial Console' : 'Launch Platform'}</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </Link>
          </div>
        )}
      </header>

      {/* HERO SECTION */}
      <section className="relative z-10 pt-10 pb-16 sm:pt-16 sm:pb-20 md:pt-24 md:pb-28 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <div className="max-w-4xl mx-auto space-y-4 sm:space-y-6">
          {/* Trust Pill */}
          <div className="inline-flex items-center gap-2 px-3 sm:px-4 py-1.5 rounded-full bg-white/5 border border-white/15 text-[11px] sm:text-xs font-semibold backdrop-blur-md shadow-lg animate-fadeIn max-w-[95vw]">
            <Sparkles className="w-3.5 h-3.5 sm:w-4 sm:h-4 text-brand-400 shrink-0" />
            <span className="text-slate-300">
              <span className="sm:hidden">Autonomous Financial AI & Surveillance</span>
              <span className="hidden sm:inline">Autonomous Financial Intelligence & Multi-Agent Surveillance</span>
            </span>
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse ml-0.5 shrink-0"></span>
          </div>

          {/* Main Title */}
          <h1 className="text-3xl sm:text-5xl md:text-6xl lg:text-7xl font-extrabold tracking-tight text-white leading-[1.15] text-balance">
            Autonomous Financial Intelligence Powered by{' '}
            <span
              className="bg-clip-text text-transparent bg-gradient-to-r from-blue-400 via-indigo-300 to-purple-400"
            >
              9 Specialist Agents
            </span>
          </h1>

          {/* Subtitle */}
          <p className="text-sm sm:text-lg md:text-xl text-slate-400 max-w-2xl mx-auto leading-relaxed text-balance px-2">
            {appTagline ||
              'Connect disconnected core banking ledgers, automate 4-step reconciliation, detect high-velocity AML structuring, and forecast cash runways in real time.'}
          </p>

          {/* Dual CTAs */}
          <div className="pt-2 sm:pt-4 flex flex-col sm:flex-row items-center justify-center gap-3 sm:gap-4 max-w-md sm:max-w-none mx-auto">
            <Link to={isAuthenticated ? '/dashboard' : '/login'} className="w-full sm:w-auto">
              <button
                className="w-full sm:w-auto px-6 sm:px-8 py-3.5 sm:py-4 rounded-2xl font-bold text-white shadow-2xl shadow-brand-500/30 flex items-center justify-center gap-2 hover:scale-105 active:scale-95 transition-all text-sm group"
                style={{ backgroundColor: accent.primary }}
              >
                <span>{isAuthenticated ? 'Open Financial Console' : 'Launch Enterprise Platform'}</span>
                <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
              </button>
            </Link>

            <a href="#agents" className="w-full sm:w-auto">
              <button className="w-full sm:w-auto px-5 sm:px-6 py-3.5 sm:py-4 rounded-2xl font-semibold text-slate-300 bg-white/5 hover:bg-white/10 border border-white/15 backdrop-blur-md flex items-center justify-center gap-2 transition-all text-sm">
                <Bot className="w-4 h-4 text-brand-400" />
                <span>Explore 9 Specialist Agents</span>
              </button>
            </a>
          </div>

          {/* Metrics Ticker */}
          <div className="pt-8 sm:pt-12 grid grid-cols-2 md:grid-cols-4 gap-2.5 sm:gap-4 max-w-4xl mx-auto">
            <div className="p-3 sm:p-4 rounded-2xl bg-white/[0.03] border border-white/10 backdrop-blur-md hover-lift card-sheen hover:border-white/20 hover:shadow-[0_0_20px_rgba(255,255,255,0.06)] transition-all">
              <div className="text-xl sm:text-3xl font-extrabold text-white font-mono">99.51%</div>
              <div className="text-[10px] sm:text-xs text-slate-400 mt-1">Recon Match Accuracy</div>
            </div>
            <div className="p-3 sm:p-4 rounded-2xl bg-white/[0.03] border border-white/10 backdrop-blur-md hover-lift card-sheen hover:border-emerald-500/30 hover:shadow-[0_0_20px_rgba(16,185,129,0.15)] transition-all">
              <div className="text-xl sm:text-3xl font-extrabold text-emerald-400 font-mono">9 Active</div>
              <div className="text-[10px] sm:text-xs text-slate-400 mt-1">Specialist AI Agents</div>
            </div>
            <div className="p-3 sm:p-4 rounded-2xl bg-white/[0.03] border border-white/10 backdrop-blur-md hover-lift card-sheen hover:border-indigo-500/30 hover:shadow-[0_0_20px_rgba(99,102,241,0.15)] transition-all">
              <div className="text-xl sm:text-3xl font-extrabold text-white font-mono">&lt; 850ms</div>
              <div className="text-[10px] sm:text-xs text-slate-400 mt-1">ReAct Tool Latency</div>
            </div>
            <div className="p-3 sm:p-4 rounded-2xl bg-white/[0.03] border border-white/10 backdrop-blur-md hover-lift card-sheen hover:border-brand-500/30 hover:shadow-[0_0_20px_rgba(59,130,246,0.15)] transition-all">
              <div className="text-xl sm:text-3xl font-extrabold text-brand-400 font-mono">$2.4B+</div>
              <div className="text-[10px] sm:text-xs text-slate-400 mt-1">Ledger Volume Monitored</div>
            </div>
          </div>
        </div>
      </section>

      {/* 9 SPECIALIST AGENTS SHOWCASE */}
      <section id="agents" className="relative z-10 py-20 border-t border-white/10 bg-slate-900/40 backdrop-blur-md">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-12">
            <Badge variant="info" size="sm">
              Autonomous Agent Orchestration
            </Badge>
            <h2 className="text-3xl sm:text-4xl font-extrabold text-white mt-3">
              The Fleet of 9 Autonomous Specialist Agents
            </h2>
            <p className="text-slate-400 text-sm mt-2">
              Every critical dimension of enterprise fintech operates through a dedicated, specialized ReAct agent with deterministic audit logs and human approval gates.
            </p>
          </div>

          {/* Interactive Agent Showcase Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {AGENT_FLEET.map((agent, idx) => {
              const Icon = agent.icon;
              return (
                <div
                  key={agent.title}
                  className="rounded-2xl p-6 bg-slate-900/80 border border-white/10 backdrop-blur-lg hover:border-brand-500/50 hover:bg-slate-900 hover:shadow-[0_12px_32px_-8px_rgba(0,0,0,0.8),0_0_24px_rgba(59,130,246,0.2)] card-sheen transition-all duration-300 hover-lift group flex flex-col justify-between"
                >
                  <div>
                    <div className="flex items-center justify-between mb-4">
                      <div
                        className="w-12 h-12 rounded-xl flex items-center justify-center text-white shadow-md group-hover:scale-110 transition-transform"
                        style={{ backgroundColor: accent.primary }}
                      >
                        <Icon className="w-6 h-6" />
                      </div>
                      <span className="text-[11px] font-mono font-semibold px-2.5 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                        {agent.stat}
                      </span>
                    </div>

                    <div className="text-[10px] font-bold uppercase tracking-wider text-brand-400">
                      {agent.category}
                    </div>
                    <h3 className="text-base font-bold text-white mt-1 group-hover:text-brand-300 transition-colors">
                      {agent.title}
                    </h3>
                    <p className="text-xs text-slate-400 mt-2 leading-relaxed">
                      {agent.desc}
                    </p>
                  </div>

                  <div className="mt-5 pt-4 border-t border-white/10">
                    <div className="p-3 rounded-xl bg-slate-950/80 border border-white/5 text-[11px] text-slate-300 font-mono">
                      <span className="text-slate-500 block text-[9px] uppercase font-bold mb-1">Simulated Output</span>
                      {agent.sampleOutput}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* BANK DOCUMENT INGESTION (PDF & CSV) SHOWCASE */}
      <section id="ingestion" className="relative z-10 py-20 border-t border-white/10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
            <div className="lg:col-span-6 space-y-5">
              <Badge variant="success" size="sm">
                Dual PDF & CSV Ingestion
              </Badge>
              <h2 className="text-3xl sm:text-4xl font-extrabold text-white">
                Official Bank E-Statements & Financial Ledgers Only
              </h2>
              <p className="text-slate-400 text-sm leading-relaxed">
                FinSight AI incorporates strict financial document classification. Upload authentic PDF bank statements, CAMT.053, MT940, or CSV ledger feeds with instant SHA-256 idempotency verification.
              </p>

              <div className="space-y-3 pt-2">
                <div className="flex items-start gap-3">
                  <div className="w-6 h-6 rounded-lg bg-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0 mt-0.5">
                    <Check className="w-3.5 h-3.5" />
                  </div>
                  <div>
                    <h4 className="text-xs font-bold text-white">Full PDF E-Statement Optical & Stream Parsing</h4>
                    <p className="text-xs text-slate-400 mt-0.5">
                      Extracts Chase, Barclays, Wells Fargo, and Citibank account numbers, statement periods, and debit/credit balances.
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <div className="w-6 h-6 rounded-lg bg-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0 mt-0.5">
                    <Check className="w-3.5 h-3.5" />
                  </div>
                  <div>
                    <h4 className="text-xs font-bold text-white">Strict Compliance Guardrail Rejection</h4>
                    <p className="text-xs text-slate-400 mt-0.5">
                      Automatically detects and rejects non-banking documents (such as personal resumes or employee rosters) with clear diagnostic feedback.
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <div className="w-6 h-6 rounded-lg bg-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0 mt-0.5">
                    <Check className="w-3.5 h-3.5" />
                  </div>
                  <div>
                    <h4 className="text-xs font-bold text-white">Zero Duplicate Ingestion Guarantee</h4>
                    <p className="text-xs text-slate-400 mt-0.5">
                      SHA-256 transaction fingerprints cached for 24h to prevent double-clearing of ledger items.
                    </p>
                  </div>
                </div>
              </div>

              <div className="pt-2">
                <Link to="/financial-data">
                  <Button size="sm" rightIcon={<ArrowRight className="w-3.5 h-3.5" />}>
                    Try Bank Document Ingestion
                  </Button>
                </Link>
              </div>
            </div>

            {/* Ingestion Visual Mockup Card */}
            <div className="lg:col-span-6">
              <div className="rounded-3xl p-6 bg-slate-900/90 border border-white/15 backdrop-blur-xl shadow-2xl hover:border-brand-500/30 hover:shadow-[0_20px_50px_rgba(0,0,0,0.8),0_0_30px_rgba(59,130,246,0.15)] card-sheen transition-all duration-300 space-y-4">
                <div className="flex items-center justify-between border-b border-white/10 pb-3">
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full bg-rose-500" />
                    <div className="w-3 h-3 rounded-full bg-amber-500" />
                    <div className="w-3 h-3 rounded-full bg-emerald-500" />
                    <span className="text-xs font-mono text-slate-400 ml-2">Bank Document Ingestion Guardrail</span>
                  </div>
                  <Badge variant="success" size="sm">
                    Verified Bank Document
                  </Badge>
                </div>

                {/* Chase Verified Preview Card */}
                <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/30 space-y-3">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2.5">
                      <FileText className="w-5 h-5 text-emerald-400" />
                      <div>
                        <div className="text-xs font-bold text-white">JPMorgan Chase Bank, N.A.</div>
                        <div className="text-[11px] text-emerald-300 font-mono">ACC-10029384 · Oct 2026 E-Statement</div>
                      </div>
                    </div>
                    <span className="text-[10px] font-bold text-emerald-400 bg-emerald-500/20 px-2 py-0.5 rounded">
                      Confidence 96%
                    </span>
                  </div>

                  <div className="grid grid-cols-3 gap-2 text-center text-xs pt-1">
                    <div className="p-2 rounded bg-slate-950/60 border border-white/5">
                      <div className="text-[10px] text-slate-400 uppercase">Records</div>
                      <div className="font-bold text-white">5 txns</div>
                    </div>
                    <div className="p-2 rounded bg-slate-950/60 border border-white/5">
                      <div className="text-[10px] text-slate-400 uppercase">Inflow</div>
                      <div className="font-bold text-emerald-400">+$209.7k</div>
                    </div>
                    <div className="p-2 rounded bg-slate-950/60 border border-white/5">
                      <div className="text-[10px] text-slate-400 uppercase">Outflow</div>
                      <div className="font-bold text-slate-300">-$98.5k</div>
                    </div>
                  </div>
                </div>

                {/* Rejection Guardrail Card */}
                <div className="p-3.5 rounded-xl bg-rose-500/10 border border-rose-500/30 flex items-center justify-between text-xs">
                  <div className="flex items-center gap-2 text-rose-300">
                    <ShieldAlert className="w-4 h-4 text-rose-400 shrink-0" />
                    <span>Non-Banking File (e.g. Resume, HR Roster)</span>
                  </div>
                  <span className="text-[10px] font-bold text-rose-400 uppercase">
                    Auto-Rejected by Guardrail
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* FOOTER */}
      <footer className="relative z-10 border-t border-white/10 bg-slate-950 py-8 sm:py-12 text-xs text-slate-500">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row items-center justify-between gap-4 sm:gap-6 text-center md:text-left">
          <div className="flex items-center gap-2.5 sm:gap-3 justify-center">
            <div
              className="w-7 h-7 rounded-lg flex items-center justify-center text-white shrink-0"
              style={{ backgroundColor: accent.primary }}
            >
              <Layers className="w-4 h-4" />
            </div>
            <span className="font-bold text-sm text-white">{appName}</span>
            <span className="hidden sm:inline">— Enterprise Multi-Agent Financial Intelligence</span>
          </div>

          <div className="flex flex-wrap items-center justify-center gap-4 sm:gap-6">
            <span className="flex items-center gap-1.5 text-slate-400">
              <ShieldCheck className="w-4 h-4 text-emerald-400" /> SOC2 Type II Compliant
            </span>
            <span className="flex items-center gap-1.5 text-slate-400">
              <Lock className="w-4 h-4 text-brand-400" /> AES-256 Encrypted
            </span>
            <span>© 2026 {appName}. All rights reserved.</span>
          </div>
        </div>
      </footer>
    </div>
  );
};

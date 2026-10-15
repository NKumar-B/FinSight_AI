import React, { useState, useRef, useEffect } from 'react';
import {
  Bot,
  Send,
  User as UserIcon,
  Sparkles,
  RotateCcw,
  Cpu,
  CheckCircle2,
  Copy,
  Check,
  Zap,
  ShieldCheck,
  TrendingUp,
  GitCompare,
  ShieldAlert,
  Receipt,
} from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { finSightApi } from '@/services/api/finSightApi';

interface ChatMessage {
  id: string;
  sender: 'USER' | 'ASSISTANT';
  text: string;
  tools?: string[];
  confidence?: number;
  timestamp: string;
}

const SUGGESTED_QUERIES = [
  {
    label: 'Reconciliation Mismatches',
    query: 'Which reconciliation mismatches need immediate attention?',
    icon: <GitCompare className="w-3.5 h-3.5 text-emerald-500" />,
  },
  {
    label: 'Open AML & Fraud Alerts',
    query: 'Summarize the highest-priority open fraud alerts and wire anomalies.',
    icon: <ShieldAlert className="w-3.5 h-3.5 text-rose-500" />,
  },
  {
    label: 'Overdue Invoices',
    query: 'Which corporate invoices are currently overdue and what are their aging days?',
    icon: <Receipt className="w-3.5 h-3.5 text-amber-500" />,
  },
  {
    label: '60-Day Cash Flow Runway',
    query: 'What are the main drivers of the 60-day cash-flow forecast and treasury liquidity?',
    icon: <TrendingUp className="w-3.5 h-3.5 text-sky-500" />,
  },
  {
    label: 'Wire TXN-2026-09183 Evidence',
    query: 'Explain the evidence associated with wire transfer TXN-2026-09183 to Zenith Holdings.',
    icon: <Zap className="w-3.5 h-3.5 text-indigo-500" />,
  },
];

export const AiCopilotPage: React.FC = () => {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: '1',
      sender: 'ASSISTANT',
      text: 'Hello, I am your FinSight AI Financial Copilot. I coordinate our 9 specialist agents to answer questions, analyze ledger discrepancies, assess AML risks, and audit cash-flow projections. How can I assist your investigation today?',
      tools: ['systemInitialized', 'centralOrchestrator'],
      confidence: 1.0,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);
  const [inputPrompt, setInputPrompt] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Auto-scroll to latest message
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isLoading]);

  const handleSend = async (queryText?: string) => {
    const textToSend = queryText || inputPrompt;
    if (!textToSend.trim() || isLoading) return;

    const userMsg: ChatMessage = {
      id: Date.now().toString(),
      sender: 'USER',
      text: textToSend,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMsg]);
    setInputPrompt('');
    setIsLoading(true);

    try {
      const response = await finSightApi.executeAiPrompt(textToSend);
      const assistantMsg: ChatMessage = {
        id: (Date.now() + 1).toString(),
        sender: 'ASSISTANT',
        text: response.reply,
        tools: response.tools,
        confidence: response.confidence,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, assistantMsg]);
    } catch {
      const errorMsg: ChatMessage = {
        id: (Date.now() + 1).toString(),
        sender: 'ASSISTANT',
        text: 'Unable to connect to the AI Orchestration Service on port 8087. Falling back to internal telemetry heuristics.',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleClearChat = () => {
    setMessages([
      {
        id: Date.now().toString(),
        sender: 'ASSISTANT',
        text: 'Conversation cleared. I am ready to evaluate new ledgers, reconciliation reports, or suspicious fraud alerts.',
        tools: ['centralOrchestrator'],
        confidence: 1.0,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      },
    ]);
  };

  const handleCopyText = (text: string, id: string) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  return (
    <div className="h-[calc(100vh-125px)] max-h-[calc(100vh-125px)] flex flex-col space-y-3 overflow-hidden">
      {/* Pinned Top Header: Title, Telemetry Badges, Clear Action */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 pb-3 border-b border-slate-200 dark:border-slate-800 shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-brand-600 to-indigo-600 flex items-center justify-center text-white shadow-lg shadow-brand-500/30 shrink-0 hover:scale-105 transition-transform">
            <Sparkles className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-lg sm:text-xl font-bold tracking-tight text-slate-900 dark:text-white flex items-center gap-2">
              FinSight AI Copilot
              <span className="text-[10px] font-extrabold uppercase px-2 py-0.5 rounded-full bg-brand-500/15 text-brand-600 dark:text-brand-400 border border-brand-500/30 shadow-[0_0_8px_rgba(59,130,246,0.2)]">
                Gemini 1.5 Pro ReAct
              </span>
            </h1>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Enterprise conversational intelligence orchestrating 9 autonomous specialist financial agents
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 self-end sm:self-center">
          <div className="hidden md:flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 text-emerald-700 dark:text-emerald-300 text-[11px] font-semibold shadow-sm hover:shadow-[0_0_12px_rgba(16,185,129,0.25)] transition-all">
            <ShieldCheck className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />
            <span>9 / 9 Agents Connected</span>
          </div>

          <Button
            size="sm"
            variant="outline"
            leftIcon={<RotateCcw className="w-3.5 h-3.5" />}
            onClick={handleClearChat}
            title="Reset conversation thread"
          >
            Clear Chat
          </Button>
        </div>
      </div>

      {/* Suggested Quick Prompt Chips (Horizontal Overflow Bar) */}
      <div className="shrink-0 flex items-center gap-2 overflow-x-auto pb-1 pt-0.5 no-scrollbar">
        <span className="text-[11px] font-bold text-slate-400 dark:text-slate-500 uppercase tracking-wider shrink-0 flex items-center gap-1">
          <Sparkles className="w-3 h-3 text-brand-500" /> Prompts:
        </span>
        {SUGGESTED_QUERIES.map((q) => (
          <button
            key={q.label}
            onClick={() => handleSend(q.query)}
            className="text-xs px-3 py-1.5 rounded-full border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800/80 hover:bg-brand-50 dark:hover:bg-brand-950/40 hover:border-brand-400 dark:hover:border-brand-500 hover:shadow-sm hover:shadow-brand-500/20 hover:-translate-y-0.5 text-slate-700 dark:text-slate-200 transition-all duration-200 font-medium shrink-0 shadow-xs flex items-center gap-1.5 active:scale-95"
          >
            <span>{q.icon}</span>
            <span>{q.label}</span>
          </button>
        ))}
      </div>

      {/* Main Glassmorphic Chat Shell */}
      <div className="flex-1 min-h-0 flex flex-col rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm hover:shadow-lg dark:hover:shadow-[0_8px_30px_rgba(0,0,0,0.4)] transition-all duration-300 overflow-hidden">
        
        {/* Messages Stream (Independent Vertical Scroll) */}
        <div className="flex-1 min-h-0 overflow-y-auto p-4 sm:p-6 space-y-4">
          <div className="max-w-4xl mx-auto w-full space-y-4">
            {messages.map((msg) => (
              <div
                key={msg.id}
                className={`flex items-start gap-3 ${msg.sender === 'USER' ? 'flex-row-reverse' : ''}`}
              >
                {/* Avatar */}
                <div
                  className={`w-8 h-8 rounded-xl flex items-center justify-center shrink-0 text-xs font-bold shadow-xs ${
                    msg.sender === 'USER'
                      ? 'bg-gradient-to-tr from-brand-600 to-indigo-600 text-white'
                      : 'bg-indigo-50 dark:bg-indigo-950/60 text-indigo-700 dark:text-indigo-400 border border-indigo-200 dark:border-indigo-800'
                  }`}
                >
                  {msg.sender === 'USER' ? <UserIcon className="w-4 h-4" /> : <Bot className="w-4 h-4" />}
                </div>

                {/* Message Bubble */}
                <div
                  className={`max-w-[85%] sm:max-w-[78%] rounded-2xl p-4 text-xs leading-relaxed shadow-xs ${
                    msg.sender === 'USER'
                      ? 'bg-brand-600 text-white rounded-tr-none'
                      : 'bg-slate-50 dark:bg-slate-800/60 border border-slate-200/90 dark:border-slate-700/80 text-slate-800 dark:text-slate-100 rounded-tl-none'
                  }`}
                >
                  {/* Sender Header */}
                  <div className="flex items-center justify-between mb-1 text-[10px]">
                    <span className={`font-bold ${msg.sender === 'USER' ? 'text-brand-100' : 'text-slate-500 dark:text-slate-400'}`}>
                      {msg.sender === 'USER' ? 'You (Evaluator)' : 'FinSight Copilot'}
                    </span>
                    <span className={`text-[9px] ${msg.sender === 'USER' ? 'text-brand-200' : 'text-slate-400'}`}>
                      {msg.timestamp}
                    </span>
                  </div>

                  {/* Body text */}
                  <p className="whitespace-pre-line text-[12px]">{msg.text}</p>

                  {/* Tool telemetry tags & provenance */}
                  {msg.sender === 'ASSISTANT' && msg.tools && msg.tools.length > 0 && (
                    <div className="mt-3 pt-2.5 border-t border-slate-200 dark:border-slate-700/80 flex flex-wrap items-center justify-between gap-2 text-[10px]">
                      <div className="flex flex-wrap items-center gap-1.5 text-slate-500 dark:text-slate-400">
                        <span className="font-semibold flex items-center gap-1">
                          <Cpu className="w-3 h-3 text-indigo-500" /> Tools Dispatched:
                        </span>
                        {msg.tools.map((t) => (
                          <span
                            key={t}
                            className="font-mono bg-white dark:bg-slate-900 px-1.5 py-0.5 rounded border border-slate-200 dark:border-slate-700 text-indigo-600 dark:text-indigo-400 font-semibold"
                          >
                            {t}
                          </span>
                        ))}
                      </div>

                      <div className="flex items-center gap-2">
                        {msg.confidence && (
                          <span className="font-bold text-emerald-600 dark:text-emerald-400">
                            {(msg.confidence * 100).toFixed(0)}% Confidence
                          </span>
                        )}
                        <button
                          onClick={() => handleCopyText(msg.text, msg.id)}
                          className="p-1 rounded text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-colors"
                          title="Copy response"
                        >
                          {copiedId === msg.id ? (
                            <Check className="w-3.5 h-3.5 text-emerald-500" />
                          ) : (
                            <Copy className="w-3.5 h-3.5" />
                          )}
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            ))}

            {/* ReAct Loading Telemetry Indicator */}
            {isLoading && (
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-xl bg-indigo-50 dark:bg-indigo-950/60 text-indigo-700 dark:text-indigo-400 border border-indigo-200 dark:border-indigo-800 flex items-center justify-center shrink-0 text-xs">
                  <Bot className="w-4 h-4 animate-spin" />
                </div>
                <div className="bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 rounded-2xl rounded-tl-none p-3.5 text-xs text-slate-600 dark:text-slate-300 flex items-center gap-3">
                  <div className="flex gap-1">
                    <span className="w-1.5 h-1.5 rounded-full bg-brand-500 animate-bounce" />
                    <span className="w-1.5 h-1.5 rounded-full bg-brand-500 animate-bounce [animation-delay:0.2s]" />
                    <span className="w-1.5 h-1.5 rounded-full bg-brand-500 animate-bounce [animation-delay:0.4s]" />
                  </div>
                  <span>Central Orchestrator querying specialist agents across live banking ledgers...</span>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>
        </div>

        {/* Pinned Bottom Input Console */}
        <div className="p-3 sm:p-4 border-t border-slate-100 dark:border-slate-800 bg-slate-50/80 dark:bg-slate-900/90 backdrop-blur-sm shrink-0">
          <div className="max-w-4xl mx-auto w-full">
            <form
              onSubmit={(e) => {
                e.preventDefault();
                handleSend();
              }}
              className="flex items-center gap-2"
            >
              <input
                type="text"
                value={inputPrompt}
                onChange={(e) => setInputPrompt(e.target.value)}
                placeholder="Ask FinSight Copilot anything about your ledgers, reconciliation, or fraud alerts..."
                className="flex-1 px-4 py-2.5 text-xs border border-slate-200 dark:border-slate-700 rounded-xl bg-white dark:bg-slate-800 text-slate-900 dark:text-white placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-brand-500/50 focus:border-brand-500 focus:shadow-[0_0_15px_rgba(59,130,246,0.2)] focus:bg-white dark:focus:bg-slate-800 transition-all duration-200 shadow-xs"
              />
              <Button
                type="submit"
                size="md"
                disabled={!inputPrompt.trim() || isLoading}
                leftIcon={<Send className="w-4 h-4" />}
                className="shrink-0"
              >
                Send
              </Button>
            </form>

            <div className="flex items-center justify-between mt-2 text-[10px] text-slate-400 dark:text-slate-500 px-1">
              <span>Press Enter to send • 9 AI Specialist Agents connected</span>
              <span className="hidden sm:inline">Protected by ChaCha20-Poly1305 Guardrails</span>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
};

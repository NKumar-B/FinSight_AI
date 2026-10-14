import React, { useState } from 'react';
import { Bot, Send, User as UserIcon, Sparkles, CheckCircle2, ShieldAlert, Cpu } from 'lucide-react';
import { Card } from '@/components/ui/Card';
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
  'Which reconciliation mismatches need immediate attention?',
  'Summarize the highest-priority open fraud alerts.',
  'Which corporate invoices are currently overdue?',
  'What are the main drivers of the 60-day cash-flow forecast?',
  'Explain the evidence associated with wire transfer TXN-2026-09183.',
];

export const AiCopilotPage: React.FC = () => {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: '1',
      sender: 'ASSISTANT',
      text: 'Hello, I am your FinSight AI Financial Copilot. I coordinate our 9 specialist agents to answer questions, analyze ledger discrepancies, assess AML risks, and audit cash-flow projections. How can I assist your investigation today?',
      tools: ['systemInitialized'],
      confidence: 1.0,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);
  const [inputPrompt, setInputPrompt] = useState('');
  const [isLoading, setIsLoading] = useState(false);

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
        text: 'Unable to connect to the AI Orchestration Service on port 8087. Please ensure the backend is running.',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 flex items-center gap-2">
            <Sparkles className="w-5 h-5 text-brand-600" />
            FinSight AI Copilot
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Enterprise conversational intelligence powered by Google Gemini and 9 specialized financial tools.
          </p>
        </div>

        <Badge variant="info" size="md">
          Gemini 1.5 Pro ReAct Online
        </Badge>
      </div>

      {/* Suggested Prompts Pills */}
      <div>
        <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block mb-2">
          Recommended Queries:
        </span>
        <div className="flex flex-wrap gap-2">
          {SUGGESTED_QUERIES.map((q) => (
            <button
              key={q}
              onClick={() => handleSend(q)}
              className="text-xs px-3 py-1.5 rounded-full border border-slate-200 bg-white hover:bg-brand-50 hover:border-brand-200 text-slate-700 transition-all font-medium text-left"
            >
              {q}
            </button>
          ))}
        </div>
      </div>

      {/* Chat Container */}
      <Card noPadding className="flex flex-col h-[540px] border border-slate-200">
        {/* Messages Scroll Area */}
        <div className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-4">
          {messages.map((msg) => (
            <div
              key={msg.id}
              className={`flex items-start gap-3 ${msg.sender === 'USER' ? 'flex-row-reverse' : ''}`}
            >
              <div
                className={`w-8 h-8 rounded-lg flex items-center justify-center shrink-0 text-xs font-bold ${
                  msg.sender === 'USER'
                    ? 'bg-brand-600 text-white'
                    : 'bg-indigo-50 text-indigo-700 border border-indigo-200'
                }`}
              >
                {msg.sender === 'USER' ? <UserIcon className="w-4 h-4" /> : <Bot className="w-4 h-4" />}
              </div>

              <div
                className={`max-w-[80%] rounded-2xl p-4 text-xs leading-relaxed ${
                  msg.sender === 'USER'
                    ? 'bg-brand-600 text-white rounded-tr-none'
                    : 'bg-slate-50 border border-slate-200 text-slate-800 rounded-tl-none'
                }`}
              >
                <p>{msg.text}</p>

                {/* Tool provenance & confidence badge */}
                {msg.sender === 'ASSISTANT' && msg.tools && msg.tools.length > 0 && (
                  <div className="mt-3 pt-2.5 border-t border-slate-200/80 flex flex-wrap items-center gap-1.5 text-[10px] text-slate-500">
                    <span className="font-semibold text-slate-600 flex items-center gap-1">
                      <Cpu className="w-3 h-3 text-indigo-500" /> Tools Dispatched:
                    </span>
                    {msg.tools.map((t) => (
                      <span key={t} className="font-mono bg-white px-1.5 py-0.5 rounded border border-slate-200 text-indigo-700">
                        {t}
                      </span>
                    ))}
                    {msg.confidence && (
                      <span className="ml-auto font-bold text-emerald-600">
                        {(msg.confidence * 100).toFixed(0)}% Confidence
                      </span>
                    )}
                  </div>
                )}
                <span className={`block text-[9px] mt-1 text-right ${msg.sender === 'USER' ? 'text-brand-100' : 'text-slate-400'}`}>
                  {msg.timestamp}
                </span>
              </div>
            </div>
          ))}

          {isLoading && (
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-lg bg-indigo-50 text-indigo-700 border border-indigo-200 flex items-center justify-center text-xs">
                <Bot className="w-4 h-4" />
              </div>
              <div className="bg-slate-50 border border-slate-200 rounded-2xl rounded-tl-none p-3.5 text-xs text-slate-500 flex items-center gap-2">
                <div className="flex gap-1">
                  <span className="w-1.5 h-1.5 rounded-full bg-slate-400 animate-bounce" />
                  <span className="w-1.5 h-1.5 rounded-full bg-slate-400 animate-bounce [animation-delay:0.2s]" />
                  <span className="w-1.5 h-1.5 rounded-full bg-slate-400 animate-bounce [animation-delay:0.4s]" />
                </div>
                <span>Orchestrator dispatching specialist agents...</span>
              </div>
            </div>
          )}
        </div>

        {/* Input Bar */}
        <div className="p-3 sm:p-4 border-t border-slate-200 bg-white">
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
              className="flex-1 px-4 py-2.5 text-xs border border-slate-200 rounded-xl bg-slate-50 text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white transition-all"
            />
            <Button type="submit" size="md" disabled={!inputPrompt.trim() || isLoading} leftIcon={<Send className="w-4 h-4" />}>
              Send
            </Button>
          </form>
        </div>
      </Card>
    </div>
  );
};

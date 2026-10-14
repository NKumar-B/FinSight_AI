import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { LineChart as LineChartIcon, TrendingUp, Calendar, AlertCircle, ShieldCheck } from 'lucide-react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Area,
  ComposedChart,
} from 'recharts';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Select } from '@/components/ui/Select';
import { finSightApi } from '@/services/api/finSightApi';
import { formatCurrency, formatDate } from '@/utils/formatters';

export const CashFlowPage: React.FC = () => {
  const [horizon, setHorizon] = useState('60');

  const { data: forecastData, isLoading } = useQuery({
    queryKey: ['cashFlowForecast'],
    queryFn: finSightApi.getCashFlowForecast,
  });

  return (
    <div className="space-y-6 pb-12">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900">
            Cash-Flow Runway & Predictive Forecasting
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Empirical historical actuals combined with AI Cash-Flow Specialist projections and uncertainty bounds.
          </p>
        </div>

        <div className="w-44">
          <Select
            value={horizon}
            onChange={(e) => setHorizon(e.target.value)}
            options={[
              { label: '30-Day Horizon', value: '30' },
              { label: '60-Day Horizon', value: '60' },
              { label: '90-Day Horizon', value: '90' },
            ]}
          />
        </div>
      </div>

      {/* Runway Status Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card className="border-l-4 border-l-brand-600">
          <div className="text-xs font-semibold text-slate-500 uppercase">Current Liquidity</div>
          <div className="text-2xl font-bold text-slate-900 mt-1 table-cell-num">{formatCurrency(1485230.5)}</div>
          <span className="text-[11px] text-emerald-600 font-medium mt-1 block">Live balance across operating accounts</span>
        </Card>

        <Card className="border-l-4 border-l-emerald-600">
          <div className="text-xs font-semibold text-slate-500 uppercase">Projected Balance (60-Day)</div>
          <div className="text-2xl font-bold text-emerald-600 mt-1 table-cell-num">{formatCurrency(1835230.0)}</div>
          <span className="text-[11px] text-slate-400 mt-1 block">+$350k net operating cash inflow</span>
        </Card>

        <Card className="border-l-4 border-l-indigo-600">
          <div className="text-xs font-semibold text-slate-500 uppercase">Estimated Cash Runway</div>
          <div className="text-2xl font-bold text-indigo-600 mt-1">18.4 Months</div>
          <span className="text-[11px] text-slate-400 mt-1 block">Based on $85k/mo average burn rate</span>
        </Card>
      </div>

      {/* Main Forecast Chart */}
      <Card
        title="Predictive Cash Trajectory & Inflow/Outflow Projection"
        subtitle="Historical actual ledger entries vs machine learning forecast with 95% confidence intervals"
      >
        <div className="h-80 w-full mt-2">
          <ResponsiveContainer width="100%" height="100%">
            <ComposedChart data={forecastData || []}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
              <XAxis dataKey="date" stroke="#94a3b8" fontSize={11} tickLine={false} tickFormatter={(d) => formatDate(d)} />
              <YAxis
                stroke="#94a3b8"
                fontSize={11}
                tickLine={false}
                tickFormatter={(val) => `$${val / 1000}k`}
              />
              <Tooltip
                formatter={(val: any) => formatCurrency(Number(val))}
                labelFormatter={(d) => formatDate(String(d))}
                contentStyle={{ backgroundColor: '#0f172a', borderColor: '#1e293b', borderRadius: '8px', color: '#fff', fontSize: '11px' }}
              />
              <Area
                type="monotone"
                dataKey="confidenceUpper"
                name="Confidence Upper (95%)"
                stroke="transparent"
                fill="#bfdbfe"
                fillOpacity={0.4}
              />
              <Area
                type="monotone"
                dataKey="confidenceLower"
                name="Confidence Lower (95%)"
                stroke="transparent"
                fill="#ffffff"
                fillOpacity={1}
              />
              <Line
                type="monotone"
                dataKey="projectedBalance"
                name="Projected Balance"
                stroke="#2563eb"
                strokeWidth={3}
                dot={{ r: 4, fill: '#2563eb' }}
              />
              <Line
                type="monotone"
                dataKey="inflow"
                name="Inflow"
                stroke="#10b981"
                strokeWidth={2}
                strokeDasharray="4 4"
              />
              <Line
                type="monotone"
                dataKey="outflow"
                name="Outflow"
                stroke="#ef4444"
                strokeWidth={2}
                strokeDasharray="4 4"
              />
            </ComposedChart>
          </ResponsiveContainer>
        </div>
      </Card>

      {/* Assumptions & Drivers */}
      <Card title="Model Drivers & Strategic Forecast Assumptions" subtitle="Transparent parameters driving the predictive runway">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs text-slate-700">
          <div className="p-3.5 rounded-xl border border-slate-200 bg-slate-50/50 space-y-2">
            <span className="font-bold text-slate-900 block">Receivables Settlement Assumption:</span>
            <p className="text-slate-600 leading-relaxed">
              Assumes 88% collection efficiency on current $130,600 outstanding invoices within 30 days based on historic counterparty payment patterns.
            </p>
          </div>

          <div className="p-3.5 rounded-xl border border-slate-200 bg-slate-50/50 space-y-2">
            <span className="font-bold text-slate-900 block">Baseline Operational Burn:</span>
            <p className="text-slate-600 leading-relaxed">
              Includes fixed recurring infrastructure ($34.5k/mo) and payroll obligations ($85k/mo), modeled with 2% quarterly inflation drift.
            </p>
          </div>
        </div>
      </Card>
    </div>
  );
};

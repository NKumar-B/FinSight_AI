import React from 'react';
import { useLocation, Link } from 'react-router-dom';
import { ChevronRight, Home } from 'lucide-react';

const routeTitleMap: Record<string, string> = {
  dashboard: 'Dashboard',
  'financial-data': 'Financial Data',
  reconciliation: 'Reconciliation',
  expenses: 'Expense Intelligence',
  invoices: 'Invoices & Receivables',
  'cash-flow': 'Cash-Flow Forecasting',
  fraud: 'Fraud Monitoring',
  investigations: 'Investigations',
  agents: 'AI Agent Activity',
  copilot: 'AI Copilot',
  reports: 'Reports & Analytics',
  audit: 'Audit History',
  users: 'User Management',
};

export const Breadcrumbs: React.FC = () => {
  const location = useLocation();
  const pathnames = location.pathname.split('/').filter((x) => x);

  if (pathnames.length === 0) return null;

  return (
    <nav className="flex items-center text-xs text-slate-500 dark:text-slate-400 font-medium" aria-label="Breadcrumb">
      <Link
        to="/dashboard"
        className="flex items-center gap-1 text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 transition-colors"
      >
        <Home className="w-3.5 h-3.5" />
      </Link>
      {pathnames.map((segment, index) => {
        const routeTo = `/${pathnames.slice(0, index + 1).join('/')}`;
        const isLast = index === pathnames.length - 1;
        const title = routeTitleMap[segment] || segment.charAt(0).toUpperCase() + segment.slice(1);

        return (
          <React.Fragment key={routeTo}>
            <ChevronRight className="w-3.5 h-3.5 text-slate-400 dark:text-slate-500 mx-1.5 shrink-0" />
            {isLast ? (
              <span className="font-semibold text-slate-800 dark:text-slate-100">{title}</span>
            ) : (
              <Link
                to={routeTo}
                className="hover:text-slate-700 dark:hover:text-slate-200 transition-colors"
              >
                {title}
              </Link>
            )}
          </React.Fragment>
        );
      })}
    </nav>
  );
};

import React from 'react';
import { Link } from 'react-router-dom';
import { ShieldX, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/Button';

export const UnauthorizedPage: React.FC = () => {
  return (
    <div className="min-h-[70vh] flex flex-col items-center justify-center text-center px-4">
      <div className="w-16 h-16 rounded-2xl bg-rose-50 dark:bg-rose-950/40 text-rose-600 dark:text-rose-400 flex items-center justify-center mb-4 ring-8 ring-rose-50/50 dark:ring-rose-950/20">
        <ShieldX className="w-8 h-8" />
      </div>
      <h1 className="text-xl font-bold text-slate-900 dark:text-white">Access Restricted</h1>
      <p className="text-xs text-slate-500 dark:text-slate-400 max-w-md mt-2 leading-relaxed">
        You do not have the required role privileges (e.g., <code className="font-mono text-brand-600 dark:text-brand-400">ROLE_ADMIN</code>) to access this administrative resource.
      </p>
      <div className="mt-6 flex items-center gap-3">
        <Link to="/dashboard">
          <Button leftIcon={<ArrowLeft className="w-4 h-4" />} size="sm">
            Return to Dashboard
          </Button>
        </Link>
      </div>
    </div>
  );
};

export const NotFoundPage: React.FC = () => {
  return (
    <div className="min-h-[70vh] flex flex-col items-center justify-center text-center px-4">
      <div className="w-16 h-16 rounded-2xl bg-slate-100 dark:bg-slate-800 text-slate-500 dark:text-slate-400 flex items-center justify-center mb-4">
        <span className="text-2xl font-black">404</span>
      </div>
      <h1 className="text-xl font-bold text-slate-900 dark:text-white">Page Not Found</h1>
      <p className="text-xs text-slate-500 dark:text-slate-400 max-w-md mt-2">
        The requested financial resource or module route does not exist.
      </p>
      <div className="mt-6">
        <Link to="/dashboard">
          <Button leftIcon={<ArrowLeft className="w-4 h-4" />} size="sm">
            Back to Overview
          </Button>
        </Link>
      </div>
    </div>
  );
};

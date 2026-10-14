import React from 'react';
import { cn } from '@/utils/formatters';

export interface CardProps extends Omit<React.HTMLAttributes<HTMLDivElement>, 'title'> {
  title?: React.ReactNode;
  subtitle?: React.ReactNode;
  action?: React.ReactNode;
  icon?: React.ReactNode;
  noPadding?: boolean;
}

export const Card: React.FC<CardProps> = ({
  className,
  title,
  subtitle,
  action,
  icon,
  noPadding = false,
  children,
  ...props
}) => {
  return (
    <div
      className={cn(
        'bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-white/10 rounded-2xl shadow-card transition-all duration-200 hover:border-slate-300 dark:hover:border-white/20',
        className
      )}
      {...props}
    >
      {(title || subtitle || action || icon) && (
        <div className="flex items-center justify-between px-6 py-4.5 border-b border-slate-100 dark:border-white/10">
          <div className="flex items-center gap-3">
            {icon && (
              <div className="p-2 bg-brand-50 dark:bg-brand-950/40 text-brand-600 dark:text-brand-400 rounded-lg shrink-0">
                {icon}
              </div>
            )}
            <div>
              {title && (
                <h3 className="text-base font-semibold text-slate-900 dark:text-white leading-tight">
                  {title}
                </h3>
              )}
              {subtitle && (
                <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5 leading-normal">
                  {subtitle}
                </p>
              )}
            </div>
          </div>
          {action && <div className="flex items-center gap-2">{action}</div>}
        </div>
      )}
      <div className={cn(noPadding ? 'p-0' : 'p-6')}>{children}</div>
    </div>
  );
};

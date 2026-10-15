import React from 'react';
import { cn } from '@/utils/formatters';

export interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  variant?: 'success' | 'warning' | 'danger' | 'info' | 'neutral';
  size?: 'sm' | 'md';
  dot?: boolean;
}

export const Badge: React.FC<BadgeProps> = ({
  className,
  variant = 'neutral',
  size = 'md',
  dot = false,
  children,
  ...props
}) => {
  const variants = {
    success: 'bg-emerald-50 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-300 border-emerald-200/80 dark:border-emerald-800/60 shadow-xs dark:shadow-[0_0_12px_rgba(16,185,129,0.18)]',
    warning: 'bg-amber-50 dark:bg-amber-950/40 text-amber-700 dark:text-amber-300 border-amber-200/80 dark:border-amber-800/60 shadow-xs dark:shadow-[0_0_12px_rgba(245,158,11,0.18)]',
    danger: 'bg-rose-50 dark:bg-rose-950/40 text-rose-700 dark:text-rose-300 border-rose-200/80 dark:border-rose-800/60 shadow-xs dark:shadow-[0_0_12px_rgba(244,63,94,0.22)]',
    info: 'bg-sky-50 dark:bg-sky-950/40 text-sky-700 dark:text-sky-300 border-sky-200/80 dark:border-sky-800/60 shadow-xs dark:shadow-[0_0_12px_rgba(56,189,248,0.18)]',
    neutral: 'bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 border-slate-200 dark:border-slate-700',
  };

  const dotColors = {
    success: 'bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.8)]',
    warning: 'bg-amber-500 shadow-[0_0_8px_rgba(245,158,11,0.8)]',
    danger: 'bg-rose-500 shadow-[0_0_8px_rgba(244,63,94,0.8)]',
    info: 'bg-sky-500 shadow-[0_0_8px_rgba(56,189,248,0.8)]',
    neutral: 'bg-slate-400',
  };

  const sizes = {
    sm: 'text-[11px] px-2 py-0.5',
    md: 'text-xs px-2.5 py-1',
  };

  return (
    <span
      className={cn(
        'inline-flex items-center font-medium rounded-full border tracking-wide uppercase transition-all duration-200 hover:scale-[1.02]',
        variants[variant],
        sizes[size],
        className
      )}
      {...props}
    >
      {dot && (
        <span
          className={cn('w-1.5 h-1.5 rounded-full mr-1.5 shrink-0 animate-pulse', dotColors[variant])}
          aria-hidden="true"
        />
      )}
      {children}
    </span>
  );
};

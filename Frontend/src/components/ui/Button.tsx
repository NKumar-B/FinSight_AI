import React from 'react';
import { Loader2 } from 'lucide-react';
import { cn } from '@/utils/formatters';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'outline' | 'danger' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  isLoading?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      className,
      variant = 'primary',
      size = 'md',
      isLoading = false,
      leftIcon,
      rightIcon,
      children,
      disabled,
      ...props
    },
    ref
  ) => {
    const baseStyles = 'inline-flex items-center justify-center font-medium rounded-lg transition-all focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed select-none';

    const variants = {
      primary: 'bg-gradient-to-r from-brand-600 to-indigo-600 hover:from-brand-500 hover:to-indigo-500 text-white shadow-md shadow-brand-500/20 hover:shadow-lg hover:shadow-brand-500/35 hover:-translate-y-0.5 active:translate-y-0 active:shadow-sm focus:ring-brand-500 transition-all duration-200',
      secondary: 'bg-navy-900 hover:bg-navy-800 text-white shadow-sm hover:shadow-md hover:shadow-navy-900/30 hover:-translate-y-0.5 active:translate-y-0 focus:ring-navy-700 active:bg-navy-950 transition-all duration-200',
      outline: 'border border-slate-300 dark:border-slate-700 bg-white/90 dark:bg-slate-800/80 hover:bg-brand-50/60 dark:hover:bg-brand-950/40 hover:border-brand-400 dark:hover:border-brand-500 text-slate-700 dark:text-slate-200 hover:text-brand-700 dark:hover:text-brand-300 shadow-xs hover:shadow-md hover:shadow-brand-500/10 hover:-translate-y-0.5 active:translate-y-0 focus:ring-brand-500 transition-all duration-200',
      danger: 'bg-red-600 bg-gradient-to-r from-rose-600 to-red-600 hover:from-rose-500 hover:to-red-500 text-white shadow-md shadow-rose-500/20 hover:shadow-lg hover:shadow-rose-500/35 hover:-translate-y-0.5 active:translate-y-0 focus:ring-rose-500 transition-all duration-200',
      ghost: 'bg-transparent hover:bg-slate-100/80 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-200 hover:text-slate-900 dark:hover:text-white focus:ring-slate-400 active:scale-95 transition-all duration-150',
    };

    const sizes = {
      sm: 'text-xs px-2.5 py-1.5 gap-1.5',
      md: 'text-sm px-4 py-2 gap-2',
      lg: 'text-base px-5 py-2.5 gap-2.5',
    };

    return (
      <button
        ref={ref}
        disabled={disabled || isLoading}
        className={cn(baseStyles, variants[variant], sizes[size], className)}
        {...props}
      >
        {isLoading && <Loader2 className="w-4 h-4 animate-spin shrink-0" />}
        {!isLoading && leftIcon && <span className="shrink-0">{leftIcon}</span>}
        <span>{children}</span>
        {!isLoading && rightIcon && <span className="shrink-0">{rightIcon}</span>}
      </button>
    );
  }
);

Button.displayName = 'Button';

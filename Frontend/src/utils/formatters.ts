import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function getCurrencySymbol(currency?: string): string {
  const code = (
    currency ||
    (typeof window !== 'undefined' ? localStorage.getItem('finsight_currency') || 'USD' : 'USD')
  ).toUpperCase();

  switch (code) {
    case 'INR':
      return '₹';
    case 'EUR':
      return '€';
    case 'GBP':
      return '£';
    case 'SGD':
      return 'S$';
    case 'JPY':
      return '¥';
    case 'USD':
    default:
      return '$';
  }
}

export function formatCurrency(amount: number, currency?: string): string {
  const activeCurrency = (
    currency ||
    (typeof window !== 'undefined' ? localStorage.getItem('finsight_currency') || 'USD' : 'USD')
  ).toUpperCase();

  try {
    const locale = activeCurrency === 'INR' ? 'en-IN' : 'en-US';
    return new Intl.NumberFormat(locale, {
      style: 'currency',
      currency: activeCurrency,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount);
  } catch {
    const symbol = getCurrencySymbol(activeCurrency);
    return `${symbol}${amount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }
}

export function formatDate(dateStr?: string): string {
  if (!dateStr) return '—';
  try {
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  } catch {
    return dateStr;
  }
}

export function formatDateTime(dateStr?: string): string {
  if (!dateStr) return '—';
  try {
    const d = new Date(dateStr);
    return d.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  } catch {
    return dateStr;
  }
}

export function formatPercentage(value: number, decimals = 1): string {
  return `${value.toFixed(decimals)}%`;
}

export function getStatusBadgeVariant(status: string): 'success' | 'warning' | 'danger' | 'info' | 'neutral' {
  switch (status?.toUpperCase()) {
    case 'CLEARED':
    case 'COMPLETED':
    case 'ACTIVE':
    case 'PAID':
    case 'RESOLVED':
    case 'SUCCESS':
    case 'EXACT':
      return 'success';
    case 'PENDING':
    case 'IN_PROGRESS':
    case 'UNDER_REVIEW':
    case 'INVESTIGATING':
    case 'TOLERANCE':
    case 'AWAITING_APPROVAL':
      return 'warning';
    case 'FLAGGED':
    case 'CONFIRMED_FRAUD':
    case 'FAILED':
    case 'OVERDUE':
    case 'UNMATCHED':
    case 'CRITICAL':
    case 'HIGH':
    case 'DISPUTED':
      return 'danger';
    case 'RUNNING':
    case 'QUEUED':
      return 'info';
    default:
      return 'neutral';
  }
}

export function getSeverityBadgeVariant(severity: string): 'success' | 'warning' | 'danger' | 'info' | 'neutral' {
  switch (severity?.toUpperCase()) {
    case 'CRITICAL':
    case 'HIGH':
      return 'danger';
    case 'MEDIUM':
      return 'warning';
    case 'LOW':
      return 'info';
    default:
      return 'neutral';
  }
}

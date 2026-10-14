import { describe, it, expect } from 'vitest';
import {
  formatCurrency,
  formatPercentage,
  getStatusBadgeVariant,
  getSeverityBadgeVariant,
} from './formatters';

describe('Formatters Utility', () => {
  it('formats USD currency correctly', () => {
    expect(formatCurrency(1485230.5, 'USD')).toContain('1,485,230.50');
    expect(formatCurrency(0, 'USD')).toContain('0.00');
  });

  it('formats percentage correctly', () => {
    expect(formatPercentage(99.51)).toBe('99.5%');
    expect(formatPercentage(99.51, 2)).toBe('99.51%');
  });

  it('maps financial status to correct badge variant', () => {
    expect(getStatusBadgeVariant('CLEARED')).toBe('success');
    expect(getStatusBadgeVariant('PENDING')).toBe('warning');
    expect(getStatusBadgeVariant('CONFIRMED_FRAUD')).toBe('danger');
    expect(getStatusBadgeVariant('QUEUED')).toBe('info');
    expect(getStatusBadgeVariant('UNKNOWN')).toBe('neutral');
  });

  it('maps severity to correct badge variant', () => {
    expect(getSeverityBadgeVariant('CRITICAL')).toBe('danger');
    expect(getSeverityBadgeVariant('HIGH')).toBe('danger');
    expect(getSeverityBadgeVariant('MEDIUM')).toBe('warning');
    expect(getSeverityBadgeVariant('LOW')).toBe('info');
  });
});

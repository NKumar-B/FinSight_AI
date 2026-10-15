import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { ReportsPage } from './ReportsPage';
import { generateSecureReport } from '../utils/reportExporter';

// Mock reportExporter module
vi.mock('../utils/reportExporter', () => ({
  generateSecureReport: vi.fn(async (options: any) => ({
    filename: `${options.reportName}_${options.reportId}.${options.format.toLowerCase()}`,
    size: 4096,
  })),
}));

describe('ReportsPage & Secure Export Suite (PDF & Excel)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderReportsPage = () =>
    render(
      <BrowserRouter>
        <ReportsPage />
      </BrowserRouter>
    );

  it('renders reports catalog with all 4 enterprise reports without any CSV option', () => {
    renderReportsPage();

    expect(screen.getByText('Reports & Compliance Exports')).toBeInTheDocument();
    expect(screen.getByText('Executive Daily Reconciliation Summary')).toBeInTheDocument();
    expect(screen.getByText('AML Surveillance & Critical Alert Dossier')).toBeInTheDocument();
    expect(screen.getByText('Receivables Aging & Cash-Flow Runway Forecast')).toBeInTheDocument();
    expect(screen.getByText('SOC-2 Immutable Audit Log & Hash Export')).toBeInTheDocument();

    // Verify CSV is removed and replaced by XLSX
    expect(screen.queryByText('CSV')).not.toBeInTheDocument();
    expect(screen.getAllByText('XLSX').length).toBe(2);
    expect(screen.getAllByText('PDF').length).toBe(2);
  });

  it('provides Secure Export for all reports in the catalog', () => {
    renderReportsPage();

    const secureExportBtns = screen.getAllByRole('button', { name: /secure export/i });
    expect(secureExportBtns.length).toBe(4);
  });

  it('opens Secure PDF Export Modal when clicking Secure Export on a PDF report and downloads encrypted PDF', async () => {
    renderReportsPage();

    const secureExportBtns = screen.getAllByRole('button', { name: /secure export/i });
    // Click Secure Export on RPT-001 (PDF)
    fireEvent.click(secureExportBtns[0]);

    await waitFor(() => {
      expect(screen.getByText('Secure PDF Export & Cryptographic Encryption')).toBeInTheDocument();
      expect(screen.getByText('Password Protection & File Encryption')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /download encrypted pdf/i })).toBeInTheDocument();
    });

    const downloadBtn = screen.getByRole('button', { name: /download encrypted pdf/i });
    fireEvent.click(downloadBtn);

    await waitFor(() => {
      expect(generateSecureReport).toHaveBeenCalledWith(
        expect.objectContaining({
          reportId: 'RPT-001',
          format: 'PDF',
          password: 'FinSight@2026',
        })
      );
    });
  });

  it('opens Secure Excel Export Modal when clicking Secure Export on an XLSX report and downloads encrypted Excel', async () => {
    renderReportsPage();

    const secureExportBtns = screen.getAllByRole('button', { name: /secure export/i });
    // Click Secure Export on RPT-003 (XLSX)
    fireEvent.click(secureExportBtns[2]);

    await waitFor(() => {
      expect(screen.getByText('Secure Excel Export & OOXML Encryption')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /download encrypted excel/i })).toBeInTheDocument();
    });

    const downloadBtn = screen.getByRole('button', { name: /download encrypted excel/i });
    fireEvent.click(downloadBtn);

    await waitFor(() => {
      expect(generateSecureReport).toHaveBeenCalledWith(
        expect.objectContaining({
          reportId: 'RPT-003',
          format: 'XLSX',
          password: 'FinSight@2026',
        })
      );
    });
  });
});


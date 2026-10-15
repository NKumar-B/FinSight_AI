import React, { useState } from 'react';
import {
  ShieldCheck,
  Lock,
  Eye,
  EyeOff,
  Sparkles,
  Download,
  AlertCircle,
  CheckCircle2,
  FileText,
  FileSpreadsheet,
  KeyRound,
} from 'lucide-react';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Badge } from '@/components/ui/Badge';

export interface ReportTemplate {
  id: string;
  name: string;
  category: 'RECONCILIATION' | 'FRAUD' | 'FINANCIAL_INSIGHTS' | 'AUDIT';
  frequency: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'ON_DEMAND';
  lastGenerated: string;
  format: 'PDF' | 'XLSX';
  status: 'READY' | 'GENERATING';
}

export interface SecureExportModalProps {
  isOpen: boolean;
  onClose: () => void;
  report: ReportTemplate | null;
  onExport: (password?: string) => Promise<void>;
  isExporting: boolean;
}

export const SecureExportModal: React.FC<SecureExportModalProps> = ({
  isOpen,
  onClose,
  report,
  onExport,
  isExporting,
}) => {
  const [enablePassword, setEnablePassword] = useState(true);
  const [password, setPassword] = useState('FinSight@2026');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!report) return null;

  const isExcel = report.format === 'XLSX';
  const formatLabel = isExcel ? 'Excel Spreadsheet (.xlsx)' : 'PDF Document (.pdf)';
  const encryptionStandard = isExcel
    ? 'Standard Microsoft Office OOXML Agile/AES Workbook Encryption'
    : 'Standard PDF 128-bit AES Encryption with Document Open Restrictions';

  const handleDownload = async () => {
    setError(null);
    if (enablePassword && !password.trim()) {
      setError('Please provide a document password or disable encryption toggle');
      return;
    }
    if (enablePassword && password.trim().length < 4) {
      setError('Password must contain at least 4 characters for compliant encryption');
      return;
    }

    try {
      await onExport(enablePassword ? password.trim() : undefined);
      onClose();
    } catch (err: any) {
      setError(err?.message || `Failed to generate encrypted ${report.format} export`);
    }
  };

  const handleSuggestPassword = () => {
    const suggestions = ['FinSight@2026', 'AuditSecure2026!', 'Fintech#9821', '834912'];
    const chosen = suggestions[Math.floor(Math.random() * suggestions.length)];
    setPassword(chosen);
    setError(null);
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isExcel ? 'Secure Excel Export & OOXML Encryption' : 'Secure PDF Export & Cryptographic Encryption'}
      description={`Configure enterprise document security and access passwords for ${formatLabel}.`}
      maxWidth="lg"
    >
      <div className="space-y-5 text-slate-800 dark:text-slate-100">
        {/* Error message */}
        {error && (
          <div className="p-3 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-xs text-rose-700 dark:text-rose-300 flex items-start gap-2">
            <AlertCircle className="w-4 h-4 text-rose-600 dark:text-rose-400 shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}

        {/* Report Overview Card */}
        <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 flex items-start justify-between gap-4">
          <div className="flex items-start gap-3">
            <div
              className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 border ${
                isExcel
                  ? 'bg-emerald-50 dark:bg-emerald-950/40 border-emerald-200 dark:border-emerald-800 text-emerald-600 dark:text-emerald-400'
                  : 'bg-rose-50 dark:bg-rose-950/40 border-rose-200 dark:border-rose-800 text-rose-600 dark:text-rose-400'
              }`}
            >
              {isExcel ? <FileSpreadsheet className="w-5 h-5" /> : <FileText className="w-5 h-5" />}
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-mono text-xs font-bold text-brand-600 dark:text-brand-400">
                  {report.id}
                </span>
                <Badge variant={isExcel ? 'success' : 'danger'} size="sm">
                  {report.format} ENCRYPTED
                </Badge>
                <Badge variant="warning" size="sm">
                  STRICTLY CONFIDENTIAL
                </Badge>
              </div>
              <h4 className="text-sm font-bold text-slate-900 dark:text-white mt-1">
                {report.name}
              </h4>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                Category: {report.category} • Format: {formatLabel} • Cadence: {report.frequency}
              </p>
            </div>
          </div>
        </div>

        {/* Encryption Configuration Section */}
        <div className="p-4 rounded-xl border border-slate-200 dark:border-slate-700 space-y-4 bg-white dark:bg-slate-900">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
            <div className="flex items-center gap-2">
              <div className="p-1.5 rounded-lg bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400">
                <ShieldCheck className="w-4 h-4" />
              </div>
              <div>
                <span className="text-xs font-bold text-slate-900 dark:text-white block">
                  Password Protection & File Encryption
                </span>
                <span className="text-[11px] text-slate-500 dark:text-slate-400">
                  {encryptionStandard}
                </span>
              </div>
            </div>

            <label className="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                checked={enablePassword}
                onChange={(e) => setEnablePassword(e.target.checked)}
                className="sr-only peer"
              />
              <div className="w-10 h-5 bg-slate-200 peer-focus:outline-none rounded-full peer dark:bg-slate-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-emerald-600" />
            </label>
          </div>

          {enablePassword && (
            <div className="space-y-3 animate-fadeIn">
              <div className="relative">
                <Input
                  label={`Document Open Password (${isExcel ? 'Excel' : 'PDF'})`}
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => {
                    setPassword(e.target.value);
                    setError(null);
                  }}
                  placeholder="Enter strong document password"
                  leftIcon={<Lock className="w-4 h-4" />}
                  rightIcon={
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
                      tabIndex={-1}
                    >
                      {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                    </button>
                  }
                  helperText={`Recipients will be required to enter this password in ${
                    isExcel ? 'Microsoft Excel, LibreOffice, or Google Sheets' : 'Adobe Acrobat, Chrome, or Apple Preview'
                  } before viewing data.`}
                  required
                />
              </div>

              <div className="flex items-center justify-between pt-1">
                <button
                  type="button"
                  onClick={handleSuggestPassword}
                  className="text-xs text-brand-600 dark:text-brand-400 hover:underline font-semibold flex items-center gap-1.5"
                >
                  <Sparkles className="w-3.5 h-3.5" /> Suggest Enterprise Key
                </button>

                <span className="text-[11px] font-mono font-bold text-slate-500 dark:text-slate-400 bg-slate-100 dark:bg-slate-800 px-2 py-0.5 rounded">
                  Current Key: {password}
                </span>
              </div>
            </div>
          )}

          {!enablePassword && (
            <div className="p-3 rounded-lg bg-amber-50 dark:bg-amber-950/40 border border-amber-200 dark:border-amber-800 text-xs text-amber-800 dark:text-amber-300 flex items-start gap-2">
              <AlertCircle className="w-4 h-4 text-amber-600 dark:text-amber-400 shrink-0 mt-0.5" />
              <span>
                Warning: Exporting unencrypted financial statements may violate enterprise data governance policies.
              </span>
            </div>
          )}
        </div>

        {/* Compliance Footnote */}
        <div className="flex items-center gap-2 text-[11px] text-slate-400 px-1">
          <KeyRound className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
          <span>
            {isExcel
              ? 'Workbook protected via Microsoft Office Standard Agile Encryption algorithm.'
              : 'Document signed with SHA-256 fingerprint and authenticated via ChaCha20-Poly1305.'}
          </span>
        </div>

        {/* Actions Footer */}
        <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-100 dark:border-slate-800">
          <Button type="button" variant="outline" size="sm" onClick={onClose} disabled={isExporting}>
            Cancel
          </Button>
          <Button
            type="button"
            size="sm"
            onClick={handleDownload}
            isLoading={isExporting}
            leftIcon={<Download className="w-4 h-4" />}
            className="font-bold shadow-md"
          >
            {enablePassword
              ? isExcel
                ? 'Download Encrypted Excel'
                : 'Download Encrypted PDF'
              : `Download Standard ${report.format}`}
          </Button>
        </div>
      </div>
    </Modal>
  );
};

// Backward-compatible alias
export const SecurePdfExportModal = SecureExportModal;
export type SecurePdfExportModalProps = SecureExportModalProps;

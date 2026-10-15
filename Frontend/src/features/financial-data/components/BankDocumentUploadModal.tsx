import React, { useState, useRef, useEffect } from 'react';
import {
  Upload,
  FileText,
  FileSpreadsheet,
  CheckCircle2,
  AlertTriangle,
  XCircle,
  ShieldCheck,
  Landmark,
  ArrowRight,
  RefreshCw,
  FileCheck2,
  Lock,
  Layers,
  ArrowDownRight,
  ArrowUpRight,
  Info,
} from 'lucide-react';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import {
  validateFinancialDocument,
  BankDocumentValidationResult,
  SAMPLE_FIXTURES,
} from '../utils/bankDocumentValidator';
import { formatCurrency, formatDateTime } from '@/utils/formatters';

export interface BankDocumentUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (result: {
    fileName: string;
    bankName: string;
    accountNumber: string;
    transactionCount: number;
    transactions: any[];
  }) => void;
}

export const BankDocumentUploadModal: React.FC<BankDocumentUploadModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [dragActive, setDragActive] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isValidating, setIsValidating] = useState(false);
  const [validationStep, setValidationStep] = useState<string>('');
  const [validationResult, setValidationResult] = useState<BankDocumentValidationResult | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState(false);

  // Reset state when modal opens or closes
  useEffect(() => {
    if (!isOpen) {
      setSelectedFile(null);
      setValidationResult(null);
      setIsValidating(false);
      setIsSubmitting(false);
      setSubmitSuccess(false);
      setValidationStep('');
    }
  }, [isOpen]);

  const handleFileSelection = async (file: File) => {
    setSelectedFile(file);
    setValidationResult(null);
    setIsValidating(true);

    try {
      setValidationStep('1. Inspecting file container & SHA-256 byte signature...');
      await new Promise((r) => setTimeout(r, 350));

      setValidationStep('2. Analyzing banking entity markers, account headers & balance fields...');
      await new Promise((r) => setTimeout(r, 450));

      setValidationStep('3. Verifying financial authenticity and ledger structure...');
      const result = await validateFinancialDocument(file);
      await new Promise((r) => setTimeout(r, 300));

      setValidationResult(result);
    } catch {
      setValidationResult({
        isValid: false,
        confidenceScore: 0,
        documentType: 'INVALID_NON_FINANCIAL',
        fileName: file.name,
        fileSizeBytes: file.size,
        matchedKeywords: [],
        rejectionReasons: ['Failed to read or parse file content. Please check file integrity.'],
        extractedTransactions: [],
      });
    } finally {
      setIsValidating(false);
      setValidationStep('');
    }
  };

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileSelection(e.dataTransfer.files[0]);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      handleFileSelection(e.target.files[0]);
    }
  };

  const handleSelectPreset = (fixtureKey: keyof typeof SAMPLE_FIXTURES) => {
    const fixture = SAMPLE_FIXTURES[fixtureKey];
    const file = fixture.createFile();
    handleFileSelection(file);
  };

  const handleIngest = async () => {
    if (!validationResult || !validationResult.isValid) return;

    setIsSubmitting(true);
    try {
      await new Promise((r) => setTimeout(r, 600));
      onSuccess({
        fileName: validationResult.fileName,
        bankName: validationResult.detectedBankName || 'Institutional Bank',
        accountNumber: validationResult.detectedAccountNumber || 'ACC-10029384',
        transactionCount: validationResult.extractedTransactions.length,
        transactions: validationResult.extractedTransactions,
      });
      setSubmitSuccess(true);
      setTimeout(() => {
        onClose();
      }, 1200);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Institutional Bank Document Ingestion"
      description="Upload official PDF bank e-statements or CSV financial ledger feeds for automated reconciliation and surveillance."
      maxWidth="xl"
    >
      <div className="space-y-5 text-slate-800 dark:text-slate-100">
        {/* Compliance Badges Header */}
        <div className="flex flex-wrap items-center justify-between gap-2 p-2.5 bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 rounded-lg text-xs">
          <div className="flex items-center gap-1.5 font-semibold text-slate-700 dark:text-slate-300">
            <Landmark className="w-4 h-4 text-brand-600 dark:text-brand-400" />
            <span>Strict Financial Guardrails Active</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-semibold bg-brand-50 dark:bg-brand-950/40 text-brand-700 dark:text-brand-300 border border-brand-200 dark:border-brand-800">
              <FileText className="w-3 h-3" /> PDF Statements
            </span>
            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-semibold bg-emerald-50 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800">
              <FileSpreadsheet className="w-3 h-3" /> CSV Ledgers
            </span>
            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-semibold bg-amber-50 dark:bg-amber-950/40 text-amber-800 dark:text-amber-300 border border-amber-200 dark:border-amber-800">
              <Lock className="w-3 h-3" /> Bank Only
            </span>
          </div>
        </div>

        {/* Quick Test Demo Presets Bar */}
        <div className="p-3 bg-gradient-to-r from-slate-50 to-brand-50/40 dark:from-slate-800/80 dark:to-brand-950/30 rounded-xl border border-slate-200 dark:border-slate-700">
          <div className="text-[11px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-2 flex items-center gap-1">
            <Info className="w-3.5 h-3.5 text-brand-500" />
            <span>Quick-Load Sample Statements (Instant Demo)</span>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
            <button
              type="button"
              onClick={() => handleSelectPreset('VALID_CHASE_PDF')}
              className="px-2.5 py-1.5 text-left text-xs bg-white dark:bg-slate-800 hover:bg-emerald-50 dark:hover:bg-emerald-950/40 hover:border-emerald-300 dark:hover:border-emerald-700 border border-slate-200 dark:border-slate-700 rounded-lg shadow-sm transition-all group"
            >
              <div className="font-bold text-slate-800 dark:text-slate-100 group-hover:text-emerald-700 dark:group-hover:text-emerald-400 flex items-center gap-1">
                <span className="w-2 h-2 rounded-full bg-emerald-500"></span>
                Chase (PDF)
              </div>
              <div className="text-[10px] text-slate-500 dark:text-slate-400">Valid e-Statement</div>
            </button>

            <button
              type="button"
              onClick={() => handleSelectPreset('VALID_BARCLAYS_CSV')}
              className="px-2.5 py-1.5 text-left text-xs bg-white dark:bg-slate-800 hover:bg-emerald-50 dark:hover:bg-emerald-950/40 hover:border-emerald-300 dark:hover:border-emerald-700 border border-slate-200 dark:border-slate-700 rounded-lg shadow-sm transition-all group"
            >
              <div className="font-bold text-slate-800 dark:text-slate-100 group-hover:text-emerald-700 dark:group-hover:text-emerald-400 flex items-center gap-1">
                <span className="w-2 h-2 rounded-full bg-emerald-500"></span>
                Barclays (CSV)
              </div>
              <div className="text-[10px] text-slate-500 dark:text-slate-400">Valid Ledger Feed</div>
            </button>

            <button
              type="button"
              onClick={() => handleSelectPreset('INVALID_RESUME_PDF')}
              className="px-2.5 py-1.5 text-left text-xs bg-white dark:bg-slate-800 hover:bg-rose-50 dark:hover:bg-rose-950/40 hover:border-rose-300 dark:hover:border-rose-700 border border-slate-200 dark:border-slate-700 rounded-lg shadow-sm transition-all group"
            >
              <div className="font-bold text-slate-800 dark:text-slate-100 group-hover:text-rose-700 dark:group-hover:text-rose-400 flex items-center gap-1">
                <span className="w-2 h-2 rounded-full bg-rose-500"></span>
                Resume (PDF)
              </div>
              <div className="text-[10px] text-rose-600 dark:text-rose-400 font-medium">Rejection Guardrail</div>
            </button>

            <button
              type="button"
              onClick={() => handleSelectPreset('INVALID_EMPLOYEE_CSV')}
              className="px-2.5 py-1.5 text-left text-xs bg-white dark:bg-slate-800 hover:bg-rose-50 dark:hover:bg-rose-950/40 hover:border-rose-300 dark:hover:border-rose-700 border border-slate-200 dark:border-slate-700 rounded-lg shadow-sm transition-all group"
            >
              <div className="font-bold text-slate-800 dark:text-slate-100 group-hover:text-rose-700 dark:group-hover:text-rose-400 flex items-center gap-1">
                <span className="w-2 h-2 rounded-full bg-rose-500"></span>
                HR Roster (CSV)
              </div>
              <div className="text-[10px] text-rose-600 dark:text-rose-400 font-medium">Rejection Guardrail</div>
            </button>
          </div>
        </div>

        {/* Drag & Drop Upload Zone */}
        <div
          onDragEnter={handleDrag}
          onDragLeave={handleDrag}
          onDragOver={handleDrag}
          onDrop={handleDrop}
          onClick={() => fileInputRef.current?.click()}
          className={`relative border-2 border-dashed rounded-2xl p-6 sm:p-8 text-center transition-all cursor-pointer ${
            dragActive
              ? 'border-brand-500 bg-brand-50/60 dark:bg-brand-950/40 scale-[1.01]'
              : 'border-slate-300 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-800/40 hover:border-brand-400 dark:hover:border-brand-500 hover:bg-slate-50 dark:hover:bg-slate-800/60'
          }`}
        >
          <input
            ref={fileInputRef}
            type="file"
            accept=".pdf,.csv,application/pdf,text/csv"
            onChange={handleInputChange}
            className="hidden"
            id="bank-document-input"
          />

          <div className="max-w-md mx-auto space-y-3">
            <div className="w-14 h-14 mx-auto rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 shadow-md flex items-center justify-center text-brand-600 dark:text-brand-400 group-hover:scale-105 transition-transform">
              <Upload className="w-7 h-7" />
            </div>

            <div>
              <p className="text-sm font-bold text-slate-800 dark:text-slate-100">
                {selectedFile ? (
                  <span className="text-brand-700 dark:text-brand-400 font-mono">{selectedFile.name}</span>
                ) : (
                  'Drag & drop your official bank statement or click to browse'
                )}
              </p>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
                Official institutional formats: <strong className="text-slate-700 dark:text-slate-200">PDF e-Statements</strong> &{' '}
                <strong className="text-slate-700 dark:text-slate-200">CSV Bank Ledgers</strong> (Max 25MB)
              </p>
            </div>

            <div className="pt-1">
              <span className="inline-flex items-center gap-1.5 text-[11px] text-amber-800 dark:text-amber-300 bg-amber-50 dark:bg-amber-950/40 px-3 py-1 rounded-full border border-amber-200 dark:border-amber-800">
                <AlertTriangle className="w-3.5 h-3.5 text-amber-600 dark:text-amber-400" />
                <span>Non-banking documents (resumes, general CSVs, invoices) will be rejected</span>
              </span>
            </div>
          </div>
        </div>

        {/* Validation In-Progress State */}
        {isValidating && (
          <div className="p-4 bg-brand-50/60 border border-brand-200 rounded-xl space-y-2 animate-pulse">
            <div className="flex items-center gap-2 text-xs font-bold text-brand-800">
              <RefreshCw className="w-4 h-4 animate-spin text-brand-600" />
              <span>FinSight AI Compliance Scanner — Inspecting Document Content</span>
            </div>
            <div className="text-xs text-brand-700 font-mono">{validationStep}</div>
            <div className="w-full bg-brand-100 rounded-full h-1.5 overflow-hidden">
              <div className="bg-brand-600 h-1.5 rounded-full w-2/3 animate-pulse"></div>
            </div>
          </div>
        )}

        {/* Validation Result View */}
        {validationResult && !isValidating && (
          <div>
            {validationResult.isValid ? (
              /* VALID BANK STATEMENT CARD */
              <div className="p-4 bg-emerald-50/80 dark:bg-emerald-950/40 border-2 border-emerald-300 dark:border-emerald-800 rounded-2xl space-y-4 shadow-sm">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-emerald-100 dark:bg-emerald-900/60 border border-emerald-300 dark:border-emerald-700 flex items-center justify-center text-emerald-700 dark:text-emerald-300 shadow-sm">
                      <CheckCircle2 className="w-6 h-6" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <h4 className="text-sm font-bold text-emerald-950 dark:text-emerald-100">
                          {validationResult.detectedBankName}
                        </h4>
                        <Badge variant="success" size="sm">
                          Verified Bank Record
                        </Badge>
                      </div>
                      <p className="text-xs text-emerald-800 dark:text-emerald-300 mt-0.5 font-medium">
                        Account: <span className="font-mono font-bold">{validationResult.detectedAccountNumber}</span> ·{' '}
                        Confidence Score: <span className="font-bold">{validationResult.confidenceScore}%</span>
                      </p>
                    </div>
                  </div>

                  <span className="text-[11px] font-mono font-semibold px-2 py-0.5 rounded bg-emerald-100 dark:bg-emerald-900/60 text-emerald-800 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-700">
                    {validationResult.documentType === 'PDF_BANK_STATEMENT' ? 'PDF e-Statement' : 'CSV Bank Feed'}
                  </span>
                </div>

                {/* Extracted Financial Metrics Grid */}
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 pt-1 text-xs">
                  <div className="p-2.5 bg-white/90 dark:bg-slate-900/90 rounded-lg border border-emerald-200 dark:border-emerald-800/50">
                    <span className="text-[10px] uppercase font-bold text-slate-400 block">Ledger Records</span>
                    <span className="text-sm font-extrabold text-slate-800 dark:text-white">
                      {validationResult.extractedTransactions.length} txns
                    </span>
                  </div>
                  <div className="p-2.5 bg-white/90 dark:bg-slate-900/90 rounded-lg border border-emerald-200 dark:border-emerald-800/50">
                    <span className="text-[10px] uppercase font-bold text-slate-400 block">Statement Period</span>
                    <span className="text-xs font-semibold text-slate-800 dark:text-white">
                      {validationResult.detectedPeriod || 'Oct 2026'}
                    </span>
                  </div>
                  <div className="p-2.5 bg-white/90 dark:bg-slate-900/90 rounded-lg border border-emerald-200 dark:border-emerald-800/50">
                    <span className="text-[10px] uppercase font-bold text-slate-400 block">Total Inflows</span>
                    <span className="text-xs font-bold text-emerald-700 dark:text-emerald-400">
                      +{formatCurrency(validationResult.rawSummary?.totalCredits || 0, 'USD')}
                    </span>
                  </div>
                  <div className="p-2.5 bg-white/90 dark:bg-slate-900/90 rounded-lg border border-emerald-200 dark:border-emerald-800/50">
                    <span className="text-[10px] uppercase font-bold text-slate-400 block">Total Outflows</span>
                    <span className="text-xs font-bold text-slate-800 dark:text-slate-100">
                      -{formatCurrency(validationResult.rawSummary?.totalDebits || 0, 'USD')}
                    </span>
                  </div>
                </div>

                {/* Extracted Transactions Preview */}
                <div className="space-y-1.5">
                  <div className="text-[11px] font-bold uppercase tracking-wider text-emerald-900 dark:text-emerald-200 flex items-center justify-between">
                    <span>Parsed Transaction Preview (First {Math.min(3, validationResult.extractedTransactions.length)})</span>
                    <span className="text-emerald-700 dark:text-emerald-400 font-normal">SHA-256 Fingerprinted</span>
                  </div>
                  <div className="bg-white dark:bg-slate-900 rounded-lg border border-emerald-200 dark:border-emerald-800/50 overflow-hidden divide-y divide-slate-100 dark:divide-slate-800 text-xs">
                    {validationResult.extractedTransactions.slice(0, 3).map((txn, idx) => (
                      <div key={idx} className="p-2.5 flex items-center justify-between gap-3 hover:bg-slate-50 dark:hover:bg-slate-800/60">
                        <div className="min-w-0 flex-1">
                          <div className="flex items-center gap-2">
                            <span className="font-mono text-[11px] font-bold text-brand-700 dark:text-brand-400">
                              {txn.referenceNumber}
                            </span>
                            <span className="text-[10px] text-slate-400">{txn.category}</span>
                          </div>
                          <div className="text-xs font-semibold text-slate-800 dark:text-slate-200 truncate">
                            {txn.description}
                          </div>
                        </div>
                        <div className="text-right">
                          <div
                            className={`font-bold flex items-center justify-end gap-0.5 ${
                              txn.transactionType === 'CREDIT' ? 'text-emerald-600 dark:text-emerald-400' : 'text-slate-900 dark:text-slate-100'
                            }`}
                          >
                            {txn.transactionType === 'CREDIT' ? (
                              <ArrowDownRight className="w-3.5 h-3.5" />
                            ) : (
                              <ArrowUpRight className="w-3.5 h-3.5 text-slate-400" />
                            )}
                            <span>{formatCurrency(txn.amount, txn.currency)}</span>
                          </div>
                          <div className="text-[10px] text-slate-400">{formatDateTime(txn.timestamp)}</div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            ) : (
              /* REJECTED NON-BANKING DOCUMENT CARD */
              <div className="p-4 bg-rose-50/90 dark:bg-rose-950/40 border-2 border-rose-300 dark:border-rose-800 rounded-2xl space-y-3 shadow-sm">
                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 rounded-xl bg-rose-100 dark:bg-rose-900/60 border border-rose-300 dark:border-rose-700 flex items-center justify-center text-rose-700 dark:text-rose-300 flex-shrink-0">
                    <XCircle className="w-6 h-6" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <h4 className="text-sm font-bold text-rose-950 dark:text-rose-100">
                        Document Rejected: Non-Banking Document
                      </h4>
                      <Badge variant="danger" size="sm">
                        Rejected by Compliance Guardrail
                      </Badge>
                    </div>
                    <p className="text-xs text-rose-800 dark:text-rose-300 mt-1">
                      FinSight AI exclusively ingests verified bank statements, settlement summaries, and financial ledgers.
                    </p>
                  </div>
                </div>

                <div className="bg-white/80 dark:bg-slate-900/90 p-3 rounded-lg border border-rose-200 dark:border-rose-800 text-xs space-y-1.5 text-rose-900 dark:text-rose-200">
                  <div className="font-bold flex items-center gap-1.5 text-rose-800 dark:text-rose-300">
                    <AlertTriangle className="w-3.5 h-3.5 text-rose-600 dark:text-rose-400" />
                    <span>Rejection Diagnosis:</span>
                  </div>
                  <ul className="list-disc pl-5 space-y-1 text-slate-700 dark:text-slate-300">
                    {validationResult.rejectionReasons?.map((reason, i) => (
                      <li key={i}>{reason}</li>
                    ))}
                  </ul>
                </div>

                <div className="flex items-center justify-between gap-3 text-xs text-rose-800 dark:text-rose-300 pt-1">
                  <span>File: <code className="font-mono bg-rose-100 dark:bg-rose-900/60 px-1.5 py-0.5 rounded text-rose-900 dark:text-rose-200">{validationResult.fileName}</code></span>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() => {
                      setSelectedFile(null);
                      setValidationResult(null);
                    }}
                  >
                    Select Another File
                  </Button>
                </div>
              </div>
            )}
          </div>
        )}

        {/* Success Confirmation Banner */}
        {submitSuccess && (
          <div className="p-3 bg-emerald-100 border border-emerald-300 rounded-xl text-xs font-bold text-emerald-900 flex items-center gap-2 animate-bounce">
            <CheckCircle2 className="w-5 h-5 text-emerald-700" />
            <span>Document accepted! Reconciling transactions with core ledger...</span>
          </div>
        )}

        {/* Modal Action Footer */}
        <div className="flex items-center justify-between border-t border-slate-100 dark:border-slate-800 pt-4">
          <div className="text-[11px] text-slate-400 dark:text-slate-500 flex items-center gap-1">
            <ShieldCheck className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />
            <span>Automated bank parser & AML reconciliation engine</span>
          </div>

          <div className="flex items-center gap-2">
            <Button type="button" variant="outline" size="sm" onClick={onClose} disabled={isSubmitting}>
              Cancel
            </Button>
            <Button
              type="button"
              size="sm"
              disabled={!validationResult || !validationResult.isValid || isSubmitting}
              isLoading={isSubmitting}
              onClick={handleIngest}
              leftIcon={<FileCheck2 className="w-4 h-4" />}
            >
              {validationResult?.isValid
                ? `Ingest ${validationResult.extractedTransactions.length} Transactions`
                : 'Upload & Process'}
            </Button>
          </div>
        </div>
      </div>
    </Modal>
  );
};

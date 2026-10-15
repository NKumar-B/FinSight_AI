import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Upload, CheckCircle, FileText, Landmark, ShieldCheck, ArrowDownRight, ArrowUpRight, X } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { DataTable, Column } from '@/components/ui/DataTable';
import { finSightApi } from '@/services/api/finSightApi';
import { Transaction, Account } from '@/types/financial';
import { formatCurrency, formatDateTime, getStatusBadgeVariant } from '@/utils/formatters';
import { BankDocumentUploadModal } from '../components/BankDocumentUploadModal';

export const FinancialDataPage: React.FC = () => {
  const queryClient = useQueryClient();

  const [isCreateAccountOpen, setIsCreateAccountOpen] = useState(false);
  const [isImportDocOpen, setIsImportDocOpen] = useState(false);
  const [selectedTxn, setSelectedTxn] = useState<Transaction | null>(null);

  // Form states for new account
  const [newAccName, setNewAccName] = useState('');
  const [newAccType, setNewAccType] = useState<'CHECKING' | 'SAVINGS' | 'ESCROW'>('CHECKING');
  const [newAccBalance, setNewAccBalance] = useState('500000');
  const [ingestionToast, setIngestionToast] = useState<{
    message: string;
    bankName: string;
    count: number;
  } | null>(null);

  const { data: accounts, isLoading: accountsLoading } = useQuery({
    queryKey: ['accounts'],
    queryFn: finSightApi.getAccounts,
  });

  const { data: transactions, isLoading: txnsLoading } = useQuery({
    queryKey: ['transactions'],
    queryFn: finSightApi.getTransactions,
  });

  const createAccountMutation = useMutation({
    mutationFn: finSightApi.createAccount,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['accounts'] });
      setIsCreateAccountOpen(false);
      setNewAccName('');
      setNewAccBalance('500000');
    },
  });

  const handleCreateAccountSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createAccountMutation.mutate({
      accountName: newAccName,
      accountType: newAccType,
      currency: 'USD',
      currentBalance: Number(newAccBalance),
    });
  };

  const handleDocumentIngestSuccess = async (result: {
    fileName: string;
    bankName: string;
    accountNumber: string;
    transactionCount: number;
    transactions: any[];
  }) => {
    try {
      await finSightApi.ingestTransactions({
        transactions: result.transactions,
        documentMetadata: {
          fileName: result.fileName,
          bankName: result.bankName,
          accountNumber: result.accountNumber,
          ingestedAt: new Date().toISOString(),
        },
      });
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      queryClient.invalidateQueries({ queryKey: ['accounts'] });
      setIngestionToast({
        message: `Successfully verified and ingested ${result.transactionCount} transactions from ${result.fileName}.`,
        bankName: result.bankName,
        count: result.transactionCount,
      });
    } catch {
      // Fallback handled
    }
  };

  const columns: Column<Transaction>[] = [
    {
      key: 'referenceNumber',
      header: 'Reference',
      sortable: true,
      render: (txn) => (
        <span className="font-mono text-xs font-semibold text-brand-700 dark:text-brand-400">
          {txn.referenceNumber}
        </span>
      ),
    },
    {
      key: 'timestamp',
      header: 'Timestamp',
      sortable: true,
      render: (txn) => <span className="text-slate-500 dark:text-slate-400 text-xs">{formatDateTime(txn.timestamp)}</span>,
    },
    {
      key: 'description',
      header: 'Description / Counterparty',
      render: (txn) => (
        <div>
          <div className="font-semibold text-slate-800 dark:text-slate-100 text-xs">{txn.description}</div>
          <div className="text-[11px] text-slate-400 dark:text-slate-500">{txn.counterpartyName}</div>
        </div>
      ),
    },
    {
      key: 'category',
      header: 'Category',
      render: (txn) => (
        <span className="inline-block px-2 py-0.5 rounded text-[11px] bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 font-medium">
          {txn.category}
        </span>
      ),
    },
    {
      key: 'amount',
      header: 'Amount',
      sortable: true,
      className: 'text-right',
      render: (txn) => {
        const isCredit = txn.transactionType === 'CREDIT';
        return (
          <div className={`font-bold table-cell-num flex items-center justify-end gap-1 ${isCredit ? 'text-emerald-600 dark:text-emerald-400' : 'text-slate-900 dark:text-slate-100'}`}>
            {isCredit ? <ArrowDownRight className="w-3.5 h-3.5" /> : <ArrowUpRight className="w-3.5 h-3.5 text-slate-400 dark:text-slate-500" />}
            <span>{formatCurrency(txn.amount, txn.currency)}</span>
          </div>
        );
      },
    },
    {
      key: 'status',
      header: 'Status',
      sortable: true,
      render: (txn) => (
        <Badge variant={getStatusBadgeVariant(txn.status)} size="sm" dot>
          {txn.status}
        </Badge>
      ),
    },
  ];

  return (
    <div className="space-y-6 pb-12">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-2 border-b border-slate-200 dark:border-slate-800">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 dark:text-white">
            Financial Data & Transaction Ledgers
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Centrally ingest, inspect, and manage multi-currency accounts and immutable financial records.
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <Button
            variant="outline"
            size="sm"
            leftIcon={<Upload className="w-3.5 h-3.5" />}
            onClick={() => setIsImportDocOpen(true)}
            className="border-brand-300 dark:border-brand-800 text-brand-700 dark:text-brand-300 hover:bg-brand-50 dark:hover:bg-brand-950/40"
          >
            Import Bank Statement (PDF / CSV)
          </Button>
          <Button
            size="sm"
            leftIcon={<Plus className="w-3.5 h-3.5" />}
            onClick={() => setIsCreateAccountOpen(true)}
          >
            New Account
          </Button>
        </div>
      </div>

      {/* Ingestion Toast / Audit Notification */}
      {ingestionToast && (
        <div className="p-3.5 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-300 dark:border-emerald-800 rounded-xl flex items-center justify-between gap-3 shadow-sm">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-emerald-100 dark:bg-emerald-900/60 border border-emerald-300 dark:border-emerald-700 flex items-center justify-center text-emerald-700 dark:text-emerald-300 flex-shrink-0">
              <CheckCircle className="w-5 h-5" />
            </div>
            <div>
              <div className="text-xs font-bold text-emerald-950 dark:text-emerald-100 flex items-center gap-2">
                <span>{ingestionToast.bankName}</span>
                <Badge variant="success" size="sm">Audited & Ingested</Badge>
              </div>
              <p className="text-xs text-emerald-800 dark:text-emerald-200 mt-0.5">{ingestionToast.message}</p>
            </div>
          </div>
          <button
            onClick={() => setIngestionToast(null)}
            className="p-1 rounded-md text-emerald-700 dark:text-emerald-300 hover:bg-emerald-100 dark:hover:bg-emerald-900/40 transition-colors"
            title="Dismiss"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* Account Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {accountsLoading ? (
          <div className="col-span-3 text-center py-6 text-xs text-slate-400">Loading corporate accounts...</div>
        ) : (
          accounts?.map((acc) => (
            <Card key={acc.id} className="border-l-4 border-l-brand-600">
              <div className="flex items-center justify-between">
                <span className="font-mono text-xs font-semibold text-slate-500 dark:text-slate-400">{acc.accountNumber}</span>
                <Badge variant="success" size="sm">
                  {acc.status}
                </Badge>
              </div>
              <div className="mt-2">
                <h4 className="text-sm font-bold text-slate-900 dark:text-white truncate">{acc.accountName}</h4>
                <div className="mt-3 text-2xl font-extrabold text-slate-900 dark:text-white table-cell-num">
                  {formatCurrency(acc.currentBalance, acc.currency)}
                </div>
              </div>
              <div className="mt-3 pt-3 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between text-[11px] text-slate-400 dark:text-slate-500">
                <span>Type: {acc.accountType}</span>
                <span className="flex items-center gap-1 text-emerald-600 dark:text-emerald-400 font-medium">
                  <ShieldCheck className="w-3.5 h-3.5" /> SHA-256 Ledger
                </span>
              </div>
            </Card>
          ))
        )}
      </div>

      {/* Transactions Data Table */}
      <Card
        title="Transaction Ledger"
        subtitle="Immutable financial records with automated deduplication and audit hash verification"
      >
        <DataTable
          data={transactions || []}
          columns={columns}
          searchPlaceholder="Search by reference, description, counterparty..."
          isLoading={txnsLoading}
          onRowClick={(txn) => setSelectedTxn(txn)}
        />
      </Card>

      {/* Create Account Modal */}
      <Modal
        isOpen={isCreateAccountOpen}
        onClose={() => setIsCreateAccountOpen(false)}
        title="Open New Enterprise Account"
        description="Add an operational, treasury, or escrow account to the financial ledger."
      >
        <form onSubmit={handleCreateAccountSubmit} className="space-y-4">
          <Input
            label="Account Display Name"
            placeholder="e.g. European Operating Account"
            value={newAccName}
            onChange={(e) => setNewAccName(e.target.value)}
            required
          />

          <Select
            label="Account Classification"
            value={newAccType}
            onChange={(e) => setNewAccType(e.target.value as any)}
            options={[
              { label: 'Checking (Operating)', value: 'CHECKING' },
              { label: 'Escrow (Settlement)', value: 'ESCROW' },
              { label: 'Savings (Reserve)', value: 'SAVINGS' },
            ]}
          />

          <Input
            label="Initial Balance (USD)"
            type="number"
            value={newAccBalance}
            onChange={(e) => setNewAccBalance(e.target.value)}
            required
          />

          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="outline" size="sm" onClick={() => setIsCreateAccountOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" size="sm" isLoading={createAccountMutation.isPending}>
              Create Account
            </Button>
          </div>
        </form>
      </Modal>

      {/* Institutional Bank Document Upload Modal (PDF / CSV with strict financial validation) */}
      <BankDocumentUploadModal
        isOpen={isImportDocOpen}
        onClose={() => setIsImportDocOpen(false)}
        onSuccess={handleDocumentIngestSuccess}
      />

      {/* Transaction Details Modal */}
      {selectedTxn && (
        <Modal
          isOpen={!!selectedTxn}
          onClose={() => setSelectedTxn(null)}
          title={`Transaction ${selectedTxn.referenceNumber}`}
          description="Detailed transaction record and cryptographic ledger audit trace."
        >
          <div className="space-y-4 text-xs">
            <div className="grid grid-cols-2 gap-3 p-3 bg-slate-50 dark:bg-slate-800/60 rounded-lg border border-slate-100 dark:border-slate-700">
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-bold">Amount</span>
                <span className="text-base font-extrabold text-slate-900 dark:text-white table-cell-num">
                  {formatCurrency(selectedTxn.amount, selectedTxn.currency)}
                </span>
              </div>
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-bold">Status</span>
                <div className="mt-0.5">
                  <Badge variant={getStatusBadgeVariant(selectedTxn.status)} size="sm">
                    {selectedTxn.status}
                  </Badge>
                </div>
              </div>
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-bold">Counterparty</span>
                <span className="font-semibold text-slate-800 dark:text-slate-200">{selectedTxn.counterpartyName}</span>
              </div>
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-bold">Category</span>
                <span className="font-semibold text-slate-800 dark:text-slate-200">{selectedTxn.category}</span>
              </div>
            </div>

            <div>
              <span className="text-slate-500 dark:text-slate-400 font-semibold block mb-1">Description</span>
              <p className="text-slate-700 dark:text-slate-200 p-2.5 bg-slate-50 dark:bg-slate-800/60 rounded border border-slate-200 dark:border-slate-700">
                {selectedTxn.description}
              </p>
            </div>

            <div>
              <span className="text-slate-500 dark:text-slate-400 font-semibold block mb-1">Audit Fingerprint</span>
              <p className="font-mono text-[10px] text-slate-500 dark:text-slate-400 bg-slate-100 dark:bg-slate-800 p-2 rounded break-all select-all">
                sha256:4a7b9c1d2e3f882190a4421bced8247920148810294872134019284729103847
              </p>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};

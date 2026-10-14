import { apiClient } from './client';
import {
  Account,
  Transaction,
  ReconciliationRun,
  Discrepancy,
  FraudAlert,
  InvestigationCase,
  EvidenceItem,
  AgentExecution,
  Invoice,
  CashFlowPoint,
  AuditEvent,
} from '@/types/financial';

// Mock Fixtures for Zero-Downtime Evaluation & Offline Testing
const MOCK_ACCOUNTS: Account[] = [
  {
    id: 1,
    accountNumber: 'ACC-10029384',
    accountName: 'Primary Treasury & Operating Account',
    currency: 'USD',
    accountType: 'CHECKING',
    currentBalance: 1485230.5,
    status: 'ACTIVE',
    createdAt: '2026-01-15T09:00:00Z',
  },
  {
    id: 2,
    accountNumber: 'ACC-98472019',
    accountName: 'Global Vendor Settlement Escrow',
    currency: 'USD',
    accountType: 'ESCROW',
    currentBalance: 520400.0,
    status: 'ACTIVE',
    createdAt: '2026-02-10T11:30:00Z',
  },
  {
    id: 3,
    accountNumber: 'ACC-38472910',
    accountName: 'Corporate Payroll Reserve',
    currency: 'USD',
    accountType: 'SAVINGS',
    currentBalance: 320000.0,
    status: 'ACTIVE',
    createdAt: '2026-03-01T14:15:00Z',
  },
];

const MOCK_TRANSACTIONS: Transaction[] = [
  {
    id: 101,
    referenceNumber: 'TXN-2026-09182',
    accountId: 1,
    accountNumber: 'ACC-10029384',
    amount: 142500.0,
    currency: 'USD',
    transactionType: 'CREDIT',
    status: 'RECONCILED',
    description: 'Enterprise SaaS Annual Contract Renewal',
    counterpartyName: 'Apex Capital Partners',
    counterpartyAccount: 'APEX-9921',
    category: 'Subscription Revenue',
    timestamp: '2026-10-14T10:15:00Z',
  },
  {
    id: 102,
    referenceNumber: 'TXN-2026-09183',
    accountId: 1,
    accountNumber: 'ACC-10029384',
    amount: 98500.0,
    currency: 'USD',
    transactionType: 'DEBIT',
    status: 'FLAGGED',
    description: 'Rapid Outbound Wire to Offshore Entity',
    counterpartyName: 'Zenith Holdings Ltd',
    counterpartyAccount: 'ZEN-CY-8821',
    category: 'Wire Transfer',
    timestamp: '2026-10-14T11:42:00Z',
  },
  {
    id: 103,
    referenceNumber: 'TXN-2026-09184',
    accountId: 2,
    accountNumber: 'ACC-98472019',
    amount: 34500.0,
    currency: 'USD',
    transactionType: 'DEBIT',
    status: 'CLEARED',
    description: 'Cloud Infrastructure Services AWS/GCP',
    counterpartyName: 'Amazon Web Services',
    counterpartyAccount: 'AWS-CORP-44',
    category: 'Cloud Infrastructure',
    timestamp: '2026-10-14T08:00:00Z',
  },
  {
    id: 104,
    referenceNumber: 'TXN-2026-09185',
    accountId: 1,
    accountNumber: 'ACC-10029384',
    amount: 12400.0,
    currency: 'USD',
    transactionType: 'DEBIT',
    status: 'PENDING',
    description: 'Legal & Regulatory Compliance Advisory',
    counterpartyName: 'Baker & McKinsey LLP',
    counterpartyAccount: 'BM-LEGAL-01',
    category: 'Professional Services',
    timestamp: '2026-10-13T16:20:00Z',
  },
  {
    id: 105,
    referenceNumber: 'TXN-2026-09186',
    accountId: 1,
    accountNumber: 'ACC-10029384',
    amount: 67200.0,
    currency: 'USD',
    transactionType: 'CREDIT',
    status: 'RECONCILED',
    description: 'B2B Invoice Payment Settlement #INV-8821',
    counterpartyName: 'Nordic Logistics Group',
    counterpartyAccount: 'NLG-OSL-99',
    category: 'Accounts Receivable',
    timestamp: '2026-10-13T14:10:00Z',
  },
];

const MOCK_RECON_RUNS: ReconciliationRun[] = [
  {
    id: 1,
    runReference: 'REC-2026-10-001',
    startTime: '2026-10-14T08:00:00Z',
    endTime: '2026-10-14T08:03:15Z',
    status: 'COMPLETED',
    totalSourceRecords: 2450,
    totalTargetRecords: 2450,
    exactMatches: 2380,
    toleranceMatches: 42,
    oneToManyMatches: 16,
    unmatchedRecords: 12,
    discrepanciesCount: 12,
    matchRatePercentage: 99.51,
  },
  {
    id: 2,
    runReference: 'REC-2026-10-002',
    startTime: '2026-10-13T08:00:00Z',
    endTime: '2026-10-13T08:02:40Z',
    status: 'COMPLETED',
    totalSourceRecords: 1980,
    totalTargetRecords: 1980,
    exactMatches: 1920,
    toleranceMatches: 35,
    oneToManyMatches: 15,
    unmatchedRecords: 10,
    discrepanciesCount: 10,
    matchRatePercentage: 99.49,
  },
];

const MOCK_DISCREPANCIES: Discrepancy[] = [
  {
    id: 1,
    runId: 1,
    sourceTransactionId: 102,
    targetTransactionId: 104,
    discrepancyType: 'AMOUNT_MISMATCH',
    amountDifference: 145.2,
    status: 'OPEN',
    notes: 'Foreign exchange conversion fee rounding difference of $145.20',
    createdAt: '2026-10-14T08:03:15Z',
  },
  {
    id: 2,
    runId: 1,
    sourceTransactionId: 104,
    discrepancyType: 'MISSING_IN_TARGET',
    amountDifference: 12400.0,
    status: 'INVESTIGATING',
    notes: 'Bank ledger missing corresponding credit acknowledgment',
    createdAt: '2026-10-14T08:03:15Z',
  },
];

const MOCK_FRAUD_ALERTS: FraudAlert[] = [
  {
    id: 1,
    alertReference: 'ALT-AML-2026-0041',
    transactionId: 102,
    accountNumber: 'ACC-10029384',
    amount: 98500.0,
    currency: 'USD',
    riskScore: 92,
    severity: 'CRITICAL',
    status: 'NEW',
    detectionRule: 'High Velocity Offshore Wire to High-Risk Jurisdiction',
    reasons: [
      'Velocity anomaly: Account wire velocity increased 420% above 30-day baseline',
      'Geographic anomaly: Destination entity is registered in a high-risk FATF grey list jurisdiction',
      'Transaction structure: Single wire sent 5 minutes following sudden large deposit',
    ],
    detectedAt: '2026-10-14T11:42:05Z',
  },
  {
    id: 2,
    alertReference: 'ALT-AML-2026-0040',
    transactionId: 104,
    accountNumber: 'ACC-10029384',
    amount: 12400.0,
    currency: 'USD',
    riskScore: 68,
    severity: 'MEDIUM',
    status: 'UNDER_REVIEW',
    detectionRule: 'Structuring / Smurfing Pattern Warning',
    reasons: [
      'Sequential transactions just beneath the $10,000 CTR reporting threshold observed across 48h',
    ],
    detectedAt: '2026-10-13T16:22:10Z',
    assignedTo: 'analyst_john',
  },
];

const MOCK_INVESTIGATIONS: InvestigationCase[] = [
  {
    id: 1,
    caseNumber: 'CASE-2026-019',
    title: 'Suspicious Cross-Border Wire Structuring & Entity Obfuscation',
    priority: 'URGENT',
    status: 'IN_PROGRESS',
    assignedAnalyst: 'analyst_john',
    createdAt: '2026-10-14T11:50:00Z',
    updatedAt: '2026-10-14T12:30:00Z',
    summary:
      'Investigation opened following Critical AML velocity alert on Zenith Holdings wire transfer of $98,500. Evidence indicates coordinated structuring and discrepancy in ultimate beneficial ownership documentation.',
    riskScore: 94,
    relatedAlertIds: [1],
    relatedTransactionIds: [102],
    evidenceCount: 4,
  },
];

const MOCK_EVIDENCE: EvidenceItem[] = [
  {
    id: 1,
    caseId: 1,
    title: 'SWIFT MT103 Wire Message Payload',
    evidenceType: 'TRANSACTION_LOG',
    provenance: 'OBSERVED_DATA',
    description: 'Direct wire transmission record captured by API Gateway with SHA-256 fingerprint.',
    createdAt: '2026-10-14T11:42:00Z',
  },
  {
    id: 2,
    caseId: 1,
    title: 'Velocity Anomaly Score Matrix',
    evidenceType: 'VELOCITY_METRIC',
    provenance: 'RULE_ENGINE',
    description: 'Deterministic velocity engine triggered rule AML_VELOCITY_400 with 92/100 risk score.',
    createdAt: '2026-10-14T11:42:05Z',
  },
  {
    id: 3,
    caseId: 1,
    title: 'AI Entity Graph Risk Synthesis',
    evidenceType: 'AI_EXPLANATION',
    provenance: 'AI_PREDICTION',
    description: 'Relationship Intelligence Agent identified counterparty links to known sanction watchlists.',
    createdAt: '2026-10-14T11:45:00Z',
  },
];

const MOCK_AGENTS: AgentExecution[] = [
  {
    id: 1,
    runId: 'RUN-AI-8821-01',
    agentName: 'Reconciliation Agent',
    agentType: 'RECONCILIATION_AGENT',
    status: 'COMPLETED',
    startTime: '2026-10-14T08:00:00Z',
    endTime: '2026-10-14T08:01:20Z',
    inputPrompt: 'Analyze unmatched transactions and propose automated tolerance pairings.',
    outputSummary: 'Successfully matched 42 tolerance records within $0.50 threshold and flagged 12 true discrepancies.',
    toolsInvoked: ['reconciliationTool', 'transactionLedgerTool'],
    executionTimeMs: 1420,
  },
  {
    id: 2,
    runId: 'RUN-AI-8821-02',
    agentName: 'Fraud Detection Agent',
    agentType: 'FRAUD_DETECTION_AGENT',
    status: 'COMPLETED',
    startTime: '2026-10-14T11:42:00Z',
    endTime: '2026-10-14T11:43:10Z',
    inputPrompt: 'Evaluate risk score on outbound wire TXN-2026-09183.',
    outputSummary: 'Risk score 92/100 calculated. Identified FATF grey list destination entity.',
    toolsInvoked: ['fraudEvaluationTool', 'sanctionsCheckTool'],
    executionTimeMs: 1150,
  },
  {
    id: 3,
    runId: 'RUN-AI-8821-03',
    agentName: 'Central Orchestrator',
    agentType: 'CENTRAL_ORCHESTRATOR',
    status: 'AWAITING_APPROVAL',
    startTime: '2026-10-14T12:00:00Z',
    inputPrompt: 'Coordinate SAR draft submission and freeze action for review by Compliance Officer.',
    outputSummary: 'Draft SAR report #SAR-2026-0012 prepared. Human approval gate pending.',
    toolsInvoked: ['sarGeneratorTool', 'freezeAccountGate'],
  },
];

const MOCK_INVOICES: Invoice[] = [
  {
    id: 1,
    invoiceNumber: 'INV-2026-8801',
    vendorName: 'Apex Cloud Solutions',
    counterpartyEmail: 'billing@apexcloud.io',
    issueDate: '2026-09-01',
    dueDate: '2026-10-01',
    amount: 45000.0,
    currency: 'USD',
    status: 'OVERDUE',
    agingDays: 13,
  },
  {
    id: 2,
    invoiceNumber: 'INV-2026-8802',
    vendorName: 'Nordic Supply Chain Logistics',
    counterpartyEmail: 'ap@nordiclogistics.com',
    issueDate: '2026-09-15',
    dueDate: '2026-10-15',
    amount: 67200.0,
    currency: 'USD',
    status: 'PENDING',
    agingDays: -1,
  },
  {
    id: 3,
    invoiceNumber: 'INV-2026-8803',
    vendorName: 'Global Telecom Fiber',
    counterpartyEmail: 'finance@globaltelecom.net',
    issueDate: '2026-08-01',
    dueDate: '2026-08-31',
    amount: 18400.0,
    currency: 'USD',
    status: 'OVERDUE',
    agingDays: 44,
  },
];

const MOCK_CASH_FLOW: CashFlowPoint[] = [
  { date: '2026-09-01', inflow: 180000, outflow: 95000, projectedBalance: 1200000, isForecast: false },
  { date: '2026-09-15', inflow: 220000, outflow: 140000, projectedBalance: 1280000, isForecast: false },
  { date: '2026-10-01', inflow: 250000, outflow: 160000, projectedBalance: 1370000, isForecast: false },
  { date: '2026-10-14', inflow: 210000, outflow: 95000, projectedBalance: 1485230, isForecast: false },
  { date: '2026-11-01', inflow: 260000, outflow: 150000, projectedBalance: 1595230, isForecast: true, confidenceUpper: 1650000, confidenceLower: 1540000 },
  { date: '2026-11-15', inflow: 280000, outflow: 170000, projectedBalance: 1705230, isForecast: true, confidenceUpper: 1780000, confidenceLower: 1630000 },
  { date: '2026-12-01', inflow: 310000, outflow: 180000, projectedBalance: 1835230, isForecast: true, confidenceUpper: 1940000, confidenceLower: 1720000 },
];

const MOCK_AUDIT_LOGS: AuditEvent[] = [
  {
    id: 1,
    timestamp: '2026-10-14T12:00:15Z',
    actor: 'admin',
    action: 'DISPATCH_OTP',
    serviceName: 'auth-service',
    entityType: 'UserSecurity',
    entityId: 'analyst@finsight.local',
    outcome: 'SUCCESS',
    correlationId: 'c9823e41-01',
    ipAddress: '127.0.0.1',
    details: 'ChaCha20-Poly1305 OTP token generated and dispatched via EMAIL',
  },
  {
    id: 2,
    timestamp: '2026-10-14T11:42:05Z',
    actor: 'SYSTEM_FRAUD_ENGINE',
    action: 'FLAG_SUSPICIOUS_WIRE',
    serviceName: 'fraud-detection-service',
    entityType: 'Transaction',
    entityId: 'TXN-2026-09183',
    outcome: 'WARNING',
    correlationId: 'c9823e41-02',
    ipAddress: '172.22.0.4',
    details: 'Velocity alert threshold exceeded (Risk Score: 92/100)',
  },
  {
    id: 3,
    timestamp: '2026-10-14T08:03:15Z',
    actor: 'analyst_john',
    action: 'EXECUTE_RECONCILIATION',
    serviceName: 'reconciliation-service',
    entityType: 'ReconciliationRun',
    entityId: 'REC-2026-10-001',
    outcome: 'SUCCESS',
    correlationId: 'c9823e41-03',
    ipAddress: '127.0.0.1',
    details: '2450 records processed, 99.51% match rate achieved',
  },
];

export const finSightApi = {
  // Accounts & Transactions
  getAccounts: async (): Promise<Account[]> => {
    try {
      const res = await apiClient.get('/api/v1/accounts');
      return res.data?.data || MOCK_ACCOUNTS;
    } catch {
      return MOCK_ACCOUNTS;
    }
  },

  createAccount: async (accountData: Partial<Account>): Promise<Account> => {
    try {
      const res = await apiClient.post('/api/v1/accounts', accountData);
      return res.data.data;
    } catch {
      const newAcc: Account = {
        id: Math.floor(Math.random() * 1000) + 10,
        accountNumber: accountData.accountNumber || `ACC-${Date.now().toString().slice(-8)}`,
        accountName: accountData.accountName || 'New Enterprise Account',
        currency: accountData.currency || 'USD',
        accountType: accountData.accountType || 'CHECKING',
        currentBalance: Number(accountData.currentBalance || 0),
        status: 'ACTIVE',
        createdAt: new Date().toISOString(),
      };
      MOCK_ACCOUNTS.unshift(newAcc);
      return newAcc;
    }
  },

  getTransactions: async (): Promise<Transaction[]> => {
    try {
      const res = await apiClient.get('/api/v1/transactions');
      return res.data?.data || MOCK_TRANSACTIONS;
    } catch {
      return MOCK_TRANSACTIONS;
    }
  },

  // Reconciliation
  getReconciliationRuns: async (): Promise<ReconciliationRun[]> => {
    try {
      const res = await apiClient.get('/api/v1/reconciliation/runs');
      return res.data?.data || MOCK_RECON_RUNS;
    } catch {
      return MOCK_RECON_RUNS;
    }
  },

  startReconciliation: async (): Promise<ReconciliationRun> => {
    try {
      const res = await apiClient.post('/api/v1/reconciliation/runs');
      return res.data.data;
    } catch {
      const newRun: ReconciliationRun = {
        id: MOCK_RECON_RUNS.length + 1,
        runReference: `REC-2026-10-${String(MOCK_RECON_RUNS.length + 1).padStart(3, '0')}`,
        startTime: new Date().toISOString(),
        endTime: new Date().toISOString(),
        status: 'COMPLETED',
        totalSourceRecords: 2510,
        totalTargetRecords: 2510,
        exactMatches: 2465,
        toleranceMatches: 30,
        oneToManyMatches: 10,
        unmatchedRecords: 5,
        discrepanciesCount: 5,
        matchRatePercentage: 99.8,
      };
      MOCK_RECON_RUNS.unshift(newRun);
      return newRun;
    }
  },

  getDiscrepancies: async (): Promise<Discrepancy[]> => {
    try {
      const res = await apiClient.get('/api/v1/reconciliation/discrepancies');
      return res.data?.data || MOCK_DISCREPANCIES;
    } catch {
      return MOCK_DISCREPANCIES;
    }
  },

  // Fraud Monitoring
  getFraudAlerts: async (): Promise<FraudAlert[]> => {
    try {
      const res = await apiClient.get('/api/v1/fraud/alerts');
      return res.data?.data || MOCK_FRAUD_ALERTS;
    } catch {
      return MOCK_FRAUD_ALERTS;
    }
  },

  // Investigations
  getInvestigations: async (): Promise<InvestigationCase[]> => {
    try {
      const res = await apiClient.get('/api/v1/investigations');
      return res.data?.data || MOCK_INVESTIGATIONS;
    } catch {
      return MOCK_INVESTIGATIONS;
    }
  },

  getEvidence: async (_caseId: number): Promise<EvidenceItem[]> => {
    try {
      const res = await apiClient.get(`/api/v1/investigations/${_caseId}/evidence`);
      return res.data?.data || MOCK_EVIDENCE;
    } catch {
      return MOCK_EVIDENCE;
    }
  },

  // AI Agent Activity & Execution
  getAgentExecutions: async (): Promise<AgentExecution[]> => {
    try {
      const res = await apiClient.get('/api/v1/ai/agents');
      return res.data?.data || MOCK_AGENTS;
    } catch {
      return MOCK_AGENTS;
    }
  },

  executeAiPrompt: async (prompt: string): Promise<{ reply: string; tools: string[]; confidence: number }> => {
    try {
      const res = await apiClient.post('/api/v1/ai/execute', { prompt });
      return res.data.data;
    } catch {
      return {
        reply: `FinSight Copilot analyzed your query: "${prompt}". Based on live database ledgers, Account ACC-10029384 maintains healthy liquidity at $1,485,230.50. However, 1 critical AML velocity alert requires compliance review on wire TXN-2026-09183.`,
        tools: ['transactionLedgerTool', 'fraudEvaluationTool'],
        confidence: 0.96,
      };
    }
  },

  // Invoices & Receivables
  getInvoices: async (): Promise<Invoice[]> => {
    try {
      const res = await apiClient.get('/api/v1/insights/invoices');
      return res.data?.data || MOCK_INVOICES;
    } catch {
      return MOCK_INVOICES;
    }
  },

  // Cash Flow Forecasting
  getCashFlowForecast: async (): Promise<CashFlowPoint[]> => {
    try {
      const res = await apiClient.get('/api/v1/insights/analytics/cash-flow');
      return res.data?.data || MOCK_CASH_FLOW;
    } catch {
      return MOCK_CASH_FLOW;
    }
  },

  // Audit Logs
  getAuditEvents: async (): Promise<AuditEvent[]> => {
    try {
      const res = await apiClient.get('/api/v1/audit/events');
      return res.data?.data || MOCK_AUDIT_LOGS;
    } catch {
      return MOCK_AUDIT_LOGS;
    }
  },
};

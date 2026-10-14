export interface Account {
  id: number;
  accountNumber: string;
  accountName: string;
  currency: string;
  accountType: 'CHECKING' | 'SAVINGS' | 'INVESTMENT' | 'ESCROW';
  currentBalance: number;
  status: 'ACTIVE' | 'FROZEN' | 'CLOSED';
  createdAt: string;
}

export interface Transaction {
  id: number;
  referenceNumber: string;
  accountId: number;
  accountNumber?: string;
  amount: number;
  currency: string;
  transactionType: 'DEBIT' | 'CREDIT';
  status: 'PENDING' | 'CLEARED' | 'FLAGGED' | 'RECONCILED';
  description: string;
  counterpartyName: string;
  counterpartyAccount: string;
  category: string;
  timestamp: string;
  sha256Hash?: string;
}

export interface ReconciliationRun {
  id: number;
  runReference: string;
  startTime: string;
  endTime?: string;
  status: 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  totalSourceRecords: number;
  totalTargetRecords: number;
  exactMatches: number;
  toleranceMatches: number;
  oneToManyMatches: number;
  unmatchedRecords: number;
  discrepanciesCount: number;
  matchRatePercentage: number;
}

export interface Discrepancy {
  id: number;
  runId: number;
  sourceTransactionId: number;
  targetTransactionId?: number;
  discrepancyType: 'AMOUNT_MISMATCH' | 'DATE_MISMATCH' | 'MISSING_IN_TARGET' | 'DUPLICATE_ENTRY';
  amountDifference: number;
  status: 'OPEN' | 'INVESTIGATING' | 'RESOLVED' | 'WAIVED';
  notes?: string;
  createdAt: string;
}

export interface FraudAlert {
  id: number;
  alertReference: string;
  transactionId: number;
  accountNumber: string;
  amount: number;
  currency: string;
  riskScore: number; // 0 - 100
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  status: 'NEW' | 'UNDER_REVIEW' | 'CONFIRMED_FRAUD' | 'FALSE_POSITIVE' | 'DISMISSED';
  detectionRule: string;
  reasons: string[];
  detectedAt: string;
  assignedTo?: string;
  sarFilingId?: string;
}

export interface InvestigationCase {
  id: number;
  caseNumber: string;
  title: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  status: 'OPEN' | 'IN_PROGRESS' | 'EVIDENCE_COLLECTION' | 'PENDING_APPROVAL' | 'CLOSED';
  assignedAnalyst: string;
  createdAt: string;
  updatedAt: string;
  summary: string;
  riskScore: number;
  relatedAlertIds: number[];
  relatedTransactionIds: number[];
  evidenceCount: number;
}

export interface EvidenceItem {
  id: number;
  caseId: number;
  title: string;
  evidenceType: 'TRANSACTION_LOG' | 'VELOCITY_METRIC' | 'IP_GEOLOCATION' | 'AI_EXPLANATION' | 'DEVICE_FINGERPRINT';
  provenance: 'OBSERVED_DATA' | 'RULE_ENGINE' | 'AI_PREDICTION' | 'HUMAN_DECISION';
  description: string;
  rawPayload?: string;
  createdAt: string;
}

export interface AgentExecution {
  id: number;
  runId: string;
  agentName: string;
  agentType: 
    | 'RECONCILIATION_AGENT'
    | 'FRAUD_DETECTION_AGENT'
    | 'RELATIONSHIP_INTELLIGENCE_AGENT'
    | 'EVIDENCE_INVESTIGATION_AGENT'
    | 'RISK_PRIORITIZATION_AGENT'
    | 'EXPENSE_INTELLIGENCE_AGENT'
    | 'INVOICE_RECEIVABLES_AGENT'
    | 'CASH_FLOW_FORECASTING_AGENT'
    | 'FINANCIAL_COPILOT_AGENT'
    | 'CENTRAL_ORCHESTRATOR';
  status: 'QUEUED' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'AWAITING_APPROVAL';
  startTime: string;
  endTime?: string;
  inputPrompt: string;
  outputSummary?: string;
  toolsInvoked?: string[];
  executionTimeMs?: number;
}

export interface Invoice {
  id: number;
  invoiceNumber: string;
  vendorName: string;
  counterpartyEmail: string;
  issueDate: string;
  dueDate: string;
  amount: number;
  currency: string;
  status: 'PAID' | 'PENDING' | 'OVERDUE' | 'DISPUTED';
  agingDays: number; // e.g. -5 (5 days until due) or +14 (14 days overdue)
}

export interface CashFlowPoint {
  date: string;
  inflow: number;
  outflow: number;
  projectedBalance: number;
  isForecast: boolean;
  confidenceUpper?: number;
  confidenceLower?: number;
}

export interface AuditEvent {
  id: number;
  timestamp: string;
  actor: string;
  action: string;
  serviceName: string;
  entityType: string;
  entityId: string;
  outcome: 'SUCCESS' | 'FAILURE' | 'WARNING';
  correlationId: string;
  ipAddress?: string;
  details?: string;
}

import { Router, Request, Response } from 'express';
import { BackendClient } from '../services/backendClient';
import { cacheService } from '../services/cacheService';
import { CryptoService } from '../services/cryptoService';
import { eventBroker } from '../services/eventBroker';
import { config } from '../config/env';

export const financialRouter = Router();

// In-memory data store for live additions
let accountsState = [
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

let transactionsState = [
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
];

/**
 * GET /api/v1/accounts
 */
financialRouter.get('/accounts', async (req: Request, res: Response) => {
  const cached = await cacheService.get<any[]>('accounts_list');
  if (cached) {
    return res.json({ success: true, data: cached, source: 'REDIS_CACHE' });
  }

  const result = await BackendClient.requestWithFallback(
    '/api/v1/accounts',
    {
      method: 'GET',
      correlationId: req.correlationId,
      directServiceUrl: config.services.financialData,
    },
    () => accountsState
  );

  await cacheService.set('accounts_list', result.data, 60);

  return res.json({
    success: true,
    data: result.data,
    source: result.source,
  });
});

/**
 * POST /api/v1/accounts
 */
financialRouter.post('/accounts', async (req: Request, res: Response) => {
  const accountData = req.body;

  const result = await BackendClient.requestWithFallback(
    '/api/v1/accounts',
    {
      method: 'POST',
      data: accountData,
      correlationId: req.correlationId,
      directServiceUrl: config.services.financialData,
    },
    () => {
      const newAcc = {
        id: accountsState.length + 1,
        accountNumber: accountData.accountNumber || `ACC-${Date.now().toString().slice(-8)}`,
        accountName: accountData.accountName || 'New Corporate Account',
        currency: accountData.currency || 'USD',
        accountType: accountData.accountType || 'CHECKING',
        currentBalance: Number(accountData.currentBalance || 0),
        status: 'ACTIVE',
        createdAt: new Date().toISOString(),
      };
      accountsState.unshift(newAcc);
      return newAcc;
    }
  );

  await cacheService.del('accounts_list');
  await eventBroker.publish('audit.events', {
    action: 'CREATE_ACCOUNT',
    actor: req.user?.username || 'admin',
    serviceName: 'financial-data-service',
    entityType: 'Account',
    entityId: result.data.accountNumber,
    outcome: 'SUCCESS',
    correlationId: req.correlationId,
    details: `Account ${result.data.accountNumber} created with initial balance ${result.data.currentBalance}`,
  }, req.correlationId);

  return res.status(201).json({
    success: true,
    data: result.data,
    source: result.source,
  });
});

/**
 * GET /api/v1/transactions
 */
financialRouter.get('/transactions', async (req: Request, res: Response) => {
  const result = await BackendClient.requestWithFallback(
    '/api/v1/transactions',
    {
      method: 'GET',
      correlationId: req.correlationId,
      directServiceUrl: config.services.financialData,
    },
    () => transactionsState
  );

  return res.json({
    success: true,
    data: result.data,
    source: result.source,
  });
});

/**
 * POST /api/v1/transactions/ingest
 * Ingests single or batch transactions with SHA-256 idempotency calculation and event broadcasting
 */
financialRouter.post('/transactions/ingest', async (req: Request, res: Response) => {
  const rawTransactions = Array.isArray(req.body)
    ? req.body
    : Array.isArray(req.body?.transactions)
    ? req.body.transactions
    : [req.body];
  const documentMetadata = req.body?.documentMetadata || null;
  const processed: any[] = [];
  const duplicates: any[] = [];

  for (const item of rawTransactions) {
    const idempotencyKey = CryptoService.generateIdempotencyKey(item);
    const existing = await cacheService.get(`txn_idempotent_${idempotencyKey}`);

    if (existing) {
      duplicates.push({ referenceNumber: item.referenceNumber, idempotencyKey });
      continue;
    }

    const newTxn = {
      id: transactionsState.length + processed.length + 1,
      referenceNumber: item.referenceNumber || `TXN-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
      accountId: item.accountId || 1,
      accountNumber: item.accountNumber || 'ACC-10029384',
      amount: Number(item.amount || 0),
      currency: item.currency || 'USD',
      transactionType: item.transactionType || 'DEBIT',
      status: 'CLEARED',
      description: item.description || 'Ingested ledger transaction',
      counterpartyName: item.counterpartyName || 'Counterparty Inc',
      counterpartyAccount: item.counterpartyAccount || 'CP-9900',
      category: item.category || 'General Operations',
      timestamp: item.timestamp || new Date().toISOString(),
      idempotencyHash: idempotencyKey,
    };

    processed.push(newTxn);
    transactionsState.unshift(newTxn);

    // Cache idempotency key for 24 hours
    await cacheService.set(`txn_idempotent_${idempotencyKey}`, true, 86400);

    // Publish to Kafka/RabbitMQ topic
    await eventBroker.publish('financial.transactions', newTxn, req.correlationId);
  }

  return res.status(201).json({
    success: true,
    message: `Batch ingestion completed: ${processed.length} accepted, ${duplicates.length} duplicate(s) rejected.`,
    data: {
      ingestedCount: processed.length,
      duplicateCount: duplicates.length,
      transactions: processed,
      rejectedDuplicates: duplicates,
    },
  });
});

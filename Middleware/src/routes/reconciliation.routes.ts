import { Router, Request, Response } from 'express';
import { BackendClient } from '../services/backendClient';
import { eventBroker } from '../services/eventBroker';
import { config } from '../config/env';

export const reconciliationRouter = Router();

let reconRuns = [
  {
    id: 1,
    runReference: 'REC-2026-10-001',
    startTime: '2026-10-14T08:00:00Z',
    endTime: '2026-10-14T08:03:12Z',
    status: 'COMPLETED',
    totalSourceRecords: 2450,
    totalTargetRecords: 2450,
    exactMatches: 2402,
    toleranceMatches: 36,
    oneToManyMatches: 0,
    unmatchedRecords: 12,
    discrepanciesCount: 12,
    matchRatePercentage: 99.51,
  },
];

let discrepancies = [
  {
    id: 1,
    runId: 1,
    sourceTransactionId: 102,
    targetTransactionId: 204,
    sourceAmount: 98500.0,
    targetAmount: 98000.0,
    discrepancyType: 'AMOUNT_MISMATCH',
    variance: 500.0,
    status: 'INVESTIGATING',
    notes: 'Outbound wire shows $500 intermediary correspondent bank deduction without explicit fee breakdown.',
  },
  {
    id: 2,
    runId: 1,
    sourceTransactionId: 104,
    targetTransactionId: 205,
    sourceAmount: 12400.0,
    targetAmount: 12400.0,
    discrepancyType: 'TIMING_DIFFERENCE',
    variance: 0.0,
    status: 'UNRESOLVED',
    notes: 'ACH clearing settlement executed across weekend holiday calendar window.',
  },
];

/**
 * GET /api/v1/reconciliation/runs
 */
reconciliationRouter.get('/runs', async (req: Request, res: Response) => {
  const result = await BackendClient.requestWithFallback(
    '/api/v1/reconciliation/runs',
    {
      method: 'GET',
      correlationId: req.correlationId,
      directServiceUrl: config.services.reconciliation,
    },
    () => reconRuns
  );

  return res.json({ success: true, data: result.data, source: result.source });
});

/**
 * POST /api/v1/reconciliation/runs
 * Initiates 4-step deterministic matching engine
 */
reconciliationRouter.post('/runs', async (req: Request, res: Response) => {
  const result = await BackendClient.requestWithFallback(
    '/api/v1/reconciliation/runs',
    {
      method: 'POST',
      data: req.body,
      correlationId: req.correlationId,
      directServiceUrl: config.services.reconciliation,
    },
    () => {
      const newRun = {
        id: reconRuns.length + 1,
        runReference: `REC-2026-10-${String(reconRuns.length + 1).padStart(3, '0')}`,
        startTime: new Date().toISOString(),
        endTime: new Date().toISOString(),
        status: 'COMPLETED',
        totalSourceRecords: 2600,
        totalTargetRecords: 2600,
        exactMatches: 2554,
        toleranceMatches: 32,
        oneToManyMatches: 8,
        unmatchedRecords: 6,
        discrepanciesCount: 6,
        matchRatePercentage: 99.77,
      };
      reconRuns.unshift(newRun);
      return newRun;
    }
  );

  // Publish reconciliation completion event
  await eventBroker.publish('reconciliation.events', result.data, req.correlationId);

  return res.status(201).json({
    success: true,
    message: 'Autonomous 4-step deterministic reconciliation completed.',
    data: result.data,
    source: result.source,
  });
});

/**
 * GET /api/v1/reconciliation/discrepancies
 */
reconciliationRouter.get('/discrepancies', async (req: Request, res: Response) => {
  const result = await BackendClient.requestWithFallback(
    '/api/v1/reconciliation/discrepancies',
    {
      method: 'GET',
      correlationId: req.correlationId,
      directServiceUrl: config.services.reconciliation,
    },
    () => discrepancies
  );

  return res.json({ success: true, data: result.data, source: result.source });
});

/**
 * POST /api/v1/reconciliation/review
 */
reconciliationRouter.post('/review', async (req: Request, res: Response) => {
  const { discrepancyId, decision, comments } = req.body;

  const item = discrepancies.find((d) => d.id === Number(discrepancyId));
  if (item) {
    item.status = decision === 'APPROVE_MATCH' ? 'RESOLVED' : 'ESCALATED';
    item.notes = `${item.notes} | Reviewer note: ${comments || decision}`;
  }

  await eventBroker.publish('audit.events', {
    action: 'RECONCILIATION_REVIEW',
    actor: req.user?.username || 'analyst',
    serviceName: 'reconciliation-service',
    entityType: 'Discrepancy',
    entityId: String(discrepancyId),
    outcome: decision,
    correlationId: req.correlationId,
    details: `Decision: ${decision}. ${comments || ''}`,
  }, req.correlationId);

  return res.json({
    success: true,
    message: `Discrepancy review decision '${decision}' successfully recorded.`,
    data: item || { discrepancyId, decision, status: 'PROCESSED' },
  });
});

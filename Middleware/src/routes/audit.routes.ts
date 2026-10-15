import { Router, Request, Response } from 'express';
import { BackendClient } from '../services/backendClient';
import { config } from '../config/env';

export const auditRouter = Router();

const auditLogs = [
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

/**
 * GET /api/v1/audit/events
 */
auditRouter.get('/events', async (req: Request, res: Response) => {
  const result = await BackendClient.requestWithFallback(
    '/api/v1/audit/events',
    {
      method: 'GET',
      correlationId: req.correlationId,
      directServiceUrl: config.services.auditReporting,
    },
    () => auditLogs
  );

  return res.json({ success: true, data: result.data, source: result.source });
});

/**
 * POST /api/v1/audit/reports
 */
auditRouter.post('/reports', (req: Request, res: Response) => {
  const { reportType, fromDate, toDate } = req.body;

  const report = {
    reportId: `RPT-SOX-${Date.now().toString().slice(-6)}`,
    reportType: reportType || 'SOX_COMPLIANCE_AUDIT',
    generatedAt: new Date().toISOString(),
    status: 'READY_FOR_DOWNLOAD',
    period: { fromDate: fromDate || '2026-10-01', toDate: toDate || '2026-10-15' },
    summary: {
      totalAuditedMutations: 12480,
      verifiedActorSignatures: '100% (ChaCha20-Poly1305 / HMAC-SHA256)',
      unauthorizedAttemptsBlocked: 14,
    },
    downloadUrl: `/api/v1/audit/reports/download/RPT-SOX-${Date.now().toString().slice(-6)}.pdf`,
  };

  return res.status(201).json({ success: true, data: report });
});

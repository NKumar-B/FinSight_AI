import { Router, Request, Response } from 'express';
import { BackendClient } from '../services/backendClient';
import { eventBroker } from '../services/eventBroker';
import { config } from '../config/env';

export const fraudRouter = Router();

let fraudAlerts = [
  {
    id: 1,
    alertReference: 'ALT-AML-2026-0039',
    transactionId: 102,
    accountNumber: 'ACC-10029384',
    amount: 98500.0,
    currency: 'USD',
    riskScore: 92,
    severity: 'CRITICAL',
    status: 'OPEN',
    detectionRule: 'High Velocity Spike & High-Risk Offshore Destination',
    reasons: [
      'Velocity exceeds 4x baseline for operating account ACC-10029384',
      'Destination jurisdiction flagged on FATF grey list monitor',
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

let investigations = [
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

let evidenceItems = [
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

/**
 * GET /api/v1/fraud/alerts
 */
fraudRouter.get('/alerts', async (req: Request, res: Response) => {
  const result = await BackendClient.requestWithFallback(
    '/api/v1/fraud/alerts',
    {
      method: 'GET',
      correlationId: req.correlationId,
      directServiceUrl: config.services.fraudDetection,
    },
    () => fraudAlerts
  );

  return res.json({ success: true, data: result.data, source: result.source });
});

/**
 * GET /api/v1/fraud/rules
 */
fraudRouter.get('/rules', async (req: Request, res: Response) => {
  const rules = [
    { ruleCode: 'HIGH_AMOUNT_ANOMALY', weight: 35, threshold: 50000, description: 'Single transaction exceeding high-value threshold' },
    { ruleCode: 'HIGH_VELOCITY_SPIKE', weight: 30, threshold: 4, description: 'Rapid succession of debit operations within rolling 1-hour window' },
    { ruleCode: 'CROSS_BORDER_ANOMALY', weight: 25, threshold: 1, description: 'Funds routing into or through FATF high-risk jurisdictions' },
    { ruleCode: 'SUSPICIOUS_ROUND_AMOUNT', weight: 20, threshold: 10000, description: 'Unusually round structured amounts (e.g., exactly $10,000.00)' },
    { ruleCode: 'CONNECTED_ACCOUNTS_CYCLE', weight: 40, threshold: 1, description: 'Circular loop funding pattern across multiple affiliated accounts' },
  ];
  return res.json({ success: true, data: rules });
});

/**
 * POST /api/v1/fraud/evaluate
 * Evaluates real-time risk score across multi-factor rule matrix
 */
fraudRouter.post('/evaluate', async (req: Request, res: Response) => {
  const txn = req.body;
  const amount = Number(txn.amount || 0);

  let riskScore = 15;
  const triggeredRules: string[] = [];

  if (amount > 50000) {
    riskScore += 35;
    triggeredRules.push('HIGH_AMOUNT_ANOMALY (Weight: 35)');
  }
  if (txn.destinationCountry && txn.destinationCountry !== 'US') {
    riskScore += 25;
    triggeredRules.push('CROSS_BORDER_ANOMALY (Weight: 25)');
  }
  if (amount % 1000 === 0 && amount >= 10000) {
    riskScore += 20;
    triggeredRules.push('SUSPICIOUS_ROUND_AMOUNT (Weight: 20)');
  }

  const severity = riskScore >= 80 ? 'CRITICAL' : riskScore >= 60 ? 'HIGH' : riskScore >= 40 ? 'MEDIUM' : 'LOW';

  const evaluationResult = {
    evaluatedTransactionId: txn.id || txn.referenceNumber || 'TXN-DEMO',
    riskScore: Math.min(riskScore, 100),
    severity,
    recommendation: riskScore >= 75 ? 'FREEZE_AND_ESCALATE' : riskScore >= 50 ? 'MANUAL_REVIEW' : 'AUTO_CLEAR',
    triggeredRules,
    evaluatedAt: new Date().toISOString(),
  };

  // If critical, generate alert and publish to broker
  if (riskScore >= 75) {
    const alert = {
      id: fraudAlerts.length + 1,
      alertReference: `ALT-AML-2026-${String(fraudAlerts.length + 40).padStart(4, '0')}`,
      transactionId: txn.id || 105,
      accountNumber: txn.accountNumber || 'ACC-10029384',
      amount,
      currency: txn.currency || 'USD',
      riskScore,
      severity,
      status: 'OPEN',
      detectionRule: triggeredRules.join(' + '),
      reasons: triggeredRules,
      detectedAt: new Date().toISOString(),
    };
    fraudAlerts.unshift(alert);
    await eventBroker.publish('fraud.alerts', alert, req.correlationId);
  }

  return res.json({
    success: true,
    data: evaluationResult,
  });
});

/**
 * GET /api/v1/investigations
 */
fraudRouter.get('/investigations', async (req: Request, res: Response) => {
  return res.json({ success: true, data: investigations });
});

/**
 * GET /api/v1/investigations/:caseId/evidence
 */
fraudRouter.get('/investigations/:caseId/evidence', async (req: Request, res: Response) => {
  const caseId = Number(req.params.caseId);
  const evidence = evidenceItems.filter((e) => e.caseId === caseId);
  return res.json({ success: true, data: evidence.length > 0 ? evidence : evidenceItems });
});

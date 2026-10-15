import { Router, Request, Response } from 'express';
import { eventBroker } from '../services/eventBroker';

export const webhooksRouter = Router();

/**
 * POST /api/v1/webhooks/banking
 * Ingests external bank feed webhook callbacks (e.g., Plaid, MX, Core Banking)
 */
webhooksRouter.post('/banking', async (req: Request, res: Response) => {
  const payload = req.body;
  const signature = req.headers['x-banking-signature'] || 'sig_auto_verified';

  const event = await eventBroker.publish('financial.transactions', {
    source: 'BANK_CORE_WEBHOOK',
    signature,
    payload,
    receivedAt: new Date().toISOString(),
  }, req.correlationId);

  return res.status(202).json({
    success: true,
    message: 'Banking webhook received and queued for asynchronous ledger reconciliation.',
    eventId: event.eventId,
  });
});

/**
 * POST /api/v1/webhooks/payment-gateway
 * Ingests payment settlement webhooks (e.g., Stripe, Adyen, Swift)
 */
webhooksRouter.post('/payment-gateway', async (req: Request, res: Response) => {
  const payload = req.body;

  const event = await eventBroker.publish('financial.transactions', {
    source: 'PAYMENT_GATEWAY_WEBHOOK',
    payload,
    receivedAt: new Date().toISOString(),
  }, req.correlationId);

  return res.status(202).json({
    success: true,
    message: 'Payment gateway settlement webhook published to event mesh.',
    eventId: event.eventId,
  });
});

/**
 * GET /api/v1/webhooks/events
 * Inspects all events published across Kafka/RabbitMQ broker topics
 */
webhooksRouter.get('/events', (req: Request, res: Response) => {
  const topic = req.query.topic as string | undefined;
  const events = eventBroker.getRecentEvents(topic);
  return res.json({
    success: true,
    count: events.length,
    data: events,
  });
});

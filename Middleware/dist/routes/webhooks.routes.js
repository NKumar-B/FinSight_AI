"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.webhooksRouter = void 0;
const express_1 = require("express");
const eventBroker_1 = require("../services/eventBroker");
exports.webhooksRouter = (0, express_1.Router)();
/**
 * POST /api/v1/webhooks/banking
 * Ingests external bank feed webhook callbacks (e.g., Plaid, MX, Core Banking)
 */
exports.webhooksRouter.post('/banking', async (req, res) => {
    const payload = req.body;
    const signature = req.headers['x-banking-signature'] || 'sig_auto_verified';
    const event = await eventBroker_1.eventBroker.publish('financial.transactions', {
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
exports.webhooksRouter.post('/payment-gateway', async (req, res) => {
    const payload = req.body;
    const event = await eventBroker_1.eventBroker.publish('financial.transactions', {
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
exports.webhooksRouter.get('/events', (req, res) => {
    const topic = req.query.topic;
    const events = eventBroker_1.eventBroker.getRecentEvents(topic);
    return res.json({
        success: true,
        count: events.length,
        data: events,
    });
});

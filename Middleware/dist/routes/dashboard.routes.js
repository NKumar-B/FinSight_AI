"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.dashboardRouter = void 0;
const express_1 = require("express");
const cacheService_1 = require("../services/cacheService");
exports.dashboardRouter = (0, express_1.Router)();
/**
 * GET /api/v1/dashboard/summary
 * High-Performance BFF Composite Aggregation Endpoint for React Dashboard
 */
exports.dashboardRouter.get('/summary', async (req, res) => {
    const cached = await cacheService_1.cacheService.get('dashboard_summary');
    if (cached) {
        return res.json({ success: true, data: cached, source: 'REDIS_CACHE' });
    }
    const compositeDashboard = {
        metrics: {
            treasuryLiquidity: 2325630.5,
            treasuryGrowthPercentage: 8.4,
            reconMatchRate: 99.51,
            discrepanciesFlagged: 12,
            openFraudAlerts: 2,
            criticalFraudAlerts: 1,
            activeAiAgents: 9,
            totalAiAgents: 9,
        },
        volumeTrend: [
            { day: 'Mon', volume: 420000, reconciled: 418000 },
            { day: 'Tue', volume: 580000, reconciled: 578000 },
            { day: 'Wed', volume: 490000, reconciled: 489000 },
            { day: 'Thu', volume: 720000, reconciled: 715000 },
            { day: 'Fri', volume: 940000, reconciled: 932000 },
            { day: 'Sat', volume: 380000, reconciled: 379500 },
            { day: 'Sun', volume: 290000, reconciled: 289900 },
        ],
        riskMatrix: [
            { severity: 'Critical', count: 1, color: '#dc2626' },
            { severity: 'High', count: 2, color: '#ea580c' },
            { severity: 'Medium', count: 4, color: '#f59e0b' },
            { severity: 'Low', count: 7, color: '#2563eb' },
        ],
        expenseBreakdown: [
            { name: 'Cloud Infrastructure', value: 84500, color: '#2563eb' },
            { name: 'Payroll & HR', value: 68200, color: '#10b981' },
            { name: 'Advisory & Legal', value: 39400, color: '#f59e0b' },
            { name: 'SaaS Subscriptions', value: 28900, color: '#8b5cf6' },
        ],
        recentAgentStream: [
            {
                runId: 'RUN-AT-8821-01',
                agent: 'Reconciliation Agent',
                status: 'COMPLETED',
                summary: 'Successfully matched 42 tolerance records within $0.50 threshold and flagged 12 true discrepancies.',
                timestamp: 'Oct 14, 2026, 01:30:00 PM',
            },
            {
                runId: 'RUN-AI-8821-02',
                agent: 'Fraud Detection Agent',
                status: 'COMPLETED',
                summary: 'Risk score 92/100 calculated. Identified FATF grey list destination entity.',
                timestamp: 'Oct 14, 2026, 05:12:00 PM',
            },
        ],
        gatewayStatus: {
            apiGateway: 'HEALTHY (:8080)',
            eurekaRegistry: 'SYNCHRONIZED (:8761)',
            middlewareBff: 'ONLINE (:5000)',
            cryptoEngine: 'CHACHA20-POLY1305 ACTIVE',
            timestamp: new Date().toISOString(),
        },
    };
    await cacheService_1.cacheService.set('dashboard_summary', compositeDashboard, 30);
    return res.json({
        success: true,
        data: compositeDashboard,
    });
});

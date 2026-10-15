"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.apiRouter = void 0;
const express_1 = require("express");
const auth_routes_1 = require("./auth.routes");
const financial_routes_1 = require("./financial.routes");
const reconciliation_routes_1 = require("./reconciliation.routes");
const fraud_routes_1 = require("./fraud.routes");
const ai_routes_1 = require("./ai.routes");
const insights_routes_1 = require("./insights.routes");
const audit_routes_1 = require("./audit.routes");
const dashboard_routes_1 = require("./dashboard.routes");
const webhooks_routes_1 = require("./webhooks.routes");
exports.apiRouter = (0, express_1.Router)();
exports.apiRouter.use('/auth', auth_routes_1.authRouter);
exports.apiRouter.use('/financial', financial_routes_1.financialRouter);
exports.apiRouter.use('/', financial_routes_1.financialRouter); // Maps /accounts and /transactions directly
exports.apiRouter.use('/reconciliation', reconciliation_routes_1.reconciliationRouter);
exports.apiRouter.use('/fraud', fraud_routes_1.fraudRouter);
exports.apiRouter.use('/', fraud_routes_1.fraudRouter); // Maps /investigations directly
exports.apiRouter.use('/ai', ai_routes_1.aiRouter);
exports.apiRouter.use('/insights', insights_routes_1.insightsRouter);
exports.apiRouter.use('/audit', audit_routes_1.auditRouter);
exports.apiRouter.use('/dashboard', dashboard_routes_1.dashboardRouter);
exports.apiRouter.use('/webhooks', webhooks_routes_1.webhooksRouter);

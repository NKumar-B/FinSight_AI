"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.correlationMiddleware = correlationMiddleware;
const crypto_1 = __importDefault(require("crypto"));
function correlationMiddleware(req, res, next) {
    const correlationId = req.headers['x-correlation-id'] || `c-${crypto_1.default.randomUUID()}`;
    req.correlationId = correlationId;
    res.setHeader('X-Correlation-ID', correlationId);
    const startTime = Date.now();
    res.on('finish', () => {
        const duration = Date.now() - startTime;
        console.log(`[Correlation: ${correlationId}] ${req.method} ${req.originalUrl} -> ${res.statusCode} (${duration}ms)`);
    });
    next();
}

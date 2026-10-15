"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.createRateLimiter = createRateLimiter;
const ipHits = new Map();
function createRateLimiter(windowMs = 60000, maxRequests = 100) {
    return (req, res, next) => {
        const ip = req.ip || req.socket.remoteAddress || 'unknown';
        const now = Date.now();
        const record = ipHits.get(ip);
        if (!record || now > record.resetTime) {
            ipHits.set(ip, { count: 1, resetTime: now + windowMs });
            return next();
        }
        record.count += 1;
        if (record.count > maxRequests) {
            const retryAfter = Math.ceil((record.resetTime - now) / 1000);
            res.setHeader('Retry-After', retryAfter);
            return res.status(429).json({
                success: false,
                error: {
                    code: 'RATE_LIMIT_EXCEEDED',
                    message: `Too many requests. Please retry in ${retryAfter} seconds.`,
                    correlationId: req.correlationId,
                },
            });
        }
        next();
    };
}

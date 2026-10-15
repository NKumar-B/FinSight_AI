import express from 'express';
import cors from 'cors';
import { config } from './config/env';
import { correlationMiddleware } from './middleware/correlation';
import { authGuard } from './middleware/authGuard';
import { createRateLimiter } from './middleware/rateLimiter';
import { notFoundHandler, globalErrorHandler } from './middleware/errorHandler';
import { apiRouter } from './routes';

const app = express();

// Security & Cross-Origin Resource Sharing (CORS)
app.use(
  cors({
    origin: '*',
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization', 'X-Correlation-ID', 'X-Banking-Signature'],
    exposedHeaders: ['X-Correlation-ID', 'Retry-After'],
  })
);

// Body Parsers
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Distributed Tracing & Correlation Header
app.use(correlationMiddleware);

// Global JWT Extraction & Authentication Context
app.use(authGuard);

// Global Sliding Window Rate Limiting (120 req / min)
app.use(createRateLimiter(60000, 120));

// Root landing & info
app.get('/', (req, res) => {
  if (req.headers.accept?.includes('text/html')) {
    return res.send(`
      <!DOCTYPE html>
      <html>
        <head>
          <title>FinSight AI — Middleware &amp; BFF Gateway</title>
          <style>
            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #0B192C; color: #F8FAFC; padding: 40px; margin: 0; }
            .card { background: #1E293B; border: 1px solid #334155; border-radius: 12px; padding: 24px; max-width: 680px; margin: 0 auto; box-shadow: 0 10px 25px rgba(0,0,0,0.5); }
            h1 { color: #38BDF8; margin-top: 0; font-size: 22px; }
            .badge { display: inline-block; background: #059669; color: white; padding: 4px 10px; border-radius: 999px; font-weight: bold; font-size: 11px; }
            .endpoint { background: #0F172A; padding: 8px 12px; border-radius: 6px; font-family: monospace; font-size: 13px; margin: 6px 0; color: #94A3B8; }
            a { color: #38BDF8; text-decoration: none; }
            a:hover { text-decoration: underline; }
            .btn { display: inline-block; background: #2563EB; color: white; padding: 10px 18px; border-radius: 6px; font-weight: bold; margin-top: 15px; }
          </style>
        </head>
        <body>
          <div class="card">
            <span class="badge">MIDDLEWARE STATUS: ONLINE (:5000)</span>
            <h1>FinSight AI — Middleware &amp; API Mediation Layer</h1>
            <p>High-throughput Backend-For-Frontend (BFF) connecting Frontend web client with Spring Boot microservices.</p>
            <hr style="border: 0; border-top: 1px solid #334155; margin: 15px 0;">
            <p><strong>Health Check:</strong> <a href="/health">/health</a></p>
            <p><strong>Target Backend Gateway:</strong> <code>http://localhost:8080</code></p>
            <p><strong>Frontend Application:</strong> <a href="http://localhost:3000">http://localhost:3000</a></p>
            <h3>Active Endpoints:</h3>
            <div class="endpoint">GET <a href="/api/v1/dashboard/summary">/api/v1/dashboard/summary</a> (BFF Composite)</div>
            <div class="endpoint">GET <a href="/api/v1/accounts">/api/v1/accounts</a></div>
            <div class="endpoint">GET <a href="/api/v1/transactions">/api/v1/transactions</a></div>
            <div class="endpoint">GET <a href="/api/v1/reconciliation/runs">/api/v1/reconciliation/runs</a></div>
            <div class="endpoint">GET <a href="/api/v1/fraud/alerts">/api/v1/fraud/alerts</a></div>
            <div class="endpoint">GET <a href="/api/v1/ai/agents">/api/v1/ai/agents</a></div>
            <div class="endpoint">GET <a href="/api/v1/webhooks/events">/api/v1/webhooks/events</a></div>
            <br>
            <a href="http://localhost:3000" class="btn">Open FinSight AI Web Application &rarr;</a>
          </div>
        </body>
      </html>
    `);
  }

  res.json({
    status: 'ONLINE',
    service: 'finsight-middleware-bff',
    version: '1.0.0',
    port: config.port,
    frontendUrl: 'http://localhost:3000',
    backendGatewayUrl: config.backendGatewayUrl,
    healthEndpoint: '/health',
    apiBase: '/api/v1',
    correlationId: req.correlationId,
    timestamp: new Date().toISOString(),
  });
});

// Health Checks
app.get(['/health', '/healthz'], (req, res) => {
  res.json({
    status: 'UP',
    service: 'finsight-middleware-bff',
    environment: config.nodeEnv,
    port: config.port,
    correlationId: req.correlationId,
    timestamp: new Date().toISOString(),
    backendGatewayTarget: config.backendGatewayUrl,
  });
});

// Mount All Domain Routes under /api/v1
app.use('/api/v1', apiRouter);

// Catch-All 404 & Global Error Handlers
app.use(notFoundHandler);
app.use(globalErrorHandler);

// Start Server
if (process.env.NODE_ENV !== 'test') {
  app.listen(config.port, () => {
    console.log(`================================================================`);
    console.log(`🚀 FinSight AI - Distributed Middleware & BFF Server ONLINE`);
    console.log(`📡 Listening on: http://localhost:${config.port}`);
    console.log(`🔗 API Base: http://localhost:${config.port}/api/v1`);
    console.log(`🎯 Routing to Backend Gateway: ${config.backendGatewayUrl}`);
    console.log(`🩺 Health: http://localhost:${config.port}/health`);
    console.log(`================================================================`);
  });
}

export default app;

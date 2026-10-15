import { Request, Response, NextFunction } from 'express';

export function notFoundHandler(req: Request, res: Response) {
  res.status(404).json({
    success: false,
    error: {
      code: 'ROUTE_NOT_FOUND',
      message: `The endpoint ${req.method} ${req.originalUrl} does not exist on the FinSight AI Middleware.`,
      correlationId: req.correlationId,
      timestamp: new Date().toISOString(),
    },
  });
}

export function globalErrorHandler(err: any, req: Request, res: Response, next: NextFunction) {
  console.error(`[Error] [Correlation: ${req.correlationId}]`, err);

  const status = err.status || err.statusCode || 500;
  const message = err.message || 'Internal Server Error encountered in Middleware layer.';
  const code = err.code || 'INTERNAL_SERVER_ERROR';

  res.status(status).json({
    success: false,
    error: {
      code,
      message,
      correlationId: req.correlationId,
      timestamp: new Date().toISOString(),
      ...(process.env.NODE_ENV !== 'production' && { stack: err.stack }),
    },
  });
}

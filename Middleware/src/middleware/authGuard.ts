import { Request, Response, NextFunction } from 'express';
import crypto from 'crypto';
import { config } from '../config/env';

export interface AuthUser {
  userId: string;
  email: string;
  username: string;
  roles: string[];
}

declare global {
  namespace Express {
    interface Request {
      user?: AuthUser;
    }
  }
}

export function authGuard(req: Request, res: Response, next: NextFunction) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return next(); // Public or optional routes can proceed; strict routes use requireAuth
  }

  const token = authHeader.substring(7);
  try {
    const parts = token.split('.');
    if (parts.length === 3) {
      const payloadJson = Buffer.from(parts[1], 'base64url').toString('utf8');
      const payload = JSON.parse(payloadJson);
      
      req.user = {
        userId: payload.sub || payload.userId || 'usr_anonymous',
        email: payload.email || '',
        username: payload.preferred_username || payload.sub || 'user',
        roles: Array.isArray(payload.roles) ? payload.roles : (payload.role ? [payload.role] : ['ROLE_USER']),
      };
    }
  } catch (err) {
    console.warn(`[AuthGuard] Failed to parse JWT payload:`, err);
  }

  next();
}

export function requireAuth(req: Request, res: Response, next: NextFunction) {
  if (!req.user) {
    return res.status(401).json({
      success: false,
      error: {
        code: 'UNAUTHORIZED',
        message: 'Valid JWT Bearer token is required to access this resource.',
        correlationId: req.correlationId,
      },
    });
  }
  next();
}

export function requireRoles(...allowedRoles: string[]) {
  return (req: Request, res: Response, next: NextFunction) => {
    if (!req.user) {
      return res.status(401).json({
        success: false,
        error: {
          code: 'UNAUTHORIZED',
          message: 'Authentication required.',
          correlationId: req.correlationId,
        },
      });
    }

    const hasRole = req.user.roles.some((r) => allowedRoles.includes(r) || r === 'ROLE_ADMIN');
    if (!hasRole) {
      return res.status(403).json({
        success: false,
        error: {
          code: 'FORBIDDEN',
          message: `Access denied. Requires one of roles: [${allowedRoles.join(', ')}]`,
          correlationId: req.correlationId,
        },
      });
    }

    next();
  };
}

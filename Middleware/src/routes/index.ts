import { Router } from 'express';
import { authRouter } from './auth.routes';
import { financialRouter } from './financial.routes';
import { reconciliationRouter } from './reconciliation.routes';
import { fraudRouter } from './fraud.routes';
import { aiRouter } from './ai.routes';
import { insightsRouter } from './insights.routes';
import { auditRouter } from './audit.routes';
import { dashboardRouter } from './dashboard.routes';
import { webhooksRouter } from './webhooks.routes';

export const apiRouter = Router();

apiRouter.use('/auth', authRouter);
apiRouter.use('/financial', financialRouter);
apiRouter.use('/', financialRouter); // Maps /accounts and /transactions directly
apiRouter.use('/reconciliation', reconciliationRouter);
apiRouter.use('/fraud', fraudRouter);
apiRouter.use('/', fraudRouter); // Maps /investigations directly
apiRouter.use('/ai', aiRouter);
apiRouter.use('/insights', insightsRouter);
apiRouter.use('/audit', auditRouter);
apiRouter.use('/dashboard', dashboardRouter);
apiRouter.use('/webhooks', webhooksRouter);

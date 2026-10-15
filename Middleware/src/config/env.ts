import dotenv from 'dotenv';
import path from 'path';

// Load root .env or local .env
dotenv.config({ path: path.resolve(__dirname, '../../../.env') });
dotenv.config();

export const config = {
  port: parseInt(process.env.PORT || '5000', 10),
  nodeEnv: process.env.NODE_ENV || 'development',
  
  // Spring Boot API Gateway & Domain Microservice Ports
  backendGatewayUrl: process.env.BACKEND_GATEWAY_URL || 'http://localhost:8080',
  services: {
    auth: process.env.AUTH_SERVICE_URL || 'http://localhost:8081',
    financialData: process.env.FINANCIAL_DATA_SERVICE_URL || 'http://localhost:8082',
    reconciliation: process.env.RECONCILIATION_SERVICE_URL || 'http://localhost:8083',
    fraudDetection: process.env.FRAUD_DETECTION_SERVICE_URL || 'http://localhost:8084',
    auditReporting: process.env.AUDIT_REPORTING_SERVICE_URL || 'http://localhost:8085',
    financialInsights: process.env.FINANCIAL_INSIGHTS_SERVICE_URL || 'http://localhost:8086',
    aiOrchestrator: process.env.AI_ORCHESTRATOR_SERVICE_URL || 'http://localhost:8087',
    eurekaServer: process.env.EUREKA_SERVER_URL || 'http://localhost:8761/eureka/',
  },

  // Security & Crypto
  jwtSecret: process.env.FINSIGHT_JWT_SECRET || '404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970',
  jwtExpirationMs: parseInt(process.env.FINSIGHT_JWT_EXPIRATION_MS || '86400000', 10),
  chachaKey: process.env.FINSIGHT_CHACHA_KEY || 'V1FkM1Z1c2d4ejF1N3g5dTh5NHMyMXE3dzl6Mng1djg=',
  otpExpirationMinutes: parseInt(process.env.OTP_EXPIRATION_MINUTES || '5', 10),
  otpLength: parseInt(process.env.OTP_LENGTH || '6', 10),

  // Redis
  redisHost: process.env.REDIS_HOST || 'localhost',
  redisPort: parseInt(process.env.REDIS_PORT || '6379', 10),
};

import crypto from 'crypto';
import { config } from '../config/env';

export class CryptoService {
  /**
   * Generates a SHA-256 idempotency hash for transaction de-duplication
   */
  static generateIdempotencyKey(data: Record<string, any>): string {
    const raw = `${data.accountNumber || ''}_${data.amount || ''}_${data.currency || ''}_${data.timestamp || ''}_${data.counterpartyAccount || ''}`;
    return crypto.createHash('sha256').update(raw).digest('hex');
  }

  /**
   * Generates a cryptographically secure numeric OTP
   */
  static generateOtp(length: number = 6): string {
    const min = Math.pow(10, length - 1);
    const max = Math.pow(10, length) - 1;
    return crypto.randomInt(min, max + 1).toString();
  }

  /**
   * ChaCha20-Poly1305 simulation encryption tag
   */
  static encryptChaCha20Tag(payload: string): string {
    const iv = crypto.randomBytes(12);
    const cipher = crypto.createCipheriv('aes-256-gcm', crypto.createHash('sha256').update(config.chachaKey).digest(), iv);
    let encrypted = cipher.update(payload, 'utf8', 'hex');
    encrypted += cipher.final('hex');
    const authTag = cipher.getAuthTag().toString('hex');
    return `chacha20:${iv.toString('hex')}:${authTag}:${encrypted}`;
  }
}

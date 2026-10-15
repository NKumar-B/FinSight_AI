"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.CryptoService = void 0;
const crypto_1 = __importDefault(require("crypto"));
const env_1 = require("../config/env");
class CryptoService {
    /**
     * Generates a SHA-256 idempotency hash for transaction de-duplication
     */
    static generateIdempotencyKey(data) {
        const raw = `${data.accountNumber || ''}_${data.amount || ''}_${data.currency || ''}_${data.timestamp || ''}_${data.counterpartyAccount || ''}`;
        return crypto_1.default.createHash('sha256').update(raw).digest('hex');
    }
    /**
     * Generates a cryptographically secure numeric OTP
     */
    static generateOtp(length = 6) {
        const min = Math.pow(10, length - 1);
        const max = Math.pow(10, length) - 1;
        return crypto_1.default.randomInt(min, max + 1).toString();
    }
    /**
     * ChaCha20-Poly1305 simulation encryption tag
     */
    static encryptChaCha20Tag(payload) {
        const iv = crypto_1.default.randomBytes(12);
        const cipher = crypto_1.default.createCipheriv('aes-256-gcm', crypto_1.default.createHash('sha256').update(env_1.config.chachaKey).digest(), iv);
        let encrypted = cipher.update(payload, 'utf8', 'hex');
        encrypted += cipher.final('hex');
        const authTag = cipher.getAuthTag().toString('hex');
        return `chacha20:${iv.toString('hex')}:${authTag}:${encrypted}`;
    }
}
exports.CryptoService = CryptoService;

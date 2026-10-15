"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.BackendClient = void 0;
const axios_1 = __importDefault(require("axios"));
const env_1 = require("../config/env");
class BackendClient {
    static client = axios_1.default.create({
        baseURL: env_1.config.backendGatewayUrl,
        timeout: 10000,
        headers: {
            'Content-Type': 'application/json',
        },
    });
    /**
     * Forwards a request to the Spring Boot microservices ecosystem.
     * If the service is running, it returns the real response.
     * If unreachable or erroring, it calls fallbackFactory to return high-fidelity data.
     */
    static async requestWithFallback(endpoint, options, fallbackFactory) {
        const url = options.directServiceUrl
            ? `${options.directServiceUrl}${endpoint}`
            : `${env_1.config.backendGatewayUrl}${endpoint}`;
        try {
            const response = await (0, axios_1.default)({
                url,
                method: options.method || 'GET',
                data: options.data,
                headers: {
                    'Content-Type': 'application/json',
                    ...(options.headers || {}),
                    ...(options.correlationId && { 'X-Correlation-ID': options.correlationId }),
                },
                timeout: 5000,
            });
            console.log(`[BackendClient] [SUCCESS] ${options.method || 'GET'} ${url} -> HTTP ${response.status}`);
            return {
                data: response.data?.data !== undefined ? response.data.data : response.data,
                source: 'SPRING_BOOT_BACKEND',
            };
        }
        catch (err) {
            console.warn(`[BackendClient] [FALLBACK] ${options.method || 'GET'} ${url} unavailable (${err.code || err.message}). Serving middleware fixture.`);
            return {
                data: fallbackFactory(),
                source: 'MIDDLEWARE_RESILIENT_FALLBACK',
            };
        }
    }
}
exports.BackendClient = BackendClient;

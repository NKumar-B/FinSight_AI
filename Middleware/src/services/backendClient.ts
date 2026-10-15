import axios, { AxiosRequestConfig } from 'axios';

import { config } from '../config/env';

export class BackendClient {
  private static client = axios.create({
    baseURL: config.backendGatewayUrl,
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
  static async requestWithFallback<T>(
    endpoint: string,
    options: {
      method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
      data?: any;
      headers?: Record<string, string>;
      correlationId?: string;
      directServiceUrl?: string;
    },
    fallbackFactory: () => T
  ): Promise<{ data: T; source: 'SPRING_BOOT_BACKEND' | 'MIDDLEWARE_RESILIENT_FALLBACK' }> {
    const url = options.directServiceUrl 
      ? `${options.directServiceUrl}${endpoint}` 
      : `${config.backendGatewayUrl}${endpoint}`;

    try {
      const response = await axios({
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
    } catch (err: any) {
      console.warn(
        `[BackendClient] [FALLBACK] ${options.method || 'GET'} ${url} unavailable (${err.code || err.message}). Serving middleware fixture.`
      );
      return {
        data: fallbackFactory(),
        source: 'MIDDLEWARE_RESILIENT_FALLBACK',
      };
    }
  }
}

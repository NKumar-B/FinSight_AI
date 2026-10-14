import axios, { AxiosError } from 'axios';

export interface FinSightApiError {
  message: string;
  correlationId?: string;
  statusCode?: number;
  fieldErrors?: Record<string, string>;
}

export function parseApiError(error: unknown): FinSightApiError {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as any;
    const message = data?.message || data?.error || error.message || 'An unexpected server error occurred';
    const correlationId = data?.correlationId || (error.response?.headers?.['x-correlation-id'] as string);
    const statusCode = error.response?.status;
    const fieldErrors = data?.fieldErrors || data?.errors;

    return {
      message,
      correlationId,
      statusCode,
      fieldErrors,
    };
  }

  if (error instanceof Error) {
    return {
      message: error.message,
    };
  }

  return {
    message: 'Unknown application error occurred',
  };
}

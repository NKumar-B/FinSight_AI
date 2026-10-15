import { describe, it, expect } from 'vitest';
import {
  validateFinancialDocument,
  SAMPLE_FIXTURES,
} from './bankDocumentValidator';

describe('Institutional Bank Document Validator & Ingestion Engine', () => {
  it('should successfully validate and extract a valid PDF bank statement (Chase)', async () => {
    const file = SAMPLE_FIXTURES.VALID_CHASE_PDF.createFile();
    const result = await validateFinancialDocument(file);

    expect(result.isValid).toBe(true);
    expect(result.documentType).toBe('PDF_BANK_STATEMENT');
    expect(result.detectedBankName).toContain('JPMorgan Chase');
    expect(result.detectedAccountNumber).toBe('ACC-10029384');
    expect(result.confidenceScore).toBeGreaterThanOrEqual(80);
    expect(result.extractedTransactions.length).toBeGreaterThan(0);
    expect(result.rawSummary?.totalCredits).toBeGreaterThan(0);
  });

  it('should successfully validate and parse a valid CSV bank ledger (Barclays)', async () => {
    const file = SAMPLE_FIXTURES.VALID_BARCLAYS_CSV.createFile();
    const result = await validateFinancialDocument(file);

    expect(result.isValid).toBe(true);
    expect(result.documentType).toBe('CSV_BANK_LEDGER');
    expect(result.detectedBankName).toContain('Barclays');
    expect(result.confidenceScore).toBeGreaterThanOrEqual(70);
    expect(result.extractedTransactions.length).toBe(5);
    expect(result.rawSummary?.rowCount).toBe(5);
  });

  it('should strictly REJECT a candidate resume PDF as a non-banking document', async () => {
    const file = SAMPLE_FIXTURES.INVALID_RESUME_PDF.createFile();
    const result = await validateFinancialDocument(file);

    expect(result.isValid).toBe(false);
    expect(result.documentType).toBe('INVALID_NON_FINANCIAL');
    expect(result.confidenceScore).toBeLessThan(50);
    expect(result.rejectionReasons).toBeDefined();
    expect(result.rejectionReasons?.[0]).toMatch(/Resume|Curriculum Vitae|personal/i);
    expect(result.extractedTransactions.length).toBe(0);
  });

  it('should strictly REJECT an HR employee roster CSV missing banking ledger columns', async () => {
    const file = SAMPLE_FIXTURES.INVALID_EMPLOYEE_CSV.createFile();
    const result = await validateFinancialDocument(file);

    expect(result.isValid).toBe(false);
    expect(result.documentType).toBe('INVALID_NON_FINANCIAL');
    expect(result.confidenceScore).toBeLessThan(50);
    expect(result.rejectionReasons).toBeDefined();
    expect(result.rejectionReasons?.join(' ')).toMatch(/Non-banking CSV|non-financial columns/i);
    expect(result.extractedTransactions.length).toBe(0);
  });

  it('should reject unsupported file extensions like .exe or .png', async () => {
    const fakeFile = new File(['binary'], 'malicious_app.exe', { type: 'application/octet-stream' });
    const result = await validateFinancialDocument(fakeFile);

    expect(result.isValid).toBe(false);
    expect(result.rejectionReasons?.[0]).toContain('Unsupported file format');
  });
});

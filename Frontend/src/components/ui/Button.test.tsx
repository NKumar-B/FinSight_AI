import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Button } from './Button';

describe('Button Component', () => {
  it('renders children correctly', () => {
    render(<Button>Authorize Wire</Button>);
    expect(screen.getByText('Authorize Wire')).toBeInTheDocument();
  });

  it('shows loading spinner when isLoading is true', () => {
    render(<Button isLoading>Processing</Button>);
    expect(screen.getByText('Processing')).toBeInTheDocument();
    expect(screen.getByRole('button')).toBeDisabled();
  });

  it('applies danger styling when variant is danger', () => {
    render(<Button variant="danger">Freeze Account</Button>);
    const button = screen.getByRole('button');
    expect(button.className).toContain('bg-red-600');
  });
});

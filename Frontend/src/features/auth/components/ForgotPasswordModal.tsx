import React, { useState } from 'react';
import { Modal } from '@/components/ui/Modal';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { authApi } from '../api/authApi';
import { Mail, Phone, KeyRound, CheckCircle, ArrowLeft } from 'lucide-react';

export interface ForgotPasswordModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const ForgotPasswordModal: React.FC<ForgotPasswordModalProps> = ({ isOpen, onClose }) => {
  const [step, setStep] = useState<'IDENTITY' | 'OTP' | 'RESET' | 'SUCCESS'>('IDENTITY');
  const [identity, setIdentity] = useState('');
  const [channel, setChannel] = useState<'EMAIL' | 'MOBILE'>('EMAIL');
  const [otp, setOtp] = useState('');
  const [generatedOtpHint, setGeneratedOtpHint] = useState<string | null>(null);
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleRequestOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!identity.trim()) {
      setError('Please provide your registered email or phone number');
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      const data = await authApi.forgotPassword(identity, channel);
      if (data.generatedOtp) {
        setGeneratedOtpHint(data.generatedOtp);
      }
      setStep('OTP');
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to dispatch verification OTP');
    } finally {
      setIsLoading(false);
    }
  };

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    if (otp.length < 6) {
      setError('Please enter the complete 6-digit verification code');
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      await authApi.verifyOtp(identity, otp);
      setStep('RESET');
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Invalid or expired OTP token');
    } finally {
      setIsLoading(false);
    }
  };

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (newPassword.length < 8) {
      setError('Password must contain at least 8 characters');
      return;
    }
    if (newPassword !== confirmPassword) {
      setError('Password confirmation does not match');
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      await authApi.resetPassword(identity, otp, newPassword, confirmPassword);
      setStep('SUCCESS');
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to update credentials');
    } finally {
      setIsLoading(false);
    }
  };

  const resetModal = () => {
    setStep('IDENTITY');
    setIdentity('');
    setOtp('');
    setGeneratedOtpHint(null);
    setNewPassword('');
    setConfirmPassword('');
    setError(null);
    onClose();
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={resetModal}
      title={
        step === 'SUCCESS'
          ? 'Password Reset Complete'
          : step === 'RESET'
          ? 'Create New Password'
          : step === 'OTP'
          ? 'Verify Security Code'
          : 'Self-Service Password Reset'
      }
      description={
        step === 'IDENTITY'
          ? 'Enter your registered identity to receive a 6-digit ChaCha20-authenticated OTP.'
          : step === 'OTP'
          ? `Enter the 6-digit code sent to ${identity}. Code valid for 5 minutes.`
          : step === 'RESET'
          ? 'Enter and confirm your new enterprise password.'
          : 'Your password has been successfully updated.'
      }
    >
      {error && (
        <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 text-xs text-red-700 font-medium">
          {error}
        </div>
      )}

      {/* Step 1: Request OTP */}
      {step === 'IDENTITY' && (
        <form onSubmit={handleRequestOtp} className="space-y-4">
          <div className="flex rounded-lg border border-slate-200 p-1 bg-slate-50 gap-1">
            <button
              type="button"
              onClick={() => setChannel('EMAIL')}
              className={`flex-1 flex items-center justify-center gap-2 py-1.5 text-xs font-semibold rounded-md transition-all ${
                channel === 'EMAIL' ? 'bg-white shadow text-slate-800' : 'text-slate-500 hover:text-slate-700'
              }`}
            >
              <Mail className="w-3.5 h-3.5" />
              Email Dispatch
            </button>
            <button
              type="button"
              onClick={() => setChannel('MOBILE')}
              className={`flex-1 flex items-center justify-center gap-2 py-1.5 text-xs font-semibold rounded-md transition-all ${
                channel === 'MOBILE' ? 'bg-white shadow text-slate-800' : 'text-slate-500 hover:text-slate-700'
              }`}
            >
              <Phone className="w-3.5 h-3.5" />
              Mobile SMS
            </button>
          </div>

          <Input
            label={channel === 'EMAIL' ? 'Registered Corporate Email' : 'Registered Mobile Number'}
            placeholder={channel === 'EMAIL' ? 'analyst@finsight.local' : '+1 (555) 019-2834'}
            value={identity}
            onChange={(e) => setIdentity(e.target.value)}
            leftIcon={channel === 'EMAIL' ? <Mail className="w-4 h-4" /> : <Phone className="w-4 h-4" />}
            required
          />

          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="outline" size="sm" onClick={resetModal}>
              Cancel
            </Button>
            <Button type="submit" size="sm" isLoading={isLoading}>
              Send Verification Code
            </Button>
          </div>
        </form>
      )}

      {/* Step 2: Verify OTP */}
      {step === 'OTP' && (
        <form onSubmit={handleVerifyOtp} className="space-y-4">
          {generatedOtpHint && (
            <div className="p-3 bg-brand-50 border border-brand-200 rounded-lg text-xs text-brand-800">
              <span className="font-bold">Dev Sandbox Code: </span>
              <span className="font-mono text-sm tracking-widest font-extrabold text-brand-600">
                {generatedOtpHint}
              </span>
            </div>
          )}

          <Input
            label="6-Digit Verification Code"
            placeholder="834912"
            maxLength={6}
            value={otp}
            onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
            leftIcon={<KeyRound className="w-4 h-4" />}
            className="text-center font-mono text-lg tracking-widest font-bold"
            required
          />

          <div className="flex items-center justify-between pt-2">
            <button
              type="button"
              onClick={() => setStep('IDENTITY')}
              className="text-xs text-slate-500 hover:text-slate-800 flex items-center gap-1 font-medium"
            >
              <ArrowLeft className="w-3 h-3" /> Change Identity
            </button>
            <div className="flex gap-2">
              <Button type="button" variant="outline" size="sm" onClick={resetModal}>
                Cancel
              </Button>
              <Button type="submit" size="sm" isLoading={isLoading}>
                Verify OTP
              </Button>
            </div>
          </div>
        </form>
      )}

      {/* Step 3: Set New Password */}
      {step === 'RESET' && (
        <form onSubmit={handleResetPassword} className="space-y-4">
          <Input
            label="New Enterprise Password"
            type="password"
            placeholder="••••••••••••"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            helperText="Minimum 8 characters with upper, lower, and numbers"
            required
          />

          <Input
            label="Confirm New Password"
            type="password"
            placeholder="••••••••••••"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
          />

          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="outline" size="sm" onClick={resetModal}>
              Cancel
            </Button>
            <Button type="submit" size="sm" isLoading={isLoading}>
              Update Password
            </Button>
          </div>
        </form>
      )}

      {/* Step 4: Success */}
      {step === 'SUCCESS' && (
        <div className="text-center py-6 space-y-4">
          <div className="w-12 h-12 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center mx-auto">
            <CheckCircle className="w-6 h-6" />
          </div>
          <div>
            <h4 className="text-sm font-semibold text-slate-800">Password Updated Successfully</h4>
            <p className="text-xs text-slate-500 mt-1">
              Your credentials have been securely updated. You may now log in to FinSight AI.
            </p>
          </div>
          <Button onClick={resetModal} className="w-full">
            Proceed to Login
          </Button>
        </div>
      )}
    </Modal>
  );
};

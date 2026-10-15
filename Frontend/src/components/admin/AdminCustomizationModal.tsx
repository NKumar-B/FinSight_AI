import React, { useState } from 'react';
import {
  Palette,
  Check,
  Sparkles,
  Sun,
  Moon,
  RotateCcw,
  Layers,
  Save,
  CheckCircle2,
  DollarSign,
  Globe,
  Sliders,
  Type,
} from 'lucide-react';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { useTheme, ACCENT_THEMES, AccentTheme } from '@/lib/theme';
import { useI18n } from '@/lib/i18n';

export interface AdminCustomizationModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const AdminCustomizationModal: React.FC<AdminCustomizationModalProps> = ({
  isOpen,
  onClose,
}) => {
  const {
    mode,
    toggleTheme,
    accent,
    setAccent,
    appName,
    setAppName,
    appTagline,
    setAppTagline,
    currency,
    setCurrency,
    resetToDefaults,
  } = useTheme();

  const { language, t } = useI18n();

  // Local draft states
  const [draftName, setDraftName] = useState(appName);
  const [draftTagline, setDraftTagline] = useState(appTagline);
  const [draftCurrency, setDraftCurrency] = useState(currency);
  const [draftAccent, setDraftAccent] = useState<AccentTheme>(accent);
  const [saveSuccess, setSaveSuccess] = useState(false);

  // Sync draft state when modal opens
  React.useEffect(() => {
    if (isOpen) {
      setDraftName(appName);
      setDraftTagline(appTagline);
      setDraftCurrency(currency);
      setDraftAccent(accent);
      setSaveSuccess(false);
    }
  }, [isOpen, appName, appTagline, currency, accent]);

  const handleSave = () => {
    setAppName(draftName.trim() || 'FinSight AI');
    setAppTagline(draftTagline.trim() || 'Enterprise Financial Intelligence');
    setCurrency(draftCurrency);
    setAccent(draftAccent);
    setSaveSuccess(true);
    setTimeout(() => {
      setSaveSuccess(false);
      onClose();
    }, 900);
  };

  const handleReset = () => {
    resetToDefaults();
    setDraftName('FinSight AI');
    setDraftTagline('Enterprise Multi-Agent Financial Intelligence & Fraud Surveillance');
    setDraftCurrency('USD');
    setDraftAccent(ACCENT_THEMES[0]);
    setSaveSuccess(true);
    setTimeout(() => setSaveSuccess(false), 1500);
  };

  const modalFooter = (
    <div className="w-full flex items-center justify-between">
      <Button
        type="button"
        variant="ghost"
        size="sm"
        leftIcon={<RotateCcw className="w-3.5 h-3.5" />}
        onClick={handleReset}
      >
        Reset Defaults
      </Button>

      <div className="flex items-center gap-2">
        <Button type="button" variant="outline" size="sm" onClick={onClose}>
          Cancel
        </Button>
        <Button
          type="button"
          size="sm"
          leftIcon={<Save className="w-3.5 h-3.5" />}
          onClick={handleSave}
          className="shadow-md"
        >
          Save & Apply Globally
        </Button>
      </div>
    </div>
  );

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Admin Platform Customizer & Branding Console"
      description="Customize the global application name, brand accent palette, base currency, and interface theme."
      maxWidth="xl"
      footer={modalFooter}
    >
      <div className="space-y-5 text-slate-800 dark:text-slate-100">
        {saveSuccess && (
          <div className="p-3 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-300 dark:border-emerald-800 rounded-xl text-xs font-bold text-emerald-800 dark:text-emerald-300 flex items-center gap-2 animate-fadeIn">
            <CheckCircle2 className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
            <span>Platform branding updated globally across all views and sessions!</span>
          </div>
        )}

        {/* Section 1: Application Name & Tagline */}
        <div className="space-y-3 p-4 rounded-xl bg-slate-50 dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700/60">
          <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-slate-700 dark:text-slate-300">
            <Type className="w-4 h-4 text-brand-600" />
            <span>Global App Name & Corporate Identity</span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <Input
              label="Application Name (Displayed Everywhere)"
              value={draftName}
              onChange={(e) => setDraftName(e.target.value)}
              placeholder="e.g. FinSight AI or Techurate Treasury"
              required
            />
            <Select
              label="Default Base Currency"
              value={draftCurrency}
              onChange={(e) => {
                const newCur = e.target.value;
                setDraftCurrency(newCur);
                setCurrency(newCur); // Live apply immediately
              }}
              options={[
                { value: 'USD', label: 'USD ($) — United States Dollar' },
                { value: 'EUR', label: 'EUR (€) — Eurozone' },
                { value: 'GBP', label: 'GBP (£) — British Pound' },
                { value: 'INR', label: 'INR (₹) — Indian Rupee' },
                { value: 'SGD', label: 'SGD ($) — Singapore Dollar' },
              ]}
            />
          </div>

          <Input
            label="Platform Tagline / Subtitle"
            value={draftTagline}
            onChange={(e) => setDraftTagline(e.target.value)}
            placeholder="e.g. Enterprise Multi-Agent Financial Intelligence & Fraud Surveillance"
          />
        </div>

        {/* Section 2: Primary Dynamic Brand Color Palette */}
        <div className="space-y-3 p-4 rounded-xl bg-slate-50 dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700/60">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-slate-700 dark:text-slate-300">
              <Palette className="w-4 h-4 text-brand-600" />
              <span>Brand Color Palette & Mode</span>
            </div>

            <Button
              size="sm"
              variant="outline"
              leftIcon={mode === 'dark' ? <Sun className="w-3.5 h-3.5 text-amber-400" /> : <Moon className="w-3.5 h-3.5" />}
              onClick={toggleTheme}
            >
              {mode === 'dark' ? 'Switch to Light' : 'Switch to Dark'}
            </Button>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5 pt-1">
            {ACCENT_THEMES.map((themeItem) => {
              const isSelected = draftAccent.id === themeItem.id;
              return (
                <button
                  key={themeItem.id}
                  type="button"
                  onClick={() => setDraftAccent(themeItem)}
                  className={`p-3 rounded-xl border text-left transition-all flex flex-col justify-between ${
                    isSelected
                      ? 'border-slate-900 dark:border-white shadow-md ring-2 ring-brand-500 bg-white dark:bg-slate-800'
                      : 'border-slate-200 dark:border-slate-700 hover:border-slate-400 bg-white/60 dark:bg-slate-800/60'
                  }`}
                >
                  <div className="flex items-center justify-between w-full mb-1.5">
                    <span
                      className="w-5 h-5 rounded-full shadow-inner flex items-center justify-center"
                      style={{ backgroundColor: themeItem.primary }}
                    >
                      {isSelected && <Check className="w-3 h-3 text-white" />}
                    </span>
                    {isSelected && (
                      <span className="text-[10px] font-bold uppercase tracking-wider text-brand-600 dark:text-brand-400">
                        Selected
                      </span>
                    )}
                  </div>
                  <div className="text-xs font-bold text-slate-900 dark:text-white">
                    {language === 'hi' ? themeItem.nameHi : themeItem.name}
                  </div>
                  <span className="text-[10px] font-mono text-slate-400">{themeItem.primary}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* Section 3: Live Preview Glass Card */}
        <div className="p-4 rounded-2xl bg-white/80 dark:bg-slate-900/80 backdrop-blur-md border border-slate-200/80 dark:border-white/10 shadow-lg space-y-3">
          <div className="text-[11px] font-bold uppercase tracking-wider text-slate-400 flex items-center justify-between">
            <span>Live Interactive Preview</span>
            <Badge variant="info" size="sm">Real-time Glassmorphism</Badge>
          </div>

          <div className="p-3.5 rounded-xl border border-slate-200 dark:border-slate-700/80 bg-slate-50/70 dark:bg-slate-800/50 flex flex-col gap-3">
            <div className="flex items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <div
                  className="w-9 h-9 rounded-xl flex items-center justify-center text-white shadow-md shadow-brand-500/20 shrink-0"
                  style={{ backgroundColor: draftAccent.primary }}
                >
                  <Layers className="w-5 h-5" />
                </div>
                <div>
                  <h4 className="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-1.5">
                    {draftName || 'FinSight AI'}
                    <span
                      className="text-[10px] font-extrabold px-1.5 py-0.2 rounded text-white"
                      style={{ backgroundColor: draftAccent.primary }}
                    >
                      PRO
                    </span>
                  </h4>
                  <p className="text-xs text-slate-500 dark:text-slate-400 truncate max-w-sm">
                    {draftTagline || 'Enterprise Multi-Agent Intelligence'}
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <button
                  type="button"
                  className="px-3 py-1.5 text-xs font-bold rounded-lg text-white shadow-sm transition-transform hover:scale-105 shrink-0"
                  style={{ backgroundColor: draftAccent.primary }}
                >
                  Action Button
                </button>
              </div>
            </div>

            {/* Currency Preview Pill */}
            <div className="flex items-center gap-2 pt-2 border-t border-slate-200/60 dark:border-slate-700/60 text-xs">
              <span className="text-[11px] font-semibold text-slate-500 dark:text-slate-400">
                Active Base Currency:
              </span>
              <Badge variant="success" size="sm">
                {draftCurrency}
              </Badge>
              <span className="text-slate-600 dark:text-slate-300 font-mono text-[11px]">
                Preview: {draftCurrency === 'INR' ? '₹23,25,630.50' : draftCurrency === 'EUR' ? '€2,325,630.50' : draftCurrency === 'GBP' ? '£2,325,630.50' : '$2,325,630.50'}
              </span>
            </div>
          </div>
        </div>
      </div>
    </Modal>
  );
};

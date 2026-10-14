import React from 'react';
import { Palette, Check, Sparkles, Sun, Moon, RotateCcw } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { useTheme, ACCENT_THEMES, AccentTheme } from '@/lib/theme';
import { useI18n } from '@/lib/i18n';
import { useAuth } from '@/features/auth/hooks/useAuth';

export const AdminThemeCustomizerCard: React.FC = () => {
  const { hasRole } = useAuth();
  const { mode, toggleTheme, accent, setAccent } = useTheme();
  const { language, t } = useI18n();

  // Show exclusively for administrators
  if (!hasRole('ROLE_ADMIN')) {
    return null;
  }

  const handleReset = () => {
    setAccent(ACCENT_THEMES[0]);
  };

  return (
    <Card
      title={
        <div className="flex items-center gap-2">
          <Palette className="w-4 h-4 text-brand-600" />
          <span>{t('themeCustomizerTitle')}</span>
          <Badge variant="info" size="sm">
            Admin Live Tool
          </Badge>
        </div>
      }
      subtitle={t('themeCustomizerDesc')}
      action={
        <div className="flex items-center gap-2">
          <Button
            size="sm"
            variant="outline"
            leftIcon={mode === 'dark' ? <Sun className="w-3.5 h-3.5 text-amber-400" /> : <Moon className="w-3.5 h-3.5" />}
            onClick={toggleTheme}
          >
            {mode === 'dark' ? 'Light Mode' : 'Dark Mode'}
          </Button>
          <Button
            size="sm"
            variant="ghost"
            leftIcon={<RotateCcw className="w-3 h-3" />}
            onClick={handleReset}
            title="Reset to default brand theme"
          >
            Reset
          </Button>
        </div>
      }
      className="border-2 border-brand-200/60 dark:border-brand-900/40 shadow-elevated"
    >
      <div className="space-y-4">
        {/* Color Palette Grid */}
        <div>
          <span className="text-xs font-semibold text-slate-700 dark:text-slate-300 block mb-2">
            Select Primary Dynamic Brand Accent:
          </span>
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
            {ACCENT_THEMES.map((themeItem) => {
              const isSelected = accent.id === themeItem.id;
              return (
                <button
                  key={themeItem.id}
                  onClick={() => setAccent(themeItem)}
                  className={`p-3 rounded-2xl border text-left transition-all relative flex flex-col items-start justify-between ${
                    isSelected
                      ? 'border-slate-900 dark:border-white shadow-md ring-2 ring-offset-2 ring-brand-500 bg-white dark:bg-slate-800'
                      : 'border-slate-200 dark:border-slate-700 hover:border-slate-400 bg-slate-50/60 dark:bg-slate-800/40'
                  }`}
                >
                  <div className="flex items-center justify-between w-full mb-2">
                    <span
                      className="w-6 h-6 rounded-full shadow-inner flex items-center justify-center"
                      style={{ backgroundColor: themeItem.primary }}
                    >
                      {isSelected && <Check className="w-3.5 h-3.5 text-white" />}
                    </span>
                    {isSelected && (
                      <span className="text-[10px] font-bold uppercase tracking-wider text-brand-600 dark:text-brand-400">
                        Active
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

        {/* Live Preview Elements Bar */}
        <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700/60 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <span
              className="w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-md"
              style={{ backgroundColor: accent.primary }}
            >
              <Sparkles className="w-5 h-5" />
            </span>
            <div>
              <div className="text-xs font-bold text-slate-900 dark:text-white">
                Live Dynamic Brand Preview
              </div>
              <div className="text-[11px] text-slate-500 dark:text-slate-400">
                Buttons, sidebar route active indicators, charts, and primary highlights instantly mirror this palette.
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              className="px-4 py-2 text-xs font-bold rounded-lg text-white shadow-sm transition-opacity hover:opacity-90"
              style={{ backgroundColor: accent.primary }}
            >
              Dynamic Button
            </button>
            <span
              className="px-2.5 py-1 text-xs font-semibold rounded-full border"
              style={{
                color: accent.primary,
                backgroundColor: accent.light,
                borderColor: accent.primary,
              }}
            >
              Accent Pill
            </span>
          </div>
        </div>
      </div>
    </Card>
  );
};

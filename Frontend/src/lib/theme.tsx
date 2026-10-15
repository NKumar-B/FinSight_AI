import React, { createContext, useContext, useState, useEffect } from 'react';

export type ThemeMode = 'light' | 'dark';

export interface AccentTheme {
  id: string;
  name: string;
  nameHi: string;
  primary: string;
  hover: string;
  light: string;
  ring: string;
}

export const ACCENT_THEMES: AccentTheme[] = [
  {
    id: 'sapphire',
    name: 'Sapphire Blue',
    nameHi: 'नीलम नीला',
    primary: '#2563eb',
    hover: '#1d4ed8',
    light: '#eff6ff',
    ring: 'rgba(37, 99, 235, 0.35)',
  },
  {
    id: 'emerald',
    name: 'Emerald Growth',
    nameHi: 'पन्ना हरा',
    primary: '#059669',
    hover: '#047857',
    light: '#ecfdf5',
    ring: 'rgba(5, 150, 105, 0.35)',
  },
  {
    id: 'violet',
    name: 'Royal Violet',
    nameHi: 'शाही बैंगनी',
    primary: '#7c3aed',
    hover: '#6d28d9',
    light: '#f5f3ff',
    ring: 'rgba(124, 58, 237, 0.35)',
  },
  {
    id: 'crimson',
    name: 'Crimson Risk',
    nameHi: 'गहरा लाल',
    primary: '#dc2626',
    hover: '#b91c1c',
    light: '#fef2f2',
    ring: 'rgba(220, 38, 38, 0.35)',
  },
  {
    id: 'amber',
    name: 'Executive Amber',
    nameHi: 'अंबर स्वर्ण',
    primary: '#d97706',
    hover: '#b45309',
    light: '#fffbeb',
    ring: 'rgba(217, 119, 6, 0.35)',
  },
  {
    id: 'cyan',
    name: 'Cyber Cyan',
    nameHi: 'साइबर नीला',
    primary: '#0891b2',
    hover: '#0e7490',
    light: '#ecfeff',
    ring: 'rgba(8, 145, 178, 0.35)',
  },
];

interface ThemeContextType {
  mode: ThemeMode;
  toggleTheme: () => void;
  accent: AccentTheme;
  setAccent: (accent: AccentTheme) => void;
  availableAccents: AccentTheme[];
  appName: string;
  setAppName: (name: string) => void;
  appTagline: string;
  setAppTagline: (tagline: string) => void;
  currency: string;
  currencySymbol: string;
  setCurrency: (currency: string) => void;
  resetToDefaults: () => void;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [mode, setMode] = useState<ThemeMode>(() => {
    const saved = localStorage.getItem('finsight_theme_mode');
    return (saved as ThemeMode) || 'light';
  });

  const [accent, setAccentState] = useState<AccentTheme>(() => {
    const savedId = localStorage.getItem('finsight_accent_theme');
    const match = ACCENT_THEMES.find((a) => a.id === savedId);
    return match || ACCENT_THEMES[0];
  });

  const [appName, setAppNameState] = useState<string>(() => {
    return localStorage.getItem('finsight_app_name') || 'FinSight AI';
  });

  const [appTagline, setAppTaglineState] = useState<string>(() => {
    return (
      localStorage.getItem('finsight_app_tagline') ||
      'Enterprise Multi-Agent Financial Intelligence & Fraud Surveillance'
    );
  });

  const [currency, setCurrencyState] = useState<string>(() => {
    return localStorage.getItem('finsight_currency') || 'USD';
  });

  const currencySymbol = (() => {
    switch (currency.toUpperCase()) {
      case 'INR':
        return '₹';
      case 'EUR':
        return '€';
      case 'GBP':
        return '£';
      case 'SGD':
        return 'S$';
      case 'JPY':
        return '¥';
      case 'USD':
      default:
        return '$';
    }
  })();

  const applyAccentCssVariables = (targetAccent: AccentTheme) => {
    const root = document.documentElement;
    root.style.setProperty('--brand-primary', targetAccent.primary);
    root.style.setProperty('--brand-hover', targetAccent.hover);
    root.style.setProperty('--brand-light', targetAccent.light);
    root.style.setProperty('--brand-ring', targetAccent.ring);
  };

  useEffect(() => {
    const root = document.documentElement;
    if (mode === 'dark') {
      root.classList.add('dark');
    } else {
      root.classList.remove('dark');
    }
    localStorage.setItem('finsight_theme_mode', mode);
  }, [mode]);

  useEffect(() => {
    applyAccentCssVariables(accent);
    localStorage.setItem('finsight_accent_theme', accent.id);
  }, [accent]);

  useEffect(() => {
    localStorage.setItem('finsight_app_name', appName);
    document.title = `${appName} — ${appTagline}`;
  }, [appName, appTagline]);

  useEffect(() => {
    localStorage.setItem('finsight_app_tagline', appTagline);
  }, [appTagline]);

  useEffect(() => {
    localStorage.setItem('finsight_currency', currency);
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('finsight_currency_updated', { detail: { currency } }));
    }
  }, [currency]);

  const toggleTheme = () => {
    setMode((prev) => (prev === 'light' ? 'dark' : 'light'));
  };

  const setAccent = (newAccent: AccentTheme) => {
    setAccentState(newAccent);
  };

  const setAppName = (name: string) => {
    setAppNameState(name);
  };

  const setAppTagline = (tagline: string) => {
    setAppTaglineState(tagline);
  };

  const setCurrency = (cur: string) => {
    setCurrencyState(cur);
    localStorage.setItem('finsight_currency', cur);
  };

  const resetToDefaults = () => {
    setMode('light');
    setAccentState(ACCENT_THEMES[0]);
    setAppNameState('FinSight AI');
    setAppTaglineState('Enterprise Multi-Agent Financial Intelligence & Fraud Surveillance');
    setCurrencyState('USD');
    localStorage.removeItem('finsight_theme_mode');
    localStorage.removeItem('finsight_accent_theme');
    localStorage.removeItem('finsight_app_name');
    localStorage.removeItem('finsight_app_tagline');
    localStorage.removeItem('finsight_currency');
  };

  return (
    <ThemeContext.Provider
      value={{
        mode,
        toggleTheme,
        accent,
        setAccent,
        availableAccents: ACCENT_THEMES,
        appName,
        setAppName,
        appTagline,
        setAppTagline,
        currency,
        currencySymbol,
        setCurrency,
        resetToDefaults,
      }}
    >
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = (): ThemeContextType => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};

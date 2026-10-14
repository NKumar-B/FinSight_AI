import React, { createContext, useContext, useState, useEffect } from 'react';

export type Language = 'en' | 'hi';

export interface Translations {
  [key: string]: {
    en: string;
    hi: string;
  };
}

export const DICTIONARY: Translations = {
  // Brand
  appName: { en: 'FinSight AI', hi: 'फिनसाइट एआई' },
  tagline: {
    en: 'Multi-Agent Financial Intelligence & Fraud Investigation Platform',
    hi: 'मल्टी-एजेंट वित्तीय बुद्धिमत्ता और धोखाधड़ी जांच मंच',
  },
  enterpriseFintech: { en: 'Enterprise Fintech', hi: 'एंटरप्राइज फिनटेक' },

  // Auth / Login / Register
  signInWorkspace: { en: 'Sign in to your workspace', hi: 'अपने कार्यक्षेत्र में साइन इन करें' },
  registerWorkspace: { en: 'Create Enterprise Account', hi: 'एंटरप्राइज खाता बनाएं' },
  protectedBy: {
    en: 'Protected by ChaCha20-Poly1305 encryption & Zero-Trust Gateway',
    hi: 'ChaCha20-Poly1305 एन्क्रिप्शन और जीरो-ट्रस्ट गेटवे द्वारा संरक्षित',
  },
  username: { en: 'Enterprise Username', hi: 'उपयोगकर्ता नाम' },
  password: { en: 'Password', hi: 'पासवर्ड' },
  fullName: { en: 'Full Legal Name', hi: 'पूरा कानूनी नाम' },
  email: { en: 'Corporate Email', hi: 'कॉर्पोरेट ईमेल' },
  selectRole: { en: 'Select Operational Role', hi: 'परिचालन भूमिका चुनें' },
  rememberWorkstation: { en: 'Remember workstation', hi: 'वर्कस्टेशन याद रखें' },
  forgotPassword: { en: 'Forgot password?', hi: 'पासवर्ड भूल गए?' },
  signInButton: { en: 'Sign In to FinSight AI', hi: 'फिनसाइट एआई में साइन इन करें' },
  registerButton: { en: 'Register with FinSight AI', hi: 'फिनसाइट एआई में पंजीकरण करें' },
  noAccountPrompt: { en: "Don't have an account? Register with us", hi: 'खाता नहीं है? हमारे साथ पंजीकरण करें' },
  haveAccountPrompt: { en: 'Already registered? Sign in to workspace', hi: 'पहले से पंजीकृत हैं? कार्यक्षेत्र में साइन इन करें' },

  // Two-Factor Verification
  twoFactorTitle: { en: 'Two-Factor Security Verification', hi: 'दो-चरणीय सुरक्षा सत्यापन' },
  twoFactorDesc: {
    en: 'Enter the 6-digit ChaCha20-authenticated code sent to your registered channel.',
    hi: 'अपने पंजीकृत चैनल पर भेजे गए 6-अंकों के ChaCha20-प्रमाणीकृत कोड को दर्ज करें।',
  },
  enterOtp: { en: '6-Digit Verification Code', hi: '6-अंकों का सत्यापन कोड' },
  verifyAndContinue: { en: 'Verify & Authorize Session', hi: 'सत्यापित करें और सत्र अधिकृत करें' },
  resendCode: { en: 'Resend Code', hi: 'कोड पुनः भेजें' },
  devHint: { en: 'Development Sandbox Code', hi: 'डेवलपमेंट सैंडबॉक्स कोड' },

  // Navigation Items
  dashboard: { en: 'Dashboard', hi: 'डैशबोर्ड' },
  financialData: { en: 'Financial Data', hi: 'वित्तीय डेटा' },
  reconciliation: { en: 'Reconciliation', hi: 'समाधान (रिकॉन्सिलिएशन)' },
  expenses: { en: 'Expense Intelligence', hi: 'व्यय बुद्धिमत्ता' },
  invoices: { en: 'Invoices & Receivables', hi: 'चालान और प्राप्य' },
  cashFlow: { en: 'Cash-Flow Forecasting', hi: 'रोकड़ प्रवाह पूर्वानुमान' },
  fraudMonitoring: { en: 'Fraud Monitoring', hi: 'धोखाधड़ी निगरानी' },
  investigations: { en: 'Investigations', hi: 'फॉरेंसिक जांच' },
  agentActivity: { en: 'AI Agent Activity', hi: 'एआई एजेंट गतिविधि' },
  aiCopilot: { en: 'AI Copilot', hi: 'एआई सह-पायलट' },
  reports: { en: 'Reports & Analytics', hi: 'रिपोर्ट और विश्लेषण' },
  auditHistory: { en: 'Audit History', hi: 'ऑडिट इतिहास' },
  userManagement: { en: 'User Management', hi: 'उपयोगकर्ता प्रबंधन' },

  // TopBar & Actions
  logout: { en: 'Logout', hi: 'लॉग आउट' },
  gatewayConnected: { en: 'Gateway :8080 Active', hi: 'गेटवे :8080 सक्रिय' },
  searchPlaceholder: { en: 'Search accounts, alerts, runs...', hi: 'खाते, अलर्ट, रन खोजें...' },
  switchLanguage: { en: 'Language', hi: 'भाषा' },
  switchTheme: { en: 'Theme', hi: 'थीम' },
  installApp: { en: 'Install App (PWA)', hi: 'ऐप इंस्टॉल करें (PWA)' },

  // Admin Customizer
  themeCustomizerTitle: { en: 'Dynamic Brand Color Customizer', hi: 'गतिशील ब्रांड रंग अनुकूलक' },
  themeCustomizerDesc: {
    en: 'Live theme styling engine for Platform Administrators. Updates CSS custom properties across the system.',
    hi: 'प्लेटफ़ॉर्म प्रशासकों के लिए लाइव थीम स्टाइलिंग इंजन। सिस्टम में सीएसएस कस्टम गुणों को अपडेट करता है।',
  },
};

interface I18nContextType {
  language: Language;
  setLanguage: (lang: Language) => void;
  t: (key: string) => string;
}

const I18nContext = createContext<I18nContextType | undefined>(undefined);

export const I18nProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [language, setLanguageState] = useState<Language>(() => {
    const saved = localStorage.getItem('finsight_language');
    return saved === 'hi' ? 'hi' : 'en';
  });

  const setLanguage = (lang: Language) => {
    setLanguageState(lang);
    localStorage.setItem('finsight_language', lang);
  };

  const t = (key: string): string => {
    const item = DICTIONARY[key];
    if (!item) return key;
    return item[language] || item.en;
  };

  return (
    <I18nContext.Provider value={{ language, setLanguage, t }}>
      {children}
    </I18nContext.Provider>
  );
};

export const useI18n = (): I18nContextType => {
  const context = useContext(I18nContext);
  if (!context) {
    throw new Error('useI18n must be used within an I18nProvider');
  }
  return context;
};

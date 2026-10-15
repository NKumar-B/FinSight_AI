import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Database,
  GitCompare,
  TrendingDown,
  Receipt,
  LineChart,
  ShieldAlert,
  SearchCheck,
  Bot,
  Sparkles,
  FileSpreadsheet,
  History,
  Users,
  ChevronLeft,
  ChevronRight,
  ShieldCheck,
  Layers,
} from 'lucide-react';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { useI18n } from '@/lib/i18n';
import { useTheme } from '@/lib/theme';
import { cn } from '@/utils/formatters';

interface NavItem {
  id: string;
  title: string;
  path: string;
  icon: React.ComponentType<{ className?: string }>;
  badge?: string;
  badgeVariant?: 'success' | 'warning' | 'danger' | 'info';
  requiredRole?: string;
}

interface NavSection {
  title: string;
  items: NavItem[];
}

export interface SidebarProps {
  isMobileOpen: boolean;
  onCloseMobile: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ isMobileOpen, onCloseMobile }) => {
  const [isCollapsed, setIsCollapsed] = useState(false);
  const { user, hasRole } = useAuth();
  const { t } = useI18n();
  const { appName } = useTheme();

  const navigationSections: NavSection[] = [
    {
      title: 'Overview',
      items: [
        {
          id: 'dashboard',
          title: t('dashboard'),
          path: '/dashboard',
          icon: LayoutDashboard,
        },
      ],
    },
    {
      title: 'Financial Operations',
      items: [
        {
          id: 'financial-data',
          title: t('financialData'),
          path: '/financial-data',
          icon: Database,
        },
        {
          id: 'reconciliation',
          title: t('reconciliation'),
          path: '/reconciliation',
          icon: GitCompare,
          badge: '4-Step',
          badgeVariant: 'success',
        },
        {
          id: 'expenses',
          title: t('expenses'),
          path: '/expenses',
          icon: TrendingDown,
        },
        {
          id: 'invoices',
          title: t('invoices'),
          path: '/invoices',
          icon: Receipt,
        },
        {
          id: 'cash-flow',
          title: t('cashFlow'),
          path: '/cash-flow',
          icon: LineChart,
        },
      ],
    },
    {
      title: 'Risk & Investigation',
      items: [
        {
          id: 'fraud',
          title: t('fraudMonitoring'),
          path: '/fraud',
          icon: ShieldAlert,
          badge: 'AML',
          badgeVariant: 'danger',
        },
        {
          id: 'investigations',
          title: t('investigations'),
          path: '/investigations',
          icon: SearchCheck,
        },
        {
          id: 'agents',
          title: t('agentActivity'),
          path: '/agents',
          icon: Bot,
          badge: '9 Agents',
          badgeVariant: 'info',
        },
      ],
    },
    {
      title: 'Intelligence & Governance',
      items: [
        {
          id: 'copilot',
          title: t('aiCopilot'),
          path: '/copilot',
          icon: Sparkles,
        },
        {
          id: 'reports',
          title: t('reports'),
          path: '/reports',
          icon: FileSpreadsheet,
        },
        {
          id: 'audit',
          title: t('auditHistory'),
          path: '/audit',
          icon: History,
        },
      ],
    },
    {
      title: 'Administration',
      items: [
        {
          id: 'users',
          title: t('userManagement'),
          path: '/users',
          icon: Users,
          requiredRole: 'ROLE_ADMIN',
        },
      ],
    },
  ];

  return (
    <>
      {/* Mobile Backdrop */}
      {isMobileOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/60 backdrop-blur-sm lg:hidden"
          onClick={onCloseMobile}
        />
      )}

      {/* Sidebar Container */}
      <aside
        className={cn(
          'fixed inset-y-0 left-0 z-50 flex flex-col bg-navy-950 text-white transition-all duration-300 border-r border-navy-800 lg:static',
          isCollapsed ? 'w-20' : 'w-64',
          isMobileOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        )}
      >
        {/* Brand Header */}
        <div className="flex items-center justify-between h-16 px-4 border-b border-navy-800/80 bg-navy-950">
          <div className="flex items-center gap-3 overflow-hidden">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-brand-600 to-indigo-500 flex items-center justify-center shadow-md shadow-brand-500/20 shrink-0">
              <Layers className="w-5 h-5 text-white" />
            </div>
            {!isCollapsed && (
              <div className="flex flex-col min-w-0">
                <span className="font-bold text-base tracking-tight text-white flex items-center gap-1.5 truncate">
                  {appName}
                </span>
                <span className="text-[10px] text-slate-400 uppercase tracking-widest font-semibold truncate">
                  Enterprise Fintech
                </span>
              </div>
            )}
          </div>

          {/* Desktop Collapse Toggle */}
          <button
            onClick={() => setIsCollapsed(!isCollapsed)}
            className="hidden lg:flex p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-navy-800 transition-colors"
            title={isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          >
            {isCollapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
          </button>
        </div>

        {/* Navigation Items */}
        <div className="flex-1 overflow-y-auto px-3 py-4 space-y-6">
          {navigationSections.map((section) => {
            // Filter section items by role if needed
            const visibleItems = section.items.filter(
              (item) => !item.requiredRole || hasRole(item.requiredRole)
            );

            if (visibleItems.length === 0) return null;

            return (
              <div key={section.title} className="space-y-1">
                {!isCollapsed && (
                  <h4 className="px-3 text-[10px] font-semibold tracking-wider text-slate-400 uppercase mb-2">
                    {section.title}
                  </h4>
                )}
                {visibleItems.map((item) => {
                  const Icon = item.icon;
                  return (
                    <NavLink
                      key={item.id}
                      to={item.path}
                      onClick={onCloseMobile}
                      className={({ isActive }) =>
                        cn(
                          'flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-medium transition-all duration-200 group relative',
                          isActive
                            ? 'bg-gradient-to-r from-brand-600 to-indigo-600 text-white shadow-lg shadow-brand-500/30 font-semibold'
                            : 'text-slate-300 hover:text-white hover:bg-navy-800/70 hover:translate-x-1'
                        )
                      }
                      title={isCollapsed ? item.title : undefined}
                    >
                      <Icon className="w-4 h-4 shrink-0 transition-transform group-hover:scale-110 duration-200" />
                      {!isCollapsed && (
                        <span className="flex-1 truncate">{item.title}</span>
                      )}
                      {!isCollapsed && item.badge && (
                        <span
                          className={cn(
                            'text-[9px] font-bold px-1.5 py-0.5 rounded-full uppercase tracking-wider',
                            item.badgeVariant === 'success' && 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30',
                            item.badgeVariant === 'danger' && 'bg-rose-500/20 text-rose-300 border border-rose-500/30',
                            item.badgeVariant === 'info' && 'bg-sky-500/20 text-sky-300 border border-sky-500/30',
                            !item.badgeVariant && 'bg-slate-700 text-slate-200'
                          )}
                        >
                          {item.badge}
                        </span>
                      )}
                    </NavLink>
                  );
                })}
              </div>
            );
          })}
        </div>

        {/* User Card at bottom */}
        <div className="p-3 border-t border-navy-800 bg-navy-900/60">
          <div className="flex items-center gap-3 px-2 py-2 rounded-xl bg-navy-950/80 border border-navy-800/80">
            <div className="w-8 h-8 rounded-lg bg-brand-500/20 border border-brand-500/30 flex items-center justify-center text-brand-400 font-bold text-xs shrink-0">
              {user?.fullName?.charAt(0) || user?.username?.charAt(0) || 'U'}
            </div>
            {!isCollapsed && (
              <div className="flex flex-col min-w-0 flex-1">
                <span className="text-xs font-semibold text-white truncate">
                  {user?.fullName || user?.username || 'FinSight User'}
                </span>
                <span className="text-[10px] text-brand-400 font-medium flex items-center gap-1">
                  <ShieldCheck className="w-3 h-3" />
                  {user?.roles?.[0]?.replace('ROLE_', '') || 'ANALYST'}
                </span>
              </div>
            )}
          </div>
        </div>
      </aside>
    </>
  );
};

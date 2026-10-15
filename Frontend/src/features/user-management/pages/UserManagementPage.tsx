import React, { useState } from 'react';
import { Users, UserPlus, Shield, CheckCircle, Lock, Edit3 } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { Select } from '@/components/ui/Select';
import { DataTable, Column } from '@/components/ui/DataTable';
import { User, RoleName } from '@/types/auth';

const INITIAL_USERS: User[] = [
  {
    id: 1,
    username: 'admin',
    email: 'admin@finsight.local',
    fullName: 'Platform Administrator',
    roles: ['ROLE_ADMIN'],
    enabled: true,
    createdAt: '2026-01-01',
  },
  {
    id: 2,
    username: 'analyst_john',
    email: 'analyst@finsight.local',
    fullName: 'John Doe',
    roles: ['ROLE_FINANCIAL_ANALYST'],
    enabled: true,
    createdAt: '2026-02-15',
  },
  {
    id: 3,
    username: 'compliance_officer',
    email: 'compliance@finsight.local',
    fullName: 'Elena Rostova',
    roles: ['ROLE_COMPLIANCE_OFFICER'],
    enabled: true,
    createdAt: '2026-03-01',
  },
  {
    id: 4,
    username: 'auditor_pwc',
    email: 'auditor@finsight.local',
    fullName: 'Marcus Vance',
    roles: ['ROLE_AUDITOR'],
    enabled: true,
    createdAt: '2026-04-10',
  },
];

export const UserManagementPage: React.FC = () => {
  const [users, setUsers] = useState<User[]>(INITIAL_USERS);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [newRole, setNewRole] = useState<RoleName>('ROLE_FINANCIAL_ANALYST');
  const [feedback, setFeedback] = useState<string | null>(null);

  const handleRoleUpdate = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedUser) return;

    setUsers((prev) =>
      prev.map((u) => (u.id === selectedUser.id ? { ...u, roles: [newRole] } : u))
    );

    setFeedback(`Updated ${selectedUser.username}'s assigned role to ${newRole}`);
    setSelectedUser(null);
    setTimeout(() => setFeedback(null), 3000);
  };

  const handleToggleStatus = (userId: number) => {
    setUsers((prev) =>
      prev.map((u) => (u.id === userId ? { ...u, enabled: !u.enabled } : u))
    );
  };

  const columns: Column<User>[] = [
    {
      key: 'username',
      header: 'Username',
      sortable: true,
      render: (u) => (
        <div>
          <span className="font-semibold text-xs text-slate-900 dark:text-white">{u.username}</span>
          <span className="block text-[11px] text-slate-400 dark:text-slate-500">{u.email}</span>
        </div>
      ),
    },
    {
      key: 'fullName',
      header: 'Full Name',
      sortable: true,
      render: (u) => <span className="text-xs text-slate-700 dark:text-slate-300">{u.fullName}</span>,
    },
    {
      key: 'roles',
      header: 'Assigned Role',
      render: (u) => (
        <Badge variant={u.roles.includes('ROLE_ADMIN') ? 'danger' : 'info'} size="sm">
          {u.roles[0]?.replace('ROLE_', '')}
        </Badge>
      ),
    },
    {
      key: 'enabled',
      header: 'Status',
      render: (u) => (
        <Badge variant={u.enabled ? 'success' : 'neutral'} size="sm" dot>
          {u.enabled ? 'ACTIVE' : 'SUSPENDED'}
        </Badge>
      ),
    },
    {
      key: 'action',
      header: 'Actions',
      className: 'text-right',
      render: (u) => (
        <div className="flex items-center justify-end gap-2">
          <Button
            size="sm"
            variant="outline"
            leftIcon={<Edit3 className="w-3 h-3" />}
            onClick={() => {
              setSelectedUser(u);
              setNewRole(u.roles[0]);
            }}
          >
            Edit Role
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className={
              u.enabled
                ? 'text-amber-600 dark:text-amber-400 hover:bg-amber-50 dark:hover:bg-amber-950/40'
                : 'text-emerald-600 dark:text-emerald-400 hover:bg-emerald-50 dark:hover:bg-emerald-950/40'
            }
            onClick={() => handleToggleStatus(u.id)}
          >
            {u.enabled ? 'Suspend' : 'Activate'}
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6 pb-12">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-2 border-b border-slate-200 dark:border-slate-800">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 dark:text-white flex items-center gap-2">
            <Shield className="w-5 h-5 text-brand-600 dark:text-brand-400" />
            User Administration & RBAC Access Control
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Restricted to Platform Administrators (<code className="font-mono text-brand-600 dark:text-brand-400">ROLE_ADMIN</code>). Manage accounts and credentials.
          </p>
        </div>
      </div>

      {feedback && (
        <div className="p-3 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 rounded-lg text-xs text-emerald-800 dark:text-emerald-300 font-semibold flex items-center gap-2">
          <CheckCircle className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
          {feedback}
        </div>
      )}

      {/* Users Table */}
      <Card title="Corporate User Directory" subtitle="Manage enterprise access credentials and role authorizations">
        <DataTable
          data={users}
          columns={columns}
          searchPlaceholder="Search by username, email, or full name..."
        />
      </Card>

      {/* Edit Role Modal */}
      {selectedUser && (
        <Modal
          isOpen={!!selectedUser}
          onClose={() => setSelectedUser(null)}
          title={`Edit Role Privileges: ${selectedUser.username}`}
          description="Adjust role-based menu access and microservice execution permissions."
        >
          <form onSubmit={handleRoleUpdate} className="space-y-4">
            <Select
              label="Assigned RBAC Role"
              value={newRole}
              onChange={(e) => setNewRole(e.target.value as RoleName)}
              options={[
                { label: 'Platform Administrator (ROLE_ADMIN)', value: 'ROLE_ADMIN' },
                { label: 'Financial Analyst (ROLE_FINANCIAL_ANALYST)', value: 'ROLE_FINANCIAL_ANALYST' },
                { label: 'Compliance Officer (ROLE_COMPLIANCE_OFFICER)', value: 'ROLE_COMPLIANCE_OFFICER' },
                { label: 'External Auditor (ROLE_AUDITOR)', value: 'ROLE_AUDITOR' },
                { label: 'Business User (ROLE_USER)', value: 'ROLE_USER' },
              ]}
            />

            <div className="flex justify-end gap-2 pt-2">
              <Button type="button" variant="outline" size="sm" onClick={() => setSelectedUser(null)}>
                Cancel
              </Button>
              <Button type="submit" size="sm">
                Save Role Assignment
              </Button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
};

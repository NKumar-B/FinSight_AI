export type RoleName = 
  | 'ROLE_ADMIN'
  | 'ROLE_FINANCIAL_ANALYST'
  | 'ROLE_COMPLIANCE_OFFICER'
  | 'ROLE_AUDITOR'
  | 'ROLE_USER';

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  roles: RoleName[];
  enabled: boolean;
  createdAt?: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface LoginResponseData {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface MenuItemDto {
  id: string;
  title: string;
  path: string;
  icon: string;
  badge?: string | null;
  children?: MenuItemDto[];
}

export interface NavigationMenuResponse {
  username: string;
  roles: string[];
  menuItems: MenuItemDto[];
}

export interface OtpResponseData {
  identity: string;
  deliveryChannel: 'EMAIL' | 'MOBILE';
  expiresInSeconds: number;
  generatedOtp?: string; // Included in dev mode
}

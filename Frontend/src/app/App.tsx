import React from 'react';
import { AppProviders } from './providers';
import { AppRoutes } from './router';

export const App: React.FC = () => {
  return (
    <AppProviders>
      <AppRoutes />
    </AppProviders>
  );
};

export default App;

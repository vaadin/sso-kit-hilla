import router from 'Frontend/routes.js';
import { useState } from 'react';
import { RouterProvider } from 'react-router-dom';
import { AuthContext, AuthState, initialState, useAuth } from './useAuth';

export default function App() {
  const [state, setState] = useState<AuthState>(initialState);

  return <AuthContext.Provider value={useAuth(state, setState)}>
    <RouterProvider router={router} />
  </AuthContext.Provider >;
}

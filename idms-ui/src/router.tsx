import { lazy } from 'react';
import { createBrowserRouter, Navigate } from 'react-router';

const Login = lazy(() => import('./routes/auth/Login'));

const router = createBrowserRouter([
  {
    path: 'auth',
    children: [
      {
        index: true,
        element: <Navigate to="login" replace />,
      },
      {
        path: 'login',
        element: <Login />,
      },
    ],
  },
]);

export default router;

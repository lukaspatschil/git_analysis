import React from 'react';
import ReactDOM from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import './index.css';
import Home from './routes/home';
import Stats from './routes/stats';

const router = createBrowserRouter([
  { path: '/', element: <Home /> },
  { path: '/stats', element: <Stats /> }
]);

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>
);

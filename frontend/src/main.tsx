import React from 'react';
import ReactDOM from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import './index.css';
import Stats from './routes/stats';
import Login from './routes/login';
import Dashboard from './routes/dashboard';
import Home from './routes/home';
import ProtectedRoute from './components/ProtectedRoute';
import Repository from "./routes/repository";
import Index from './routes/branch';
import Commits from './routes/branch/commits';
import Committer from "./routes/branch/committer";
import BranchOverview from "./routes/branch/branchOverview";
import CommitterStats from "./routes/branch/committerStats";
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
    ArcElement,
} from 'chart.js';

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
    ArcElement
);

const router = createBrowserRouter([
  { path: '/', element: <Home /> },
  { path: '/repository', element: <ProtectedRoute><Dashboard /></ProtectedRoute> },
  { path: '/repository/:repositoryId', element: <ProtectedRoute><Repository /></ProtectedRoute>},
  { path: '/repository/:repositoryId/:branchName', element: <ProtectedRoute><Index /></ProtectedRoute>, children: [
      { path: '', element: <BranchOverview />},
      { path: 'committerStats', element: <CommitterStats />},
      { path: 'commits', element: <Commits />},
      { path: 'committer', element: <Committer />},
    ]},
  { path: '/stats', element: <ProtectedRoute><Stats /></ProtectedRoute> },
  { path: '/login', element: <Login /> }
]);

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>
);

import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { WorkspacePage } from './pages/WorkspacePage';
import { CompanyManagementPage } from './pages/CompanyManagementPage';
import { ToastContainer } from './components/common/Toast';
import { useToastStore } from './store/toastStore';
import { Spinner } from './components/common';

const InterviewPage = lazy(() => import('./pages/InterviewPage').then(m => ({ default: m.InterviewPage })));
const InterviewReportPage = lazy(() => import('./pages/InterviewReportPage').then(m => ({ default: m.InterviewReportPage })));
const InterviewListPage = lazy(() => import('./pages/InterviewListPage').then(m => ({ default: m.InterviewListPage })));

function App() {
  const { toasts, removeToast } = useToastStore();

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<WorkspacePage />} />
        <Route path="/companies" element={<CompanyManagementPage />} />
        
        <Route
          path="/interviews"
          element={
            <Suspense fallback={<LoadingScreen />}>
              <InterviewListPage />
            </Suspense>
          }
        />
        <Route
          path="/interview/new"
          element={
            <Suspense fallback={<LoadingScreen />}>
              <InterviewPage />
            </Suspense>
          }
        />
        <Route
          path="/interview/:sessionId"
          element={
            <Suspense fallback={<LoadingScreen />}>
              <InterviewPage />
            </Suspense>
          }
        />
        <Route
          path="/interview/:sessionId/report"
          element={
            <Suspense fallback={<LoadingScreen />}>
              <InterviewReportPage />
            </Suspense>
          }
        />
      </Routes>
      <ToastContainer toasts={toasts} onClose={removeToast} />
    </BrowserRouter>
  );
}

function LoadingScreen() {
  return (
    <div className="min-h-screen bg-paper-50 flex items-center justify-center">
      <Spinner size="xl" />
    </div>
  );
}

export default App;

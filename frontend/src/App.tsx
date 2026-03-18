import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { WorkspacePage } from './pages/WorkspacePage';
import { CompanyManagementPage } from './pages/CompanyManagementPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { ToastContainer } from './components/common/Toast';
import { useToastStore } from './store/toastStore';
import { Spinner } from './components/common';
import { ProtectedRoute, PublicRoute, AuthLoadingWrapper } from './components/auth';

const InterviewPage = lazy(() => import('./pages/InterviewPage').then(m => ({ default: m.InterviewPage })));
const InterviewReportPage = lazy(() => import('./pages/InterviewReportPage').then(m => ({ default: m.InterviewReportPage })));
const InterviewListPage = lazy(() => import('./pages/InterviewListPage').then(m => ({ default: m.InterviewListPage })));
const ResumeListPage = lazy(() => import('./pages/ResumeListPage').then(m => ({ default: m.ResumeListPage })));
const ResumeCreatePage = lazy(() => import('./pages/ResumeCreatePage').then(m => ({ default: m.ResumeCreatePage })));
const ResumeEditPage = lazy(() => import('./pages/ResumeEditPage').then(m => ({ default: m.ResumeEditPage })));

function App() {
  const { toasts, removeToast } = useToastStore();

  return (
    <BrowserRouter>
      <AuthLoadingWrapper>
        <Routes>
          <Route
            path="/login"
            element={
              <PublicRoute>
                <LoginPage />
              </PublicRoute>
            }
          />
          <Route
            path="/register"
            element={
              <PublicRoute>
                <RegisterPage />
              </PublicRoute>
            }
          />

          <Route
            path="/"
            element={
              <ProtectedRoute>
                <WorkspacePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/companies"
            element={
              <ProtectedRoute>
                <CompanyManagementPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/interviews"
            element={
              <ProtectedRoute>
                <Suspense fallback={<LoadingScreen />}>
                  <InterviewListPage />
                </Suspense>
              </ProtectedRoute>
            }
          />
          <Route
            path="/interview/:sessionId"
            element={
              <ProtectedRoute>
                <Suspense fallback={<LoadingScreen />}>
                  <InterviewPage />
                </Suspense>
              </ProtectedRoute>
            }
          />
          <Route
            path="/interview/:sessionId/report"
            element={
              <ProtectedRoute>
                <Suspense fallback={<LoadingScreen />}>
                  <InterviewReportPage />
                </Suspense>
              </ProtectedRoute>
            }
          />

          <Route
            path="/resumes"
            element={
              <ProtectedRoute>
                <Suspense fallback={<LoadingScreen />}>
                  <ResumeListPage />
                </Suspense>
              </ProtectedRoute>
            }
          />
          <Route
            path="/resumes/new"
            element={
              <ProtectedRoute>
                <Suspense fallback={<LoadingScreen />}>
                  <ResumeCreatePage />
                </Suspense>
              </ProtectedRoute>
            }
          />
          <Route
            path="/resumes/:resumeId/edit"
            element={
              <ProtectedRoute>
                <Suspense fallback={<LoadingScreen />}>
                  <ResumeEditPage />
                </Suspense>
              </ProtectedRoute>
            }
          />
        </Routes>
        <ToastContainer toasts={toasts} onClose={removeToast} />
      </AuthLoadingWrapper>
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

import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { WorkspacePage } from './pages/WorkspacePage';
import { CompanyManagementPage } from './pages/CompanyManagementPage';
import { ToastContainer } from './components/common/Toast';
import { useToastStore } from './store/toastStore';

function App() {
  const { toasts, removeToast } = useToastStore();

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<WorkspacePage />} />
        <Route path="/companies" element={<CompanyManagementPage />} />
      </Routes>
      <ToastContainer toasts={toasts} onClose={removeToast} />
    </BrowserRouter>
  );
}

export default App;

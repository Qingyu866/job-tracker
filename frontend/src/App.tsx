import { BrowserRouter } from 'react-router-dom';
import { WorkspacePage } from './pages/WorkspacePage';
import { ToastContainer } from './components/common/Toast';
import { useToastStore } from './store/toastStore';

function App() {
  const { toasts, removeToast } = useToastStore();

  return (
    <BrowserRouter>
      <WorkspacePage />
      <ToastContainer toasts={toasts} onClose={removeToast} />
    </BrowserRouter>
  );
}

export default App;

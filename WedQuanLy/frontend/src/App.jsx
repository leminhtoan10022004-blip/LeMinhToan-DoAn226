import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Toaster } from 'sonner';
import NotFound from './pages/NotFound';
import Sidebar from './components/Sidebar';
import Dashboard from './pages/Dashboard';
import UserManagement from './pages/UserManagement';
import TestHistory from './pages/TestHistory';
import Header from './components/Header';
import Careers from './pages/Careers';
import TestManagement from './pages/TestManagement';
import QuestionManagement from './pages/QuestionManagement';


function App() {
  return (
    <BrowserRouter>
      <Toaster position="top-right" richColors />
      <div className="flex min-h-screen w-full bg-white">
        <Sidebar />
        <main className="flex-1 bg-gray-50">
        <Header />
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/users" element={<UserManagement />} />
            <Route path="/history" element={<TestHistory />} />
            <Route path="/careers" element={<Careers />} />
            <Route path="/tests" element={<TestManagement />} />
            <Route path="tests/1/questions" element={<QuestionManagement />} />
            <Route path="*" element={<NotFound />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
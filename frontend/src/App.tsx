import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import { AppRoutes } from './app/routes';
import { AuthProvider } from './features/auth';
import { ToastContainer } from './shared/ui/Toast';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
      staleTime: 5 * 60 * 1000, // 5 minutes
    },
  },
});

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
        {/* Toast global — monté une fois, visible sur toutes les pages (auth, inscription, dashboard) */}
        <ToastContainer position="top-right" />
      </BrowserRouter>
    </QueryClientProvider>
  );
}

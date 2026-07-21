import React from 'react';
import ReactDOM from 'react-dom/client';
import { App } from './App';
import './index.css';

async function enableMocking() {
  if (import.meta.env.VITE_ENABLE_MOCKS !== 'true') return;
  const { worker } = await import('./mocks/handlers');
  return worker.start({ onUnhandledRequest: 'bypass' });
}

enableMocking().then(() => {
  ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
      <App />
    </React.StrictMode>,
  );
});

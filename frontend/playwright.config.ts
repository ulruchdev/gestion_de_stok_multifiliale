import { defineConfig, devices } from '@playwright/test';

/**
 * E2E catalogue — backend Spring Boot réel sur :8080, frontend Vite sur :5173.
 * Les données de test sont créées via l'API dans `e2e/catalogue.spec.ts`
 * (tenant unique par exécution), jamais via MSW.
 */
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1,
  timeout: 45_000,
  expect: { timeout: 10_000 },
  reporter: [['list']],
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'retain-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
  },
});

import { defineConfig, devices } from "@playwright/test";

/**
 * End-to-end tests against a running stack: backend on :8080 (with APP_DEV_OUTBOX=true so tests can read
 * verification e-mails) and this frontend on :3000. See README "End-to-end tests".
 */
export default defineConfig({
  testDir: "./tests/e2e",
  timeout: 60_000,
  fullyParallel: false,
  retries: process.env.CI ? 1 : 0,
  use: {
    baseURL: process.env.E2E_BASE_URL ?? "http://localhost:3000",
    trace: "retain-on-failure",
  },
  projects: [
    { name: "desktop", use: { ...devices["Desktop Chrome"] }, testIgnore: /responsive\.spec\.ts/ },
    { name: "mobile", use: { ...devices["Pixel 7"] }, testMatch: /responsive\.spec\.ts/ },
  ],
});

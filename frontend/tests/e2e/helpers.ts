import { expect, type Browser, type Page } from "@playwright/test";

export const API = process.env.E2E_API_URL ?? "http://localhost:8080";
export const ADMIN = { email: process.env.E2E_ADMIN_EMAIL ?? "admin@craftora.local", password: process.env.E2E_ADMIN_PASSWORD ?? "admin-password-123" };

/** Renders a solid-colour square and returns it as a PNG, so tests need no binary fixtures. */
export async function makePng(browser: Browser, color: string): Promise<Buffer> {
  const page = await browser.newPage({ viewport: { width: 400, height: 400 } });
  await page.setContent(`<body style="margin:0"><div style="width:400px;height:400px;background:${color}"></div></body>`);
  const png = await page.screenshot({ type: "png" });
  await page.close();
  return png;
}

/** Reads the sandbox outbox (APP_DEV_OUTBOX=true) and returns the token from the latest matching link. */
export async function tokenFromEmail(page: Page, to: string, path: string): Promise<string> {
  let token: string | undefined;
  await expect.poll(async () => {
    const res = await page.request.get(`${API}/api/dev/outbox`);
    const mails = (await res.json()) as { to: string; body: string }[];
    token = mails.find((m) => m.to === to)?.body.match(new RegExp(`${path}\\?token=([\\w-]+)`))?.[1];
    return token;
  }).toBeTruthy();
  return token!;
}

export async function login(page: Page, email: string, password: string) {
  await page.goto("/login");
  await page.getByLabel("Email", { exact: true }).fill(email);
  await page.getByLabel("Password", { exact: true }).fill(password);
  await page.getByRole("button", { name: "Log in" }).click();
  await page.waitForURL((url) => !url.pathname.startsWith("/login"));
}

export const unique = () => `${Date.now().toString(36)}${Math.random().toString(36).slice(2, 6)}`;

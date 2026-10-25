import { expect, test } from "@playwright/test";

/** Runs in the "mobile" project (Pixel 7 viewport). */
test("public pages fit a phone screen and the menu works", async ({ page }) => {
  for (const path of ["/", "/marketplace", "/makers", "/pricing", "/register"]) {
    await page.goto(path);
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
    expect(overflow, `horizontal overflow on ${path}`).toBeLessThanOrEqual(1);
  }
  await page.goto("/");
  await page.getByRole("button", { name: "Menu" }).click();
  await page.getByRole("navigation", { name: "Mobile" }).getByRole("link", { name: "Marketplace" }).click();
  await expect(page).toHaveURL(/\/marketplace/);
});

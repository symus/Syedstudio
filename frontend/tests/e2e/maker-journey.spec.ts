import { expect, test } from "@playwright/test";
import { ADMIN, login, makePng, tokenFromEmail, unique } from "./helpers";

/**
 * Definition of Done journey: a maker registers, creates a profile, publishes a product with an image and a
 * website; an admin approves them; a visitor finds the product and sends a custom-order inquiry; the maker
 * manages it.
 */
test("maker onboarding → publication → discovery → inquiry", async ({ browser }) => {
  const id = unique();
  const makerEmail = `maker-${id}@example.com`;
  const slug = `studio-${id}`;
  const productName = `Speckled mug ${id}`;
  const png = await makePng(browser, "#b0603f");

  // --- maker registers and creates the business profile
  const maker = await (await browser.newContext({ locale: "en-GB" })).newPage();
  await maker.goto("/register");
  await maker.getByLabel("Sell my creations and get a website", { exact: true }).check();
  await maker.getByLabel("Your name", { exact: true }).fill("Ania");
  await maker.getByLabel("Email", { exact: true }).fill(makerEmail);
  await maker.getByLabel("Password", { exact: true }).fill("password123");
  await maker.getByRole("button", { name: "Create account" }).click();
  await maker.waitForURL("**/dashboard/profile");

  await maker.getByLabel("Business name", { exact: true }).fill(`Studio ${id}`);
  await maker.getByLabel("Website address", { exact: true }).fill(slug);
  await maker.getByLabel(/^City/).fill("Kraków");
  await maker.getByRole("button", { name: "Save", exact: true }).click();
  await expect(maker.getByText("Profile created!")).toBeVisible();

  // --- verify e-mail via the sandbox outbox
  const token = await tokenFromEmail(maker, makerEmail, "verify-email");
  await maker.goto(`/verify-email?token=${token}`);
  await expect(maker.getByText("Your email is verified")).toBeVisible();

  // --- create a product, add a photo, publish
  await maker.goto("/dashboard/products/new");
  await maker.getByLabel("Product name", { exact: true }).fill(productName);
  await maker.getByLabel(/^Description/).fill("Hand-thrown stoneware mug with a speckled glaze.");
  await maker.getByLabel("Category", { exact: true }).selectOption({ label: "Ceramics & pottery" });
  await maker.getByLabel("Price", { exact: true }).fill("89,00");
  await maker.getByRole("button", { name: "Save", exact: true }).click();
  await maker.waitForURL(/\/dashboard\/products\/[0-9a-f-]{36}/);
  await maker.locator('input[type="file"]').setInputFiles({ name: "mug.png", mimeType: "image/png", buffer: png });
  await expect(maker.getByRole("img", { name: productName })).toBeVisible();
  await maker.getByRole("button", { name: "Publish" }).click();
  await expect(maker.getByText("Published", { exact: true })).toBeVisible();

  // --- customise and publish the website
  await maker.goto("/dashboard/storefront");
  await maker.getByLabel("Headline", { exact: true }).fill(`Welcome to Studio ${id}`);
  await maker.getByRole("button", { name: "Publish website" }).click();
  await expect(maker.getByText("Published and up to date.").first()).toBeVisible();

  // --- admin approves the maker
  const admin = await (await browser.newContext({ locale: "en-GB" })).newPage();
  await login(admin, ADMIN.email, ADMIN.password);
  await admin.goto("/admin/makers");
  const row = admin.getByRole("row", { name: new RegExp(`Studio ${id}`) });
  await row.getByRole("button", { name: "Approve" }).click();
  await expect(row).toHaveCount(0);

  // --- a visitor discovers the product and the website
  const visitor = await (await browser.newContext({ locale: "en-GB" })).newPage();
  await visitor.goto(`/marketplace?q=${encodeURIComponent(productName)}`);
  await expect(visitor.getByText("1 results")).toBeVisible();
  await visitor.getByRole("link", { name: new RegExp(productName) }).click();
  await expect(visitor.getByRole("heading", { name: productName })).toBeVisible();
  await expect(visitor.getByText(/89[.,]00/).first()).toBeVisible();

  await visitor.goto(`/${slug}`);
  await expect(visitor.getByRole("heading", { name: `Welcome to Studio ${id}` })).toBeVisible();
  await expect(visitor.getByText(productName).first()).toBeVisible();

  await visitor.goto(`/makers?q=${id}`);
  await expect(visitor.getByRole("link", { name: new RegExp(`Studio ${id}`) })).toBeVisible();

  // --- the visitor sends a custom-order inquiry from the product page
  await visitor.goto(`/p/${slug}/speckled-mug-${id}`);
  await visitor.getByRole("link", { name: "Request a custom order" }).click();
  await expect(visitor.getByText(productName)).toBeVisible();
  await visitor.getByLabel("Your name", { exact: true }).fill("Kasia");
  await visitor.getByLabel("Your email", { exact: true }).fill(`kasia-${id}@example.com`);
  await visitor.getByLabel("Describe what you'd like", { exact: true }).fill("Could you make six of these with initials?");
  await visitor.getByLabel(/^Budget \(PLN\)/).fill("500");
  await visitor.locator('input[name="files"]').setInputFiles({ name: "idea.png", mimeType: "image/png", buffer: png });
  await visitor.getByRole("checkbox").check();
  await visitor.getByRole("button", { name: "Send inquiry" }).click();
  await expect(visitor.getByText("Inquiry sent!")).toBeVisible();

  // --- the maker manages the inquiry
  await maker.goto("/dashboard/inquiries");
  await maker.getByRole("link", { name: "Kasia" }).click();
  await expect(maker.getByText("Could you make six of these with initials?")).toBeVisible();
  await expect(maker.getByRole("img", { name: "idea.png" })).toBeVisible();
  await maker.getByLabel("Status", { exact: true }).selectOption("QUOTED");
  await maker.getByLabel("Message to customer (optional)", { exact: true }).fill("Six mugs for 480 PLN.");
  await maker.getByRole("button", { name: "Update" }).click();
  await expect(maker.getByText("Six mugs for 480 PLN.")).toBeVisible();
});

test("private areas require login and the right role", async ({ page }) => {
  await page.goto("/dashboard");
  await expect(page).toHaveURL(/\/login\?next=%2Fdashboard/);
  const res = await page.request.get("/api/admin/stats");
  expect(res.status()).toBe(401);
});

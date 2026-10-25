# Run Craftora on Windows — checklist

Tick each box as you go. Total time: ~20 minutes (most of it is the first Docker build).

## 1. Install the tools (one time)

- [ ] **Git for Windows** — https://git-scm.com/download/win (keep the default options)
- [ ] **Docker Desktop** — https://www.docker.com/products/docker-desktop/
  - During install, keep **“Use WSL 2”** ticked. Restart Windows if asked.
  - Open Docker Desktop and wait until the bottom-left says **“Engine running”**.
- [ ] Open **PowerShell** and confirm both work:
  ```powershell
  git --version
  docker --version
  docker compose version
  ```
  ✔️ Each prints a version number.

## 2. Get the code

- [ ] In PowerShell:
  ```powershell
  cd $HOME
  git clone https://github.com/symus/Syedstudio.git
  cd Syedstudio
  git checkout ccr-1837d2ae-epptin   # not needed once the pull request is merged into main
  ```
  ✔️ `dir` shows `backend`, `frontend`, `docker-compose.yml`, `README.md`.

## 3. Start everything

- [ ] Make sure ports **3000, 8080, 5432, 8025, 1025** are free. If you already run PostgreSQL locally, stop it first, because it uses port 5432.
- [ ] Start the stack:
  ```powershell
  docker compose up --build
  ```
  The first run downloads and builds everything (5–15 min). Leave this window open.
  ✔️ The logs show `Started CraftoraApplication` and the frontend shows `Ready`.

## 4. Check it's alive

Open these in your browser:

| ✔️ | URL | You should see |
|---|---|---|
| [ ] | http://localhost:3000 | Craftora homepage (“Your craft deserves its own corner of the internet.”) |
| [ ] | http://localhost:8080/actuator/health | `{"status":"UP"...}` |
| [ ] | http://localhost:8080/api/docs | Swagger API documentation |
| [ ] | http://localhost:8025 | Mailpit inbox (all emails the app sends land here) |

## 5. Walk through the MVP (Definition of Done)

**Maker**
- [ ] Click **Sign up** → choose *“Sell my creations and get a website”* → create the account.
- [ ] Fill in **Business profile** (name, website address, city) → **Save**. ✔️ “Profile created!”
- [ ] In **Mailpit** (http://localhost:8025) open the *Confirm your email* message → click the link. ✔️ “Your email is verified”.
- [ ] **Products → Add product**: name, category, price → **Save** → upload a photo → **Publish**. ✔️ Status “Published”.
- [ ] **Website**: pick a template, change colours/headline → check the preview in Desktop/Tablet/Mobile → **Publish website**.

**Admin** (open a private/incognito window)
- [ ] Log in with `admin@craftora.local` / `change-me-admin-password`.
- [ ] **Admin → Makers** → **Approve** your maker.

**Customer** (in the incognito window, after logging out)
- [ ] **Marketplace** → search for your product. ✔️ It appears.
- [ ] Open `http://localhost:3000/<your-website-address>`. ✔️ Your website is live.
- [ ] On the product page click **Request a custom order** → fill in the form, attach an image, tick consent → **Send**. ✔️ “Inquiry sent!”

**Maker again**
- [ ] **Inquiries** → open the new inquiry → change the status to *Quoted* and add a message. ✔️ The customer's email arrives in Mailpit.

**Mobile**
- [ ] Press **F12** → device toolbar (Ctrl+Shift+M) → pick a phone. ✔️ Pages fit the screen and the ☰ menu works.

🎉 All boxes ticked = the MVP works on your machine.

## Stop / restart / reset

```powershell
# stop (press Ctrl+C in the compose window), or:
docker compose down
# start again (fast after the first build):
docker compose up
# wipe all data (database + uploaded images) and start fresh:
docker compose down -v
```

## Troubleshooting

| Problem | Fix |
|---|---|
| `port is already allocated` | Another app is using that port. Stop it, or change the left-hand number in `docker-compose.yml` (e.g. `"3001:3000"`). |
| Docker says WSL is missing or outdated | Run `wsl --update` in an admin PowerShell, then restart Docker Desktop. |
| Build is very slow / runs out of memory | Docker Desktop → Settings → Resources: give it at least 4 GB RAM. |
| No verification email | Open http://localhost:8025. All mail is captured there and nothing is really sent. |
| Can't log in as admin | The admin is created on first start. Change `APP_ADMIN_PASSWORD` in `docker-compose.yml` (min 12 characters) and run `docker compose down -v` then `docker compose up` to recreate it. |

Running without Docker (Java 21, Node 22, PostgreSQL 16 installed directly) is covered in `README.md` → *Running locally → Option B*.

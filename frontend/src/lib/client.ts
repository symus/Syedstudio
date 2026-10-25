"use client";

import type { ApiProblem } from "./types";

/** Error carrying the backend's problem document (status, code and per-field errors). */
export class ApiError extends Error {
  constructor(public readonly problem: ApiProblem) {
    super(problem.detail ?? problem.title ?? `Request failed (${problem.status})`);
  }
  get status() { return this.problem.status; }
  get code() { return this.problem.code; }
  get fieldErrors() { return this.problem.errors ?? {}; }
}

function readCookie(name: string): string | null {
  const match = document.cookie.split("; ").find((c) => c.startsWith(`${name}=`));
  return match ? decodeURIComponent(match.slice(name.length + 1)) : null;
}

let csrfPromise: Promise<void> | null = null;

/** Ensures the XSRF-TOKEN cookie exists (Spring Security issues it on first access). */
async function ensureCsrf(): Promise<string> {
  let token = readCookie("XSRF-TOKEN");
  if (!token) {
    csrfPromise ??= fetch("/api/auth/csrf", { credentials: "same-origin" }).then(() => undefined);
    await csrfPromise;
    csrfPromise = null;
    token = readCookie("XSRF-TOKEN");
  }
  return token ?? "";
}

type Body = object | FormData | undefined;

/** Browser-side API call through the same-origin /api rewrite, with CSRF and JSON/problem handling. */
export async function api<T = void>(path: string, init: { method?: string; body?: Body; signal?: AbortSignal } = {}): Promise<T> {
  const method = init.method ?? (init.body ? "POST" : "GET");
  const headers: Record<string, string> = { Accept: "application/json" };
  if (method !== "GET") headers["X-XSRF-TOKEN"] = await ensureCsrf();
  let body: BodyInit | undefined;
  if (init.body instanceof FormData) body = init.body;
  else if (init.body !== undefined) {
    headers["Content-Type"] = "application/json";
    body = JSON.stringify(init.body);
  }
  const res = await fetch(path, { method, headers, body, credentials: "same-origin", signal: init.signal });
  if (!res.ok) {
    let problem: ApiProblem = { status: res.status };
    try {
      problem = { ...(await res.json()), status: res.status };
    } catch {
      /* non-JSON error body */
    }
    throw new ApiError(problem);
  }
  if (res.status === 204 || res.headers.get("content-length") === "0") return undefined as T;
  const text = await res.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

export function jsonBlob(value: unknown): Blob {
  return new Blob([JSON.stringify(value)], { type: "application/json" });
}

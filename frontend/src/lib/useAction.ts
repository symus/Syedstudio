"use client";

import { useState } from "react";
import { ApiError } from "./client";

/** Runs a mutation, then a refresh callback; exposes busy + error message for the UI. */
export function useAction(onDone: () => unknown) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  async function run(fn: () => Promise<unknown>) {
    setBusy(true);
    setError(null);
    try {
      await fn();
      await onDone();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Error");
    } finally {
      setBusy(false);
    }
  }
  return { busy, error, run };
}

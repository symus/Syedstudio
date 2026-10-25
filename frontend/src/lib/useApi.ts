"use client";

import { useCallback, useEffect, useState } from "react";
import { api, ApiError } from "./client";

/** Loads JSON from the API with loading/error state and a reload function. Pass null to skip. */
export function useApi<T>(path: string | null) {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<ApiError | Error | null>(null);
  const [loading, setLoading] = useState(Boolean(path));

  const load = useCallback(async (signal?: AbortSignal) => {
    if (!path) return;
    setLoading(true);
    setError(null);
    try {
      setData(await api<T>(path, { signal }));
    } catch (e) {
      if ((e as Error).name !== "AbortError") setError(e as Error);
    } finally {
      if (!signal?.aborted) setLoading(false);
    }
  }, [path]);

  useEffect(() => {
    const controller = new AbortController();
    // eslint-disable-next-line react-hooks/set-state-in-effect -- data fetching on mount/param change
    void load(controller.signal);
    return () => controller.abort();
  }, [load]);

  return { data, setData, error, loading, reload: () => load() };
}

export function isApiError(e: unknown, status?: number): e is ApiError {
  return e instanceof ApiError && (status === undefined || e.status === status);
}

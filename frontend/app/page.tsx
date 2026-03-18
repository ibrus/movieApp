"use client";

import styles from "./page.module.css";
import { useEffect, useState } from "react";

type HelloResponse = {
  message: string;
  timestamp: string;
};

export default function Home() {
  const [hello, setHello] = useState<HelloResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function run() {
      try {
        const res = await fetch("/api/hello", { cache: "no-store" });
        if (!res.ok) throw new Error(`Failed to fetch /api/hello (${res.status})`);
        const data = (await res.json()) as HelloResponse;
        if (!cancelled) setHello(data);
      } catch (e) {
        if (!cancelled) setError(e instanceof Error ? e.message : String(e));
      }
    }

    void run();
    return () => {
      cancelled = true;
    };
  }, []);
  return (
    <div className={styles.page}>
      <main className={styles.main}>
        <h1 className={styles.title}>Hello Movie App</h1>
        {error ? (
          <>
            <p className={styles.subtitle}>
              Backend says: <span className={styles.badge}>Error</span>
            </p>
            <p className={styles.muted}>{error}</p>
          </>
        ) : hello ? (
          <>
            <p className={styles.subtitle}>
              Backend says: <span className={styles.badge}>{hello.message}</span>
            </p>
            <p className={styles.muted}>Timestamp: {hello.timestamp}</p>
          </>
        ) : (
          <p className={styles.muted}>Loading backend response…</p>
        )}
      </main>
    </div>
  );
}

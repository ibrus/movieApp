"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { getStoredAuthToken } from "../lib/auth-token";
import styles from "./library.module.css";

type MovieLibraryItem = {
  movieId: number;
  tmdbId: number;
  title: string;
  posterPath: string | null;
  releaseDate: string | null;
};

type LibraryTab = "watchlist" | "liked" | "disliked";

const POSTER_BASE = "https://image.tmdb.org/t/p/w185";

function formatReleaseDate(iso: string | null): string {
  if (!iso || !iso.trim()) return "Release unknown";
  const d = iso.trim().slice(0, 10);
  if (d.length >= 4) return d;
  return "Release unknown";
}

export default function LibraryPage() {
  const [tab, setTab] = useState<LibraryTab>("watchlist");
  const [items, setItems] = useState<MovieLibraryItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {
    setToken(getStoredAuthToken());
  }, []);

  const load = useCallback(async (list: LibraryTab, authToken: string) => {
    setLoading(true);
    setError(null);
    try {
      const url = `/api/me/movies?list=${encodeURIComponent(list)}`;
      const res = await fetch(url, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${authToken}`,
        },
        cache: "no-store",
      });

      const text = await res.text();
      let parsed: unknown = null;
      try {
        parsed = text ? JSON.parse(text) : null;
      } catch {
        // ignore
      }

      if (!res.ok) {
        const message =
          typeof parsed === "object" && parsed && "message" in parsed
            ? String((parsed as { message?: unknown }).message ?? `Request failed (${res.status})`)
            : `Request failed (${res.status})`;
        setError(message);
        setItems([]);
        return;
      }

      const data = (parsed ?? []) as MovieLibraryItem[];
      setItems(Array.isArray(data) ? data : []);
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
      setItems([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!token) {
      setItems([]);
      setError(null);
      setLoading(false);
      return;
    }
    void load(tab, token);
  }, [tab, token, load]);

  return (
    <div className={styles.page}>
      <main className={styles.main}>
        <h1 className={styles.title}>Library</h1>

        <div className={styles.tabs} role="tablist" aria-label="Library lists">
          {(
            [
              { id: "watchlist" as const, label: "Watchlist" },
              { id: "liked" as const, label: "Liked" },
              { id: "disliked" as const, label: "Disliked" },
            ] as const
          ).map(({ id, label }) => (
            <button
              key={id}
              type="button"
              role="tab"
              aria-selected={tab === id}
              className={`${styles.tab} ${tab === id ? styles.tabActive : ""}`}
              onClick={() => setTab(id)}
            >
              {label}
            </button>
          ))}
        </div>

        {!token && (
          <p className={styles.muted}>
            Log in on{" "}
            <Link href="/" className={styles.inlineLink}>
              Search
            </Link>{" "}
            to see your saved movies.
          </p>
        )}

        {token && loading && <p className={styles.muted}>Loading…</p>}
        {error && <p className={styles.error}>{error}</p>}

        {token && !loading && !error && items.length === 0 && (
          <p className={styles.muted}>No movies in this list yet.</p>
        )}

        {token && items.length > 0 && (
          <div className={styles.resultsGrid}>
            {items.map((m) => {
              const posterUrl = m.posterPath ? `${POSTER_BASE}${m.posterPath}` : null;
              return (
                <article key={`${m.movieId}-${m.tmdbId}`} className={styles.card}>
                  {posterUrl ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img className={styles.poster} src={posterUrl} alt={m.title || "Movie poster"} />
                  ) : (
                    <div className={styles.posterPlaceholder} aria-hidden="true">
                      No poster
                    </div>
                  )}
                  <div className={styles.meta}>
                    <h2 className={styles.cardTitle}>{m.title || "Untitled"}</h2>
                    <p className={styles.cardSub}>{formatReleaseDate(m.releaseDate)}</p>
                  </div>
                </article>
              );
            })}
          </div>
        )}
      </main>
    </div>
  );
}

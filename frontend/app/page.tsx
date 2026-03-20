"use client";

import styles from "./page.module.css";
import { useEffect, useState } from "react";

type HelloResponse = {
  message: string;
  timestamp: string;
};

type UserDto = {
  id: number;
  email: string;
  displayName?: string | null;
  createdAt?: string | null;
};

type RegisterResponse = UserDto;

type LoginResponse = {
  token: string;
  user: UserDto;
};

type TmdbMovieSearchResult = {
  id?: number;
  title?: string;
  release_date?: string;
  vote_average?: number;
  overview?: string;
  poster_path?: string | null;
};

type TmdbMovieSearchResponse = {
  page?: number;
  totalResults?: number;
  results: TmdbMovieSearchResult[];
};

export default function Home() {
  const [hello, setHello] = useState<HelloResponse | null>(null);
  const [helloError, setHelloError] = useState<string | null>(null);

  const [registerEmail, setRegisterEmail] = useState("");
  const [registerPassword, setRegisterPassword] = useState("");
  const [registerDisplayName, setRegisterDisplayName] = useState("");
  const [registerLoading, setRegisterLoading] = useState(false);
  const [registerMessage, setRegisterMessage] = useState<string | null>(null);
  const [registerError, setRegisterError] = useState<string | null>(null);

  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [loginLoading, setLoginLoading] = useState(false);
  const [loginMessage, setLoginMessage] = useState<string | null>(null);
  const [loginError, setLoginError] = useState<string | null>(null);
  const [authToken, setAuthToken] = useState<string | null>(null);
  const [profileLoading, setProfileLoading] = useState(false);
  const [profileError, setProfileError] = useState<string | null>(null);

  const [currentUser, setCurrentUser] = useState<UserDto | null>(null);

  const [movieQuery, setMovieQuery] = useState("");
  const [movieSearchLoading, setMovieSearchLoading] = useState(false);
  const [movieSearchError, setMovieSearchError] = useState<string | null>(null);
  const [movieSearchPerformed, setMovieSearchPerformed] = useState(false);
  const [movieSearchResults, setMovieSearchResults] = useState<TmdbMovieSearchResult[]>([]);

  useEffect(() => {
    let cancelled = false;

    async function run() {
      try {
        const res = await fetch("/api/hello", { cache: "no-store" });
        if (!res.ok) throw new Error(`Failed to fetch /api/hello (${res.status})`);
        const data = (await res.json()) as HelloResponse;
        if (!cancelled) setHello(data);
      } catch (e) {
        if (!cancelled) setHelloError(e instanceof Error ? e.message : String(e));
      }
    }

    void run();
    return () => {
      cancelled = true;
    };
  }, []);

  async function handleRegister(e: React.FormEvent) {
    e.preventDefault();
    setRegisterError(null);
    setRegisterMessage(null);
    setRegisterLoading(true);
    try {
      const res = await fetch("/api/auth/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          email: registerEmail,
          password: registerPassword,
          displayName: registerDisplayName || undefined,
        }),
      });

      const text = await res.text();
      let parsed: unknown = null;
      try {
        parsed = text ? JSON.parse(text) : null;
      } catch {
        // ignore json parse errors; we still show status text below
      }

      if (!res.ok) {
        const message =
          typeof parsed === "object" && parsed && "message" in parsed
            ? String((parsed as { message?: unknown }).message ?? `Registration failed (${res.status})`)
            : `Registration failed (${res.status})`;
        setRegisterError(message);
      } else {
        const data = parsed as RegisterResponse | null;
        setRegisterMessage("Registration successful.");
        if (data) {
          setCurrentUser((prev) => prev ?? data);
        }
        setRegisterPassword("");
      }
    } catch (e) {
      setRegisterError(e instanceof Error ? e.message : String(e));
    } finally {
      setRegisterLoading(false);
    }
  }

  async function handleLogin(e: React.FormEvent) {
    e.preventDefault();
    setLoginError(null);
    setLoginMessage(null);
    setLoginLoading(true);
    try {
      const res = await fetch("/api/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          email: loginEmail,
          password: loginPassword,
        }),
      });

      const text = await res.text();
      let parsed: unknown = null;
      try {
        parsed = text ? JSON.parse(text) : null;
      } catch {
        // ignore json parse errors; we still show status text below
      }

      if (!res.ok) {
        const message =
          typeof parsed === "object" && parsed && "message" in parsed
            ? String((parsed as { message?: unknown }).message ?? `Login failed (${res.status})`)
            : `Login failed (${res.status})`;
        setLoginError(message);
      } else {
        const data = parsed as LoginResponse | null;
        if (data?.user) {
          setCurrentUser(data.user);
        }
        if (data?.token) {
          setAuthToken(data.token);
        }
        setProfileError(null);
        setLoginMessage("Login successful.");
        setLoginPassword("");
      }
    } catch (e) {
      setLoginError(e instanceof Error ? e.message : String(e));
    } finally {
      setLoginLoading(false);
    }
  }

  async function handleLoadProfile() {
    if (!authToken) {
      setProfileError("Log in first to get a JWT.");
      return;
    }

    setProfileError(null);
    setProfileLoading(true);
    try {
      const res = await fetch("/api/me", {
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
        // ignore json parse errors; we still show status text below
      }

      if (!res.ok) {
        const message =
          typeof parsed === "object" && parsed && "message" in parsed
            ? String((parsed as { message?: unknown }).message ?? `Failed to load profile (${res.status})`)
            : `Failed to load profile (${res.status})`;
        setProfileError(message);
        return;
      }

      setCurrentUser(parsed as UserDto);
    } catch (e) {
      setProfileError(e instanceof Error ? e.message : String(e));
    } finally {
      setProfileLoading(false);
    }
  }

  async function handleMovieSearch(e: React.FormEvent) {
    e.preventDefault();

    if (!authToken) {
      setMovieSearchError("Log in first to get a JWT.");
      setMovieSearchPerformed(true);
      return;
    }

    const query = movieQuery.trim();
    if (!query) {
      setMovieSearchError("Please enter a movie title to search.");
      setMovieSearchPerformed(true);
      return;
    }

    setMovieSearchError(null);
    setMovieSearchLoading(true);
    setMovieSearchPerformed(true);
    setMovieSearchResults([]);

    try {
      const url = `/api/movies/search?query=${encodeURIComponent(query)}&page=1`;
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
        // ignore json parse errors; we still show status text below
      }

      if (!res.ok) {
        const message =
          typeof parsed === "object" && parsed && "message" in parsed
            ? String((parsed as { message?: unknown }).message ?? `Search failed (${res.status})`)
            : `Search failed (${res.status})`;
        setMovieSearchError(message);
        return;
      }

      const data = (parsed as TmdbMovieSearchResponse | null) ?? null;
      setMovieSearchResults(data?.results ?? []);
    } catch (e) {
      setMovieSearchError(e instanceof Error ? e.message : String(e));
    } finally {
      setMovieSearchLoading(false);
    }
  }

  return (
    <div className={styles.page}>
      <main className={styles.main}>
        <h1 className={styles.title}>Hello Movie App</h1>

        <section className={styles.section}>
          <h2 className={styles.subtitle}>Backend health check</h2>
          {helloError ? (
            <>
              <p className={styles.subtitle}>
                Backend says: <span className={styles.badge}>Error</span>
              </p>
              <p className={styles.muted}>{helloError}</p>
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
        </section>

        <section className={styles.section}>
          <h2 className={styles.subtitle}>Register</h2>
          <form onSubmit={handleRegister} className={styles.form}>
            <label className={styles.label}>
              <span>Email</span>
              <input
                type="email"
                required
                value={registerEmail}
                onChange={(e) => setRegisterEmail(e.target.value)}
                className={styles.input}
              />
            </label>
            <label className={styles.label}>
              <span>Password</span>
              <input
                type="password"
                required
                value={registerPassword}
                onChange={(e) => setRegisterPassword(e.target.value)}
                className={styles.input}
              />
            </label>
            <label className={styles.label}>
              <span>Display name (optional)</span>
              <input
                type="text"
                value={registerDisplayName}
                onChange={(e) => setRegisterDisplayName(e.target.value)}
                className={styles.input}
              />
            </label>
            <button type="submit" className={styles.button} disabled={registerLoading}>
              {registerLoading ? "Registering…" : "Register"}
            </button>
          </form>
          {registerMessage && <p className={styles.success}>{registerMessage}</p>}
          {registerError && <p className={styles.error}>{registerError}</p>}
        </section>

        <section className={styles.section}>
          <h2 className={styles.subtitle}>Login</h2>
          <form onSubmit={handleLogin} className={styles.form}>
            <label className={styles.label}>
              <span>Email</span>
              <input
                type="email"
                required
                value={loginEmail}
                onChange={(e) => setLoginEmail(e.target.value)}
                className={styles.input}
              />
            </label>
            <label className={styles.label}>
              <span>Password</span>
              <input
                type="password"
                required
                value={loginPassword}
                onChange={(e) => setLoginPassword(e.target.value)}
                className={styles.input}
              />
            </label>
            <button type="submit" className={styles.button} disabled={loginLoading}>
              {loginLoading ? "Logging in…" : "Login"}
            </button>
          </form>
          {loginMessage && <p className={styles.success}>{loginMessage}</p>}
          {loginError && <p className={styles.error}>{loginError}</p>}
          <p className={styles.muted}>
            Token: {authToken ? `${authToken.slice(0, 24)}...` : "Not set"}
          </p>
          <button type="button" className={styles.button} onClick={handleLoadProfile} disabled={profileLoading || !authToken}>
            {profileLoading ? "Loading profile…" : "Load profile"}
          </button>
          {profileError && <p className={styles.error}>{profileError}</p>}
        </section>

        <section className={styles.section}>
          <h2 className={styles.subtitle}>Current user</h2>
          {currentUser ? (
            <div className={styles.card}>
              <p>
                Logged in as <strong>{currentUser.displayName || currentUser.email}</strong>
              </p>
              <p className={styles.muted}>{currentUser.email}</p>
              {currentUser.createdAt && (
                <p className={styles.muted}>Created at: {currentUser.createdAt}</p>
              )}
            </div>
          ) : (
            <p className={styles.muted}>No user is currently logged in.</p>
          )}
        </section>

        <section className={styles.section}>
          <h2 className={styles.subtitle}>TMDB Movie Search</h2>
          <form onSubmit={handleMovieSearch} className={styles.searchForm}>
            <label className={styles.label}>
              <span>Movie title</span>
              <input
                type="text"
                required
                value={movieQuery}
                onChange={(e) => setMovieQuery(e.target.value)}
                className={styles.input}
                placeholder="e.g. fight club"
              />
            </label>

            <button
              type="submit"
              className={styles.button}
              disabled={movieSearchLoading || !authToken}
              title={!authToken ? "Log in first to enable searching" : undefined}
            >
              {movieSearchLoading ? "Searching…" : "Search"}
            </button>
          </form>

          {!authToken && (
            <p className={styles.muted}>
              Log in to search TMDB.
            </p>
          )}

          {movieSearchError && <p className={styles.error}>{movieSearchError}</p>}
          {movieSearchLoading && <p className={styles.muted}>Calling TMDB…</p>}

          {movieSearchPerformed && !movieSearchLoading && movieSearchResults.length === 0 && !movieSearchError && (
            <p className={styles.muted}>No results found.</p>
          )}

          {movieSearchResults.length > 0 && (
            <>
              <p className={styles.muted}>
                Showing {movieSearchResults.length} result{movieSearchResults.length === 1 ? "" : "s"}.
              </p>
              <div className={styles.resultsGrid}>
                {movieSearchResults.map((m) => {
                  const year = m.release_date ? m.release_date.slice(0, 4) : null;
                  const vote = typeof m.vote_average === "number" ? m.vote_average.toFixed(1) : null;
                  const overview = (m.overview ?? "").trim();
                  const overviewSnippet = overview.length > 180 ? `${overview.slice(0, 180)}…` : overview;
                  const posterUrl = m.poster_path
                    ? `https://image.tmdb.org/t/p/w200${m.poster_path}`
                    : null;

                  return (
                    <article key={m.id ?? `${m.title ?? "movie"}-${year ?? "unknown"}-${overviewSnippet}`} className={styles.movieCard}>
                      {posterUrl ? (
                        // eslint-disable-next-line @next/next/no-img-element
                        <img className={styles.moviePoster} src={posterUrl} alt={m.title ?? "Movie poster"} />
                      ) : (
                        <div className={styles.moviePosterPlaceholder} aria-hidden="true">
                          No poster
                        </div>
                      )}
                      <div className={styles.movieMeta}>
                        <h3 className={styles.movieTitle}>{m.title ?? "Untitled"}</h3>
                        <p className={styles.movieSub}>
                          {year ? `Release: ${year}` : "Release: unknown"}{vote ? ` • Rating: ${vote}` : ""}
                        </p>
                        {overviewSnippet && <p className={styles.movieOverview}>{overviewSnippet}</p>}
                      </div>
                    </article>
                  );
                })}
              </div>
            </>
          )}
        </section>
      </main>
    </div>
  );
}

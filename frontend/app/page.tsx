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
  message: string;
  user: UserDto;
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

  const [currentUser, setCurrentUser] = useState<UserDto | null>(null);

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
        setLoginMessage(data?.message ?? "Login successful.");
        setLoginPassword("");
      }
    } catch (e) {
      setLoginError(e instanceof Error ? e.message : String(e));
    } finally {
      setLoginLoading(false);
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
        </section>

        <section className={styles.section}>
          <h2 className={styles.subtitle}>Current user (frontend state only)</h2>
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
      </main>
    </div>
  );
}

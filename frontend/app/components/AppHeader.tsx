"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import styles from "./AppHeader.module.css";

export function AppHeader() {
  const pathname = usePathname();
  const searchActive = pathname === "/";
  const libraryActive = pathname === "/library";

  return (
    <header className={styles.header}>
      <span className={styles.brand}>Movie App</span>
      <nav className={styles.nav} aria-label="Main">
        <Link
          href="/"
          className={`${styles.navLink} ${searchActive ? styles.navLinkActive : ""}`}
          aria-current={searchActive ? "page" : undefined}
        >
          Search
        </Link>
        <Link
          href="/library"
          className={`${styles.navLink} ${libraryActive ? styles.navLinkActive : ""}`}
          aria-current={libraryActive ? "page" : undefined}
        >
          Library
        </Link>
        <span className={styles.navPlaceholder} title="Coming soon" aria-disabled="true">
          Notifications
        </span>
        <span className={styles.navPlaceholder} title="Coming soon" aria-disabled="true">
          Profile
        </span>
      </nav>
    </header>
  );
}

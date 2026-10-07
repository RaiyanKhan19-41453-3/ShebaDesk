import type { ReactNode } from "react";
import { useTranslation } from "react-i18next";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import LanguageToggle from "../components/LanguageToggle";

export default function Layout({ children }: { children: ReactNode }) {
  const { t } = useTranslation();
  const { pathname } = useLocation();
  const { username, logout } = useAuth();
  const navigate = useNavigate();

  const NAV = [
    { to: "/", label: t("nav.dashboard") },
    { to: "/patients", label: t("nav.patients") },
    { to: "/doctors", label: t("nav.doctors") },
    { to: "/appointments", label: t("nav.appointments") },
  ];

  return (
    <div className="min-h-screen">
      <header className="bg-emerald-700 text-white shadow">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
          <Link to="/" className="text-xl font-bold tracking-tight">
            🏥 {t("app.name")}
          </Link>
          <div className="flex items-center gap-3 text-sm">
            <span className="hidden opacity-80 sm:inline">{username}</span>
            <LanguageToggle />
            <button
              onClick={() => {
                logout();
                navigate("/login");
              }}
              className="rounded bg-emerald-800 px-3 py-1.5 font-medium hover:bg-emerald-900"
            >
              {t("nav.logout")}
            </button>
          </div>
        </div>
        <nav className="bg-emerald-800">
          <div className="mx-auto flex max-w-6xl gap-1 overflow-x-auto px-4">
            {NAV.map((item) => (
              <Link
                key={item.to}
                to={item.to}
                className={`whitespace-nowrap px-4 py-2.5 text-sm font-medium ${
                  pathname === item.to ? "bg-emerald-900 text-white" : "text-emerald-100 hover:bg-emerald-700"
                }`}
              >
                {item.label}
              </Link>
            ))}
          </div>
        </nav>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-6">{children}</main>
    </div>
  );
}

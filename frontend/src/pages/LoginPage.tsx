import { useState } from "react";
import type { FormEvent } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { problemMessage } from "../api/client";
import { useAuth } from "../auth/AuthContext";

export default function LoginPage() {
  const { t } = useTranslation();
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await login(username, password);
      navigate("/", { replace: true });
    } catch (err) {
      setError(problemMessage(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center px-4">
      <form onSubmit={onSubmit} className="w-full max-w-sm rounded-xl bg-white p-8 shadow-lg">
        <h1 className="mb-1 text-2xl font-bold text-emerald-800">🏥 {t("login.title")}</h1>
        <p className="mb-6 text-sm text-slate-500">{t("login.subtitle")}</p>
        {error && (
          <p role="alert" className="mb-4 rounded bg-red-50 px-3 py-2 text-sm text-red-700">
            {error}
          </p>
        )}
        <label className="mb-3 block text-sm font-medium">
          {t("login.username")}
          <input
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            className="mt-1 w-full rounded border border-slate-300 px-3 py-2 outline-none focus:border-emerald-600 focus:ring-1 focus:ring-emerald-600"
          />
        </label>
        <label className="mb-5 block text-sm font-medium">
          {t("login.password")}
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            className="mt-1 w-full rounded border border-slate-300 px-3 py-2 outline-none focus:border-emerald-600 focus:ring-1 focus:ring-emerald-600"
          />
        </label>
        <button
          type="submit"
          disabled={busy}
          className="w-full rounded bg-emerald-700 py-2.5 font-semibold text-white hover:bg-emerald-800 disabled:opacity-60"
        >
          {busy ? t("login.busy") : t("login.submit")}
        </button>
        <p className="mt-4 text-center text-xs text-slate-400">{t("login.demo")}</p>
      </form>
    </div>
  );
}

import { useQuery } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import api from "../api/client";
import type { Appointment, Page } from "../api/types";

function todayISO() {
  return new Date().toISOString().slice(0, 10);
}

export default function DashboardPage() {
  const { t } = useTranslation();
  const today = todayISO();
  const patients = useQuery({ queryKey: ["patients-count"], queryFn: () => api.get<Page<unknown>>("/patients?size=1").then((r) => r.data.totalElements) });
  const doctors = useQuery({ queryKey: ["doctors-count"], queryFn: () => api.get<Page<unknown>>("/doctors?size=1").then((r) => r.data.totalElements) });
  const appointments = useQuery({ queryKey: ["appointments-count"], queryFn: () => api.get<Page<unknown>>("/appointments?size=1").then((r) => r.data.totalElements) });
  const todays = useQuery({
    queryKey: ["appointments-today", today],
    queryFn: () =>
      api
        .get<Page<Appointment>>("/appointments", { params: { from: today, to: today, size: 20, sort: "appointmentDate" } })
        .then((r) => r.data.content),
  });

  const cards = [
    { label: t("dash.patients"), value: patients.data, to: "/patients", icon: "🧑‍⚕️" },
    { label: t("dash.doctors"), value: doctors.data, to: "/doctors", icon: "👨‍⚕️" },
    { label: t("dash.appointments"), value: appointments.data, to: "/appointments", icon: "📅" },
  ];

  return (
    <div>
      <h1 className="mb-4 text-2xl font-bold">{t("dash.title")}</h1>
      <div className="grid gap-4 sm:grid-cols-3">
        {cards.map((c) => (
          <Link key={c.label} to={c.to} className="rounded-xl bg-white p-6 shadow transition hover:shadow-md">
            <div className="text-3xl">{c.icon}</div>
            <div className="mt-2 text-3xl font-bold">{c.value ?? "…"}</div>
            <div className="text-sm text-slate-500">{c.label}</div>
          </Link>
        ))}
      </div>

      <div className="mt-6 rounded-xl bg-white p-6 shadow">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="font-semibold">{t("dash.today")} ({today})</h2>
          <Link to="/appointments" className="text-sm text-emerald-700 hover:underline">
            {t("dash.viewAll")}
          </Link>
        </div>
        {todays.isPending && <p className="text-sm text-slate-400">{t("dash.loading")}</p>}
        {todays.data?.length === 0 && <p className="text-sm text-slate-400">{t("dash.emptyToday")}</p>}
        {todays.data && todays.data.length > 0 && (
          <ul className="divide-y divide-slate-100 text-sm">
            {todays.data.map((a) => (
              <li key={a.id} className="flex items-center justify-between py-2">
                <span>
                  <strong>{a.patientName}</strong> with {a.doctorName}
                </span>
                <span className="text-slate-500">{a.appointmentDate}</span>
              </li>
            ))}
          </ul>
        )}
      </div>

      <div className="mt-6 rounded-xl bg-white p-6 shadow">
        <h2 className="mb-2 font-semibold">{t("dash.health")}</h2>
        <div className="flex flex-wrap gap-3 text-sm">
          <a className="text-emerald-700 underline" href="http://localhost:4000/swagger-ui.html" target="_blank" rel="noreferrer">Swagger UI</a>
          <a className="text-emerald-700 underline" href="http://localhost:3000" target="_blank" rel="noreferrer">Grafana</a>
          <a className="text-emerald-700 underline" href="http://localhost:9090" target="_blank" rel="noreferrer">Prometheus</a>
        </div>
      </div>
    </div>
  );
}

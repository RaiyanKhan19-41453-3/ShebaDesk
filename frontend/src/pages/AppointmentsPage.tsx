import { useState } from "react";
import { useTranslation } from "react-i18next";
import type { FormEvent } from "react";
import { problemMessage } from "../api/client";
import {
  useAppointments,
  useCreateAppointment,
  useDeleteAppointment,
  useDoctorOptions,
  usePatientOptions,
} from "../api/hooks";
import type { AppointmentForm } from "../api/types";
import FormError, { fieldErrors, inputClass } from "../components/FormError";
import Modal from "../components/Modal";
import Pagination from "../components/Pagination";

const EMPTY: AppointmentForm = { doctorId: "", patientId: "", appointmentDate: "", notes: "" };

export default function AppointmentsPage() {
  const { t } = useTranslation();
  const [page, setPage] = useState(0);
  const [doctorId, setDoctorId] = useState("");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [booking, setBooking] = useState(false);
  const [notice, setNotice] = useState<string | null>(null);

  const list = useAppointments({ page, doctorId, patientId: "", from, to });
  const remove = useDeleteAppointment();

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-2xl font-bold">{t("appts.title")}</h1>
        <button
          onClick={() => setBooking(true)}
          className="rounded bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-800"
        >
          {t("appts.new")}
        </button>
      </div>

      <div className="mb-4 grid grid-cols-2 gap-2 sm:flex">
        <input
          placeholder={t("appts.filterDoctorId")}
          value={doctorId}
          onChange={(e) => {
            setDoctorId(e.target.value);
            setPage(0);
          }}
          className="rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-emerald-600"
        />
        <input
          type="date"
          value={from}
          onChange={(e) => {
            setFrom(e.target.value);
            setPage(0);
          }}
          className="rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-emerald-600"
        />
        <input
          type="date"
          value={to}
          onChange={(e) => {
            setTo(e.target.value);
            setPage(0);
          }}
          className="rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-emerald-600"
        />
      </div>

      {notice && <p className="mb-4 rounded bg-amber-50 px-3 py-2 text-sm text-amber-800">{notice}</p>}
      {list.isError && <p className="mb-4 rounded bg-red-50 px-3 py-2 text-sm text-red-700">{problemMessage(list.error)}</p>}

      <div className="overflow-x-auto rounded-xl bg-white shadow">
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-500">
            <tr>
              <th className="px-4 py-3 font-medium">{t("appts.date")}</th>
              <th className="px-4 py-3 font-medium">{t("appts.doctor")}</th>
              <th className="px-4 py-3 font-medium">{t("appts.patient")}</th>
              <th className="px-4 py-3 font-medium">{t("appts.notes")}</th>
              <th className="px-4 py-3 font-medium">{t("common.actions")}</th>
            </tr>
          </thead>
          <tbody>
            {list.isPending && (
              <tr>
                <td colSpan={5} className="px-4 py-6 text-center text-slate-400">
                  {t("common.loading")}
                </td>
              </tr>
            )}
            {list.data?.content.length === 0 && (
              <tr>
                <td colSpan={5} className="px-4 py-6 text-center text-slate-400">
                  {t("appts.empty")}
                </td>
              </tr>
            )}
            {list.data?.content.map((a) => (
              <tr key={a.id} className="border-t border-slate-100 hover:bg-slate-50">
                <td className="px-4 py-3 font-medium">{a.appointmentDate}</td>
                <td className="px-4 py-3">{a.doctorName}</td>
                <td className="px-4 py-3">{a.patientName}</td>
                <td className="px-4 py-3">{a.notes ?? "—"}</td>
                <td className="px-4 py-3">
                  <button
                    onClick={async () => {
                      if (!confirm(t("appts.confirmCancel", { date: a.appointmentDate }))) return;
                      setNotice(null);
                      try {
                        await remove.mutateAsync(a.id);
                      } catch (err) {
                        setNotice(problemMessage(err));
                      }
                    }}
                    className="text-red-600 hover:underline"
                  >
                    {t("appts.cancel")}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {list.data && (
        <Pagination page={list.data.number} totalPages={list.data.totalPages} totalElements={list.data.totalElements} onPage={setPage} />
      )}

      {booking && <BookModal onClose={() => setBooking(false)} />}
    </div>
  );
}

function BookModal({ onClose }: { onClose: () => void }) {
  const { t } = useTranslation();
  const [values, setValues] = useState(EMPTY);
  const [patientSearch, setPatientSearch] = useState("");
  const [localError, setLocalError] = useState<unknown>(null);
  const doctors = useDoctorOptions();
  const patients = usePatientOptions(patientSearch);
  const create = useCreateAppointment();
  const fields = fieldErrors(create.error ?? localError);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setLocalError(null);
    try {
      if (values.doctorId === "") throw new Error(t("appts.chooseDoctor"));
      if (!values.patientId) throw new Error(t("appts.choosePatient"));
      await create.mutateAsync({
        doctorId: Number(values.doctorId),
        patientId: values.patientId,
        appointmentDate: values.appointmentDate,
        notes: values.notes || undefined,
      });
      onClose();
    } catch (err) {
      setLocalError(err);
    }
  }

  return (
    <Modal title={t("appts.bookTitle")} onClose={onClose}>
      <form onSubmit={submit}>
        <FormError error={create.error ?? localError} />
        <label className="mb-3 block text-sm font-medium">
          {t("appts.doctor")}
          <select
            value={values.doctorId}
            onChange={(e) => setValues((s) => ({ ...s, doctorId: e.target.value === "" ? "" : Number(e.target.value) }))}
            className={inputClass(fields.doctorId)}
          >
            <option value="">{t("appts.chooseDoctor")}</option>
            {doctors.data?.map((d) => (
              <option key={d.id} value={d.id}>
                {d.name} ({d.specialty})
              </option>
            ))}
          </select>
          {fields.doctorId && <span className="mt-1 block text-xs text-red-600">{fields.doctorId}</span>}
        </label>
        <label className="mb-3 block text-sm font-medium">
          {t("appts.patient")}
          <input
            placeholder={t("appts.searchPatient")}
            value={patientSearch}
            onChange={(e) => setPatientSearch(e.target.value)}
            className="mb-2 mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-emerald-600"
          />
          <select
            value={values.patientId}
            onChange={(e) => setValues((s) => ({ ...s, patientId: e.target.value }))}
            className={inputClass(fields.patientId)}
          >
            <option value="">{t("appts.choosePatient")}</option>
            {patients.data?.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name} ({p.email})
              </option>
            ))}
          </select>
          {fields.patientId && <span className="mt-1 block text-xs text-red-600">{fields.patientId}</span>}
        </label>
        <label className="mb-3 block text-sm font-medium">
          {t("appts.date")}
          <input
            type="date"
            value={values.appointmentDate}
            onChange={(e) => setValues((s) => ({ ...s, appointmentDate: e.target.value }))}
            className={inputClass(fields.appointmentDate)}
          />
          {fields.appointmentDate && <span className="mt-1 block text-xs text-red-600">{fields.appointmentDate}</span>}
        </label>
        <label className="mb-4 block text-sm font-medium">
          {t("appts.notesOptional")}
          <input
            value={values.notes ?? ""}
            onChange={(e) => setValues((s) => ({ ...s, notes: e.target.value }))}
            className={inputClass(undefined)}
          />
        </label>
        <button
          type="submit"
          disabled={create.isPending}
          className="w-full rounded bg-emerald-700 py-2.5 font-semibold text-white hover:bg-emerald-800 disabled:opacity-60"
        >
          {create.isPending ? t("appts.booking") : t("appts.book")}
        </button>
      </form>
    </Modal>
  );
}

import { useState } from "react";
import type { FormEvent } from "react";
import { useTranslation } from "react-i18next";
import { useCreatePatient, useDeletePatient, usePatients, useUpdatePatient } from "../api/hooks";
import type { Patient } from "../api/types";
import FormError, { fieldErrors, inputClass } from "../components/FormError";
import Modal from "../components/Modal";
import Pagination from "../components/Pagination";
import { problemMessage } from "../api/client";

const EMPTY = { name: "", email: "", location: "", dateOfBirth: "", registeredDate: "" };

export default function PatientsPage() {
  const { t } = useTranslation();
  const [page, setPage] = useState(0);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [editing, setEditing] = useState<Patient | "new" | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  const list = usePatients({ page, name, email });
  const create = useCreatePatient();
  const update = useUpdatePatient(editing && editing !== "new" ? editing.id : null);
  const remove = useDeletePatient();

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-2xl font-bold">{t("patients.title")}</h1>
        <button
          onClick={() => setEditing("new")}
          className="rounded bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-800"
        >
          {t("patients.new")}
        </button>
      </div>

      <div className="mb-4 flex flex-col gap-2 sm:flex-row">
        <input
          placeholder={t("patients.searchName")}
          value={name}
          onChange={(e) => {
            setName(e.target.value);
            setPage(0);
          }}
          className="rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-emerald-600"
        />
        <input
          placeholder={t("patients.searchEmail")}
          value={email}
          onChange={(e) => {
            setEmail(e.target.value);
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
              <th className="px-4 py-3 font-medium">{t("patients.name")}</th>
              <th className="px-4 py-3 font-medium">{t("patients.email")}</th>
              <th className="px-4 py-3 font-medium">{t("patients.location")}</th>
              <th className="px-4 py-3 font-medium">{t("patients.dob")}</th>
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
                  {t("patients.empty")}
                </td>
              </tr>
            )}
            {list.data?.content.map((p) => (
              <tr key={p.id} className="border-t border-slate-100 hover:bg-slate-50">
                <td className="px-4 py-3 font-medium">{p.name}</td>
                <td className="px-4 py-3">{p.email}</td>
                <td className="px-4 py-3">{p.location}</td>
                <td className="px-4 py-3">{p.dateOfBirth}</td>
                <td className="px-4 py-3">
                  <div className="flex gap-2">
                    <button onClick={() => setEditing(p)} className="text-emerald-700 hover:underline">
                      {t("common.edit")}
                    </button>
                    <button
                      onClick={async () => {
                        if (!confirm(t("patients.confirmDelete", { name: p.name }))) return;
                        setNotice(null);
                        try {
                          await remove.mutateAsync(p.id);
                        } catch (err) {
                          setNotice(problemMessage(err));
                        }
                      }}
                      className="text-red-600 hover:underline"
                    >
                      {t("common.delete")}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {list.data && (
        <Pagination page={list.data.number} totalPages={list.data.totalPages} totalElements={list.data.totalElements} onPage={setPage} />
      )}

      {editing && (
        <PatientFormModal
          key={editing === "new" ? "new" : editing.id}
          initial={editing === "new" ? EMPTY : { ...editing, registeredDate: editing.registeredDate ?? "" }}
          isNew={editing === "new"}
          title={editing === "new" ? t("patients.newTitle") : t("patients.editTitle")}
          saving={editing === "new" ? create.isPending : update.isPending}
          error={editing === "new" ? create.error : update.error}
          onClose={() => {
            setEditing(null);
            create.reset();
            update.reset();
          }}
          onSubmit={async (values) => {
            if (editing === "new") await create.mutateAsync(values);
            else await update.mutateAsync(values);
            setEditing(null);
          }}
        />
      )}
    </div>
  );
}

function PatientFormModal({
  initial,
  isNew,
  title,
  saving,
  error,
  onClose,
  onSubmit,
}: {
  initial: typeof EMPTY;
  isNew: boolean;
  title: string;
  saving: boolean;
  error: unknown;
  onClose: () => void;
  onSubmit: (v: typeof EMPTY) => Promise<unknown>;
}) {
  const { t } = useTranslation();
  const [values, setValues] = useState(initial);
  const [localError, setLocalError] = useState<unknown>(null);
  const fields = fieldErrors(error ?? localError);

  function set<K extends keyof typeof EMPTY>(k: K, v: string) {
    setValues((s) => ({ ...s, [k]: v }));
  }

  async function submit(e: FormEvent) {
    e.preventDefault();
    setLocalError(null);
    try {
      const payload = isNew ? values : { ...values, registeredDate: values.registeredDate || undefined };
      await onSubmit(payload as typeof EMPTY);
    } catch (err) {
      setLocalError(err);
    }
  }

  return (
    <Modal title={title} onClose={onClose}>
      <form onSubmit={submit}>
        <FormError error={error ?? localError} />
        {(["name", "email", "location", "dateOfBirth"] as const).map((f) => (
          <label key={f} className="mb-3 block text-sm font-medium capitalize">
            {{ name: t("patients.name"), email: t("patients.email"), location: t("patients.location"), dateOfBirth: t("patients.dob") }[f]}
            <input
              type={f === "dateOfBirth" ? "date" : f === "email" ? "email" : "text"}
              value={values[f]}
              onChange={(e) => set(f, e.target.value)}
              className={inputClass(fields[f])}
            />
            {fields[f] && <span className="mt-1 block text-xs text-red-600">{fields[f]}</span>}
          </label>
        ))}
        {isNew && (
          <label className="mb-4 block text-sm font-medium">
            {t("patients.registeredDate")}
            <input
              type="date"
              value={values.registeredDate}
              onChange={(e) => set("registeredDate", e.target.value)}
              className={inputClass(fields.registeredDate)}
            />
            {fields.registeredDate && <span className="mt-1 block text-xs text-red-600">{fields.registeredDate}</span>}
          </label>
        )}
        <button
          type="submit"
          disabled={saving}
          className="w-full rounded bg-emerald-700 py-2.5 font-semibold text-white hover:bg-emerald-800 disabled:opacity-60"
        >
          {saving ? t("common.saving") : t("common.save")}
        </button>
      </form>
    </Modal>
  );
}

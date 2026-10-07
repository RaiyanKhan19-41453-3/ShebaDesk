import { useState } from "react";
import { useTranslation } from "react-i18next";
import type { FormEvent } from "react";
import { problemMessage } from "../api/client";
import { useCreateDoctor, useDeleteDoctor, useDoctors, useUpdateDoctor } from "../api/hooks";
import type { Doctor, DoctorForm } from "../api/types";
import FormError, { fieldErrors, inputClass } from "../components/FormError";
import Modal from "../components/Modal";
import Pagination from "../components/Pagination";

const EMPTY: DoctorForm = { name: "", specialty: "", email: "", phone: "" };

export default function DoctorsPage() {
  const { t } = useTranslation();
  const [page, setPage] = useState(0);
  const [name, setName] = useState("");
  const [specialty, setSpecialty] = useState("");
  const [editing, setEditing] = useState<Doctor | "new" | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  const list = useDoctors({ page, name, specialty });
  const create = useCreateDoctor();
  const update = useUpdateDoctor(editing && editing !== "new" ? editing.id : null);
  const remove = useDeleteDoctor();

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-2xl font-bold">{t("doctors.title")}</h1>
        <button
          onClick={() => setEditing("new")}
          className="rounded bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-800"
        >
          {t("doctors.new")}
        </button>
      </div>

      <div className="mb-4 flex flex-col gap-2 sm:flex-row">
        <input
          placeholder={t("doctors.searchName")}
          value={name}
          onChange={(e) => {
            setName(e.target.value);
            setPage(0);
          }}
          className="rounded border border-slate-300 px-3 py-2 text-sm outline-none focus:border-emerald-600"
        />
        <input
          placeholder={t("doctors.searchSpecialty")}
          value={specialty}
          onChange={(e) => {
            setSpecialty(e.target.value);
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
              <th className="px-4 py-3 font-medium">{t("doctors.name")}</th>
              <th className="px-4 py-3 font-medium">{t("doctors.specialty")}</th>
              <th className="px-4 py-3 font-medium">{t("doctors.email")}</th>
              <th className="px-4 py-3 font-medium">{t("doctors.phone")}</th>
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
                  {t("doctors.empty")}
                </td>
              </tr>
            )}
            {list.data?.content.map((d) => (
              <tr key={d.id} className="border-t border-slate-100 hover:bg-slate-50">
                <td className="px-4 py-3 font-medium">{d.name}</td>
                <td className="px-4 py-3">{d.specialty}</td>
                <td className="px-4 py-3">{d.email}</td>
                <td className="px-4 py-3">{d.phone ?? "—"}</td>
                <td className="px-4 py-3">
                  <div className="flex gap-2">
                    <button onClick={() => setEditing(d)} className="text-emerald-700 hover:underline">
                      {t("common.edit")}
                    </button>
                    <button
                      onClick={async () => {
                        if (!confirm(t("doctors.confirmDelete", { name: d.name }))) return;
                        setNotice(null);
                        try {
                          await remove.mutateAsync(d.id);
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
        <DoctorFormModal
          key={editing === "new" ? "new" : editing.id}
          initial={editing === "new" ? EMPTY : { name: editing.name, specialty: editing.specialty, email: editing.email, phone: editing.phone ?? "" }}
          title={editing === "new" ? t("doctors.newTitle") : t("doctors.editTitle")}
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

function DoctorFormModal({
  initial,
  title,
  saving,
  error,
  onClose,
  onSubmit,
}: {
  initial: DoctorForm;
  title: string;
  saving: boolean;
  error: unknown;
  onClose: () => void;
  onSubmit: (v: DoctorForm) => Promise<unknown>;
}) {
  const { t } = useTranslation();
  const [values, setValues] = useState(initial);
  const [localError, setLocalError] = useState<unknown>(null);
  const fields = fieldErrors(error ?? localError);

  function set<K extends keyof DoctorForm>(k: K, v: string) {
    setValues((s) => ({ ...s, [k]: v }));
  }

  async function submit(e: FormEvent) {
    e.preventDefault();
    setLocalError(null);
    try {
      await onSubmit({ ...values, phone: values.phone || undefined });
    } catch (err) {
      setLocalError(err);
    }
  }

  return (
    <Modal title={title} onClose={onClose}>
      <form onSubmit={submit}>
        <FormError error={error ?? localError} />
        <label className="mb-3 block text-sm font-medium">
          {t("doctors.name")}
          <input value={values.name} onChange={(e) => set("name", e.target.value)} className={inputClass(fields.name)} />
          {fields.name && <span className="mt-1 block text-xs text-red-600">{fields.name}</span>}
        </label>
        <label className="mb-3 block text-sm font-medium">
          {t("doctors.specialty")}
          <input value={values.specialty} onChange={(e) => set("specialty", e.target.value)} className={inputClass(fields.specialty)} />
          {fields.specialty && <span className="mt-1 block text-xs text-red-600">{fields.specialty}</span>}
        </label>
        <label className="mb-3 block text-sm font-medium">
          {t("doctors.email")}
          <input type="email" value={values.email} onChange={(e) => set("email", e.target.value)} className={inputClass(fields.email)} />
          {fields.email && <span className="mt-1 block text-xs text-red-600">{fields.email}</span>}
        </label>
        <label className="mb-4 block text-sm font-medium">
          {t("doctors.phoneOptional")}
          <input value={values.phone ?? ""} onChange={(e) => set("phone", e.target.value)} className={inputClass(fields.phone)} />
        </label>
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

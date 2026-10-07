import { problemMessage, type ProblemDetail } from "../api/client";
import axios from "axios";

export function fieldErrors(err: unknown): Record<string, string> {
  if (axios.isAxiosError(err)) {
    const data = err.response?.data as ProblemDetail | undefined;
    if (data?.errors) return data.errors;
  }
  return {};
}

export default function FormError({ error }: { error: unknown }) {
  if (!error) return null;
  const fields = fieldErrors(error);
  const entries = Object.entries(fields);
  return (
    <div role="alert" className="mb-4 rounded bg-red-50 px-3 py-2 text-sm text-red-700">
      {entries.length > 0 ? (
        <ul className="list-disc pl-5">
          {entries.map(([f, m]) => (
            <li key={f}>
              <strong>{f}:</strong> {m}
            </li>
          ))}
        </ul>
      ) : (
        problemMessage(error)
      )}
    </div>
  );
}

export function inputClass(hasError?: string) {
  return `mt-1 w-full rounded border px-3 py-2 text-sm outline-none focus:ring-1 ${
    hasError
      ? "border-red-400 focus:border-red-500 focus:ring-red-500"
      : "border-slate-300 focus:border-emerald-600 focus:ring-emerald-600"
  }`;
}

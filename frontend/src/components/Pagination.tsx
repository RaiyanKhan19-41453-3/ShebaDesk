import { useTranslation } from "react-i18next";

export default function Pagination({
  page,
  totalPages,
  totalElements,
  onPage,
}: {
  page: number;
  totalPages: number;
  totalElements: number;
  onPage: (p: number) => void;
}) {
  const { t } = useTranslation();
  return (
    <div className="mt-4 flex items-center justify-between text-sm">
      <span className="text-slate-500">{t("common.results", { count: totalElements })}</span>
      <div className="flex gap-2">
        <button
          disabled={page <= 0}
          onClick={() => onPage(page - 1)}
          className="rounded border border-slate-300 bg-white px-3 py-1.5 disabled:opacity-40"
        >
          {t("common.prev")}
        </button>
        <span className="px-2 py-1.5 text-slate-500">
          {t("common.pageOf", { page: totalPages === 0 ? 0 : page + 1, total: totalPages })}
        </span>
        <button
          disabled={page + 1 >= totalPages}
          onClick={() => onPage(page + 1)}
          className="rounded border border-slate-300 bg-white px-3 py-1.5 disabled:opacity-40"
        >
          {t("common.next")}
        </button>
      </div>
    </div>
  );
}

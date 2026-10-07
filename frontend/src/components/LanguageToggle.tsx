import { useTranslation } from "react-i18next";

export default function LanguageToggle() {
  const { i18n } = useTranslation();
  const current = i18n.resolvedLanguage?.startsWith("bn") ? "bn" : "en";
  return (
    <div className="flex overflow-hidden rounded bg-emerald-800 text-xs font-semibold" role="group" aria-label="Language">
      {(["en", "bn"] as const).map((lng) => (
        <button
          key={lng}
          onClick={() => void i18n.changeLanguage(lng)}
          className={`px-2.5 py-1.5 ${current === lng ? "bg-white text-emerald-900" : "text-emerald-100 hover:bg-emerald-700"}`}
        >
          {lng === "en" ? "EN" : "বাং"}
        </button>
      ))}
    </div>
  );
}

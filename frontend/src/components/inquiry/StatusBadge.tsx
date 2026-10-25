"use client";

import { Badge } from "@/components/ui";
import { useI18n } from "@/lib/i18n/client";
import type { InquiryStatus } from "@/lib/types";

const tones: Record<InquiryStatus, "neutral" | "success" | "warning" | "danger" | "accent"> = {
  NEW: "accent", IN_PROGRESS: "warning", QUOTED: "neutral", ACCEPTED: "success", COMPLETED: "success", DECLINED: "danger",
};

export function InquiryStatusBadge({ status }: { status: InquiryStatus }) {
  const { t } = useI18n();
  return <Badge tone={tones[status]}>{t.inquiries.status[status]}</Badge>;
}

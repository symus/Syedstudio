"use client";

import { useState } from "react";
import { cx } from "@/components/ui";
import type { ImageRef } from "@/lib/types";

export function Gallery({ images, name }: { images: ImageRef[]; name: string }) {
  const [active, setActive] = useState(0);
  const current = images[active];
  if (!current) {
    return <div className="grid aspect-square place-items-center rounded-[var(--radius-card)] bg-sand text-6xl text-line" aria-hidden>✿</div>;
  }
  return (
    <div>
      <div className="aspect-square overflow-hidden rounded-[var(--radius-card)] bg-sand">
        <img src={current.url} alt={current.altText} width={900} height={900} className="h-full w-full object-cover" />
      </div>
      {images.length > 1 && (
        <ul className="mt-3 flex gap-2 overflow-x-auto pb-1">
          {images.map((img, i) => (
            <li key={img.id}>
              <button type="button" onClick={() => setActive(i)} aria-label={`${name} ${i + 1}`} aria-current={i === active}
                className={cx("h-20 w-20 overflow-hidden rounded-xl border-2", i === active ? "border-terracotta" : "border-transparent")}>
                <img src={img.url} alt="" loading="lazy" className="h-full w-full object-cover" />
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

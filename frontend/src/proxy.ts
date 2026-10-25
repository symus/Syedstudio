import { NextResponse, type NextRequest } from "next/server";

/**
 * Fast redirect to /login for private areas when no session cookie is present.
 * This is UX only — every protected API call is authorised by the backend.
 */
export function proxy(request: NextRequest) {
  if (!request.cookies.has("craftora_session")) {
    const url = request.nextUrl.clone();
    url.pathname = "/login";
    url.search = `?next=${encodeURIComponent(request.nextUrl.pathname + request.nextUrl.search)}`;
    return NextResponse.redirect(url);
  }
  return NextResponse.next();
}

export const config = {
  matcher: ["/dashboard/:path*", "/admin/:path*", "/account/:path*", "/preview/:path*"],
};

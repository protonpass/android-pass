#!/usr/bin/env python3
"""Merge Google's upstream privileged-browser allowlist with supplemental extras.

Usage: merge_privileged_browser_allowlist.py <snapshot.json> <extras.json>...

Strips publicly-known AOSP test-key signers from the upstream snapshot (their private keys
are public, so any app could forge them to claim a privileged web origin), then merges each
extras file by package_name. Extras are subject to the same test-key strip, and merging is
per-package so a future upstream adoption dedupes fingerprints instead of duplicating
entries. Provenance '_source' keys are dropped from the output. Upstream ordering is kept
stable; new packages are appended alphabetically for a minimal, deterministic diff.

Idempotent: re-running on an already-merged snapshot is a no-op.
"""

import json
import os
import sys

# SHA-256 of the checked-in AOSP test certs (build/target/product/security/*.x509.pem):
# platform, testkey, media, shared. Upstream marks some as `release`, so we strip them on
# every sync. Keep in sync with PasskeyOriginVerifierTest.
PUBLIC_TEST_KEYS = {
    "C8:A2:E9:BC:CF:59:7C:2F:B6:DC:66:BE:E2:93:FC:13:F2:FC:47:EC:77:BC:6B:2B:0D:52:C1:1F:51:19:2A:B8",
    "A4:0D:A8:0A:59:D1:70:CA:A9:50:CF:15:C1:8C:45:4D:47:A3:9B:26:98:9D:8B:64:0E:CD:74:5B:A7:1B:F5:DC",
    "46:59:83:F7:79:1F:2A:BE:B4:3E:A2:CB:DC:7F:21:A8:26:0B:72:BC:08:A5:5C:83:9F:C1:A4:3B:C7:41:A8:1E",
    "28:BB:FE:4A:7B:97:E7:46:81:DC:55:C2:FB:B6:CC:B8:D6:C7:49:63:73:3F:6A:F6:AE:74:D8:C3:A6:E8:79:FD",
}


def sanitize(apps, deny):
    sanitized = []
    for app in apps:
        info = app.get("info", {})
        info["signatures"] = [
            sig
            for sig in info.get("signatures", [])
            if sig.get("cert_fingerprint_sha256", "").upper() not in deny
        ]
        if info["signatures"]:
            sanitized.append(app)
    return sanitized


def merge_extras(apps, extras_path, deny):
    if not os.path.exists(extras_path):
        # Paths are passed explicitly by the caller; a missing file means a rename or a
        # wrong cwd. Failing loudly is safer than silently shipping snapshots without
        # the extras (or without the F-Droid keys).
        raise SystemExit(f"Extras file not found: {extras_path}")
    with open(extras_path, "r", encoding="utf-8") as file:
        extras_root = json.load(file)
    extras = [
        app
        for app in extras_root.get("apps", [])
        if app.get("info", {}).get("signatures")
    ]
    extras = sanitize(extras, deny)

    by_pkg = {app["info"]["package_name"]: app for app in apps}
    for app in extras:
        pkg = app["info"]["package_name"]
        existing = by_pkg.get(pkg)
        if existing is None:
            apps.append(app)
            by_pkg[pkg] = app
        else:
            known = {
                s.get("cert_fingerprint_sha256", "").upper()
                for s in existing["info"]["signatures"]
            }
            existing["info"]["signatures"].extend(
                sig
                for sig in app["info"]["signatures"]
                if sig.get("cert_fingerprint_sha256", "").upper() not in known
            )


def main():
    path = sys.argv[1]
    extras_paths = sys.argv[2:]

    with open(path, "r", encoding="utf-8") as file:
        root = json.load(file)

    apps = root.get("apps")
    if not isinstance(apps, list) or not apps:
        raise SystemExit("Allowlist must contain a non-empty apps array")

    deny = {fp.upper() for fp in PUBLIC_TEST_KEYS}
    apps = sanitize(apps, deny)
    if not apps:
        raise SystemExit("Allowlist is empty after removing public test-key signers")

    original_pkgs = {app["info"]["package_name"] for app in apps}
    for extras_path in extras_paths:
        merge_extras(apps, extras_path, deny)

    for app in apps:
        app["info"].pop("_source", None)
    kept = [a for a in apps if a["info"]["package_name"] in original_pkgs]
    new_apps = sorted(
        (a for a in apps if a["info"]["package_name"] not in original_pkgs),
        key=lambda a: a["info"]["package_name"],
    )
    root["apps"] = kept + new_apps

    first = root["apps"][0]
    if first.get("type") != "android":
        raise SystemExit("Allowlist entries must use type=android")
    info = first.get("info", {})
    if not info.get("package_name"):
        raise SystemExit("Allowlist entries must contain info.package_name")
    signatures = info.get("signatures")
    if not isinstance(signatures, list) or not signatures:
        raise SystemExit("Allowlist entries must contain signatures")
    if not signatures[0].get("cert_fingerprint_sha256"):
        raise SystemExit("Allowlist signatures must contain cert_fingerprint_sha256")

    with open(path, "w", encoding="utf-8") as file:
        file.write(json.dumps(root, indent=2, ensure_ascii=False))
        file.write("\n")


if __name__ == "__main__":
    main()

---
name: deploy
description: Build webshooter and install to the running Android emulator (Windows-local) and iOS simulator (Mac-remote via SSH) in parallel. Use when the user says /deploy, "deploy", "ship it to both", or asks to push current changes to both running emulators. Accepts target flags (/deploy ios, /deploy android) and flavor flags (/deploy prod); combine in any order. Windows-only trigger; requires the macmini SSH alias and at least one AVD on Windows + one iOS simulator runtime on the Mac.
allowed-tools: Bash(pwsh:*), Bash(git status:*), Bash(git log -1:*)
---

# /deploy

Run `pwsh -File scripts/deploy.ps1 $ARGUMENTS` from the repo root.

Stream the script's output verbatim. Do not paraphrase build errors — the script already prints colored prefixes (`[ANDROID]`, `[IOS]`) and a tail of the failing log when a target fails.

On exit, report the PASS/FAIL summary lines the script printed — one per deployed target — plus the flavor and any stash warning. Do not retry on failure; surface the error and stop.

## Arguments

Pass `$ARGUMENTS` through unchanged. The script accepts these tokens in any order, defaulting to both targets + staging when empty:

- `ios` — iOS only (skip Android job).
- `android` — Android only (skip iOS job + SSH).
- `prod` — switch flavor (default `staging`).
- `staging` — explicit staging.
- `-ForceWip` — auto-commit a dirty Windows tree as `wip: deploy <timestamp>` before pushing.

Examples: `/deploy`, `/deploy ios`, `/deploy android prod`, `/deploy ios prod -ForceWip`.

## When NOT to invoke

- Don't run from the Mac. The script asserts `$IsWindows` and aborts.
- Don't run if the user just wants to build — this also installs and launches on the emulators.

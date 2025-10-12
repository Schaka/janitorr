#!/usr/bin/env python3
"""
Temporal commit history rewriting helper for janitorr.

IMPORTANT:
  - Do NOT commit this file. Use it once and then delete it.
  - Requires: git, python3, and git-filter-repo installed (pip install git-filter-repo).
  - It rewrites ALL commits on the current branch (default main) into Conventional Commits,
    translating some common Spanish prefixes/verbs to English and normalizing bracket tags.
  - Merge commits (multiple parents) are left untouched (message preserved) per user decision.

USAGE (from inside the git repo you want to rewrite):
  1. Ensure clean worktree: git status should show no changes.
  2. Backup (optional): git clone --mirror <origin_url> ../janitorr-backup.git
  3. Install tool if missing: python3 -m pip install --upgrade git-filter-repo
  4. Run: python3 rewrite_commits.py
  5. Inspect: git log --oneline | head -n 25
  6. Force push (ONLY when satisfied): git push --force-with-lease origin main

SAFEGUARDS:
  - Aborts if uncommitted changes exist.
  - Aborts if branch != main unless you pass --allow-non-main.
  - Will not modify messages already in valid Conventional Commit format.

LIMITATIONS:
  - Translation is heuristic; only a curated subset of Spanish words is mapped.
  - Complex multi-line bodies are preserved but not translated.
  - If a subject becomes empty after cleaning, it is replaced with "update".
"""

from __future__ import annotations
import argparse
import os
import re
import subprocess
import sys
import textwrap
from typing import Dict

VALID_TYPES = {
    "feat", "fix", "docs", "style", "refactor", "perf", "test", "build", "ci", "chore", "revert"
}

SERVICE_SCOPES = {
    "jellyfin", "emby", "sonarr", "radarr", "jellyseerr", "overseerr", "cleanup", "stats", "media", "mediaserver"
}

BRACKET_TYPE_MAP: Dict[str, str] = {
    # Common explicit types
    "build": "build",
    "ci": "ci",
    "docs": "docs",
    "doc": "docs",
    "fix": "fix",
    "bug": "fix",
    "feat": "feat",
    "feature": "feat",
    "refactor": "refactor",
    "perf": "perf",
    "test": "test",
    "chore": "chore",
    # Map domain names to feat scope
    "jellyfin": "feat",
    "emby": "feat",
    "sonarr": "feat",
    "radarr": "feat",
    "jellyseerr": "feat",
    "overseerr": "feat",
    "cleanup": "chore",
}

SPANISH_VERB_MAP: Dict[str, str] = {
    "agrega": "add",
    "agregado": "add",
    "añade": "add",
    "añadido": "add",
    "crea": "create",
    "corrige": "fix",
    "arregla": "fix",
    "corrig": "fix",
    "soluciona": "fix",
    "mejora": "improve",
    "optimiza": "optimize",
    "actualiza": "update",
    "actualización": "update",
    "refactoriza": "refactor",
    "documenta": "docs",
    "renombra": "rename",
    "elimina": "remove",
    "borra": "remove",
    "limpia": "cleanup",
    "configura": "configure",
    "soporta": "support",
}

CONVENTIONAL_RE = re.compile(r"^(?P<type>\w+)(\([^)]*\))?!?: .+")
BRACKET_RE = re.compile(r"^\s*\[([^\]]+)\]\s*:?-?\s*(.*)$")
EMOJI_RE = re.compile(r"^[\W_]+")


def detect_type_and_scope_from_first_word(word: str):
    lw = word.lower()
    for k, v in [
        ("add", "feat"), ("create", "feat"), ("implement", "feat"),
        ("fix", "fix"), ("resolve", "fix"), ("repair", "fix"),
        ("bump", "chore"), ("update", "chore"), ("upgrade", "chore"),
        ("refactor", "refactor"), ("perf", "perf"), ("optimiz", "perf"),
        ("test", "test"), ("doc", "docs"), ("readme", "docs"),
        ("build", "build"), ("ci", "ci"), ("config", "chore"), ("clean", "chore")
    ]:
        if lw.startswith(k):
            return v, None
    return None, None


def translate_spanish_first_word(word: str) -> str:
    lw = word.lower()
    return SPANISH_VERB_MAP.get(lw, word)


def normalize_subject(raw: str) -> str:
    # Strip emojis / leading punctuation
    s = raw.strip()
    s = EMOJI_RE.sub("", s)  # crude remove of leading symbols/emojis
    s = s.strip()
    # Remove trailing period
    if s.endswith('.') and len(s) > 1:
        s = s[:-1]
    return s or "update"


def build_conventional(type_: str, scope: str | None, subject: str) -> str:
    if scope:
        return f"{type_}({scope}): {subject}"
    return f"{type_}: {subject}"


def transform_subject_line(line: str) -> str:
    original = line
    if CONVENTIONAL_RE.match(line):
        return line  # already conventional

    # Handle bracket prefix
    m = BRACKET_RE.match(line)
    ctype = None
    scope = None
    remainder = line
    if m:
        tag = m.group(1).strip().lower()
        remainder = m.group(2).strip()
        base_type = BRACKET_TYPE_MAP.get(tag)
        if base_type:
            ctype = base_type
            if base_type == "feat" and tag in SERVICE_SCOPES:
                scope = tag
        else:
            # treat unknown bracket as scope for feat
            scope = tag if tag else None
            ctype = "feat"

    # Split first word for heuristic
    if not ctype:
        parts = remainder.split()
        if parts:
            first = translate_spanish_first_word(parts[0])
            ctype_guess, scope_guess = detect_type_and_scope_from_first_word(first)
            if ctype_guess:
                ctype = ctype_guess
                if scope_guess:
                    scope = scope_guess
                # Replace first word if translated
                parts[0] = first
                remainder = " ".join(parts)

    if not ctype:
        # Default bucket
        ctype = "chore"

    # Attempt scope inference if still none
    if not scope:
        lowered = remainder.lower()
        for svc in SERVICE_SCOPES:
            if svc in lowered:
                scope = svc
                break

    subject = normalize_subject(remainder)
    result = build_conventional(ctype, scope, subject)
    # Fallback safety: ensure type in VALID_TYPES
    t = result.split(':', 1)[0]
    base = t.split('(')[0]
    if base not in VALID_TYPES:
        result = build_conventional('chore', scope, subject)
    return result


def write_callback_file(path: str):
    """Genera un callback autónomo y simple para git-filter-repo.

    Evitamos interpolaciones complejas para no introducir errores de sintaxis.
    """
    code = r"""# Temporary callback generated by rewrite_commits.py
import re

CONVENTIONAL_RE = re.compile(r'^(feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert)(\([^)]*\))?!?: .+')
BRACKET_RE = re.compile(r'^\s*\[([^\]]+)\]\s*:?-?\s*(.*)$')
EMOJI_RE = re.compile(r'^\W+')

SERVICE_SCOPES = {
    'jellyfin','emby','sonarr','radarr','jellyseerr','overseerr','cleanup','stats','media','mediaserver'
}

SPANISH_MAP = {
    'agrega':'add','agregado':'add','añade':'add','añadido':'add','crea':'create',
    'corrige':'fix','arregla':'fix','soluciona':'fix','mejora':'improve','optimiza':'optimize',
    'actualiza':'update','refactoriza':'refactor','documenta':'docs','renombra':'rename',
    'elimina':'remove','borra':'remove','limpia':'cleanup','configura':'configure','soporta':'support'
}

def _normalize(s: str) -> str:
    s = s.strip()
    s = EMOJI_RE.sub('', s).strip()
    if s.endswith('.') and len(s) > 1:
        s = s[:-1]
    return s or 'update'

def _guess_type_scope(remainder: str):
    low = remainder.lower()
    def starts(*pref):
        return any(low.startswith(p) for p in pref)
    if starts('fix','resolve','arregla','corrige'): return 'fix', None
    if starts('add','create','implement','agrega','añade'): return 'feat', None
    if starts('refactor','refactoriza'): return 'refactor', None
    if starts('test'): return 'test', None
    if starts('doc','readme','documenta'): return 'docs', None
    if starts('perf','optimiz'): return 'perf', None
    if starts('build'): return 'build', None
    if starts('ci'): return 'ci', None
    if starts('bump','update','upgrade'): return 'chore', None
    if starts('clean','limpia'): return 'chore', None
    return 'chore', None

def _transform(line: str) -> str:
    if CONVENTIONAL_RE.match(line):
        return line
    m = BRACKET_RE.match(line)
    remainder = line
    ctype = None
    scope = None
    if m:
        tag = m.group(1).strip().lower()
        remainder = m.group(2).strip()
        domain_tags = {'jellyfin','emby','sonarr','radarr','jellyseerr','overseerr'}
        if tag in domain_tags:
            ctype = 'feat'
            scope = tag
        elif tag in {'build','ci','docs','fix','feat','refactor','perf','test','chore'}:
            ctype = tag
        else:
            ctype = 'feat'
            scope = tag
    if not ctype:
        parts = remainder.split()
        if parts:
            first = parts[0]
            if first.lower() in SPANISH_MAP:
                parts[0] = SPANISH_MAP[first.lower()]
                remainder = ' '.join(parts)
            ctype, _ = _guess_type_scope(remainder)
    low = remainder.lower()
    if not scope:
        for svc in SERVICE_SCOPES:
            if svc in low:
                scope = svc
                break
    subject = _normalize(remainder)
    if scope:
        return f"{ctype}({scope}): {subject}"
    return f"{ctype}: {subject}"

def commit_callback(commit):
    # Skip merge commits
    if len(commit.parents) > 1:
        return
    try:
        msg = commit.message.decode('utf-8', errors='replace')
    except Exception:
        return
    lines = msg.splitlines()
    if not lines:
        return
    lines[0] = _transform(lines[0])
    commit.message = ('\n'.join(lines)).encode('utf-8')
"""
    with open(path, 'w', encoding='utf-8') as fh:
        fh.write(code)


def ensure_clean_worktree():
    res = subprocess.run(["git", "status", "--porcelain"], capture_output=True, text=True)
    if res.returncode != 0:
        print("[ERROR] git status failed", file=sys.stderr)
        sys.exit(1)
    if res.stdout.strip():
        print("[ABORT] Uncommitted changes detected. Commit/stash first.", file=sys.stderr)
        sys.exit(2)


def main():
    parser = argparse.ArgumentParser(description="Rewrite commit messages to Conventional Commits")
    parser.add_argument("--allow-non-main", action="store_true", help="Allow rewriting even if current branch != main")
    args = parser.parse_args()

    # Basic repo checks
    if not os.path.isdir('.git'):
        print("[ERROR] This is not a git repository (missing .git directory)", file=sys.stderr)
        sys.exit(1)

    # Determine current branch (may fail in detached head)
    branch = subprocess.run(["git", "rev-parse", "--abbrev-ref", "HEAD"], capture_output=True, text=True)
    if branch.returncode != 0:
        print("[ERROR] Cannot determine current branch", file=sys.stderr)
        sys.exit(1)
    current_branch = branch.stdout.strip()
    if current_branch != "main" and not args.allow_non_main:
        print(f"[ABORT] Current branch is '{current_branch}', not 'main'. Use --allow-non-main to override.")
        sys.exit(3)

    ensure_clean_worktree()

    # Check git-filter-repo availability
    has_gfr = subprocess.run([sys.executable, "-c", "import git_filter_repo"], capture_output=True)
    if has_gfr.returncode != 0:
        print("[INFO] git-filter-repo not found in current interpreter; attempting pip install ...")
        pip = subprocess.run([sys.executable, "-m", "pip", "install", "--quiet", "git-filter-repo"], capture_output=True)
        if pip.returncode != 0:
            print("[ERROR] Failed to install git-filter-repo automatically", file=sys.stderr)
            sys.exit(4)

    callback_path = ".tmp_rewrite_callback.py"
    if os.path.exists(callback_path):
        os.remove(callback_path)
    write_callback_file(callback_path)

    # Capture initial HEAD and commit count
    head_before = subprocess.run(["git", "rev-parse", "HEAD"], capture_output=True, text=True)
    head_before_val = head_before.stdout.strip() if head_before.returncode == 0 else "UNKNOWN"
    count_before = subprocess.run(["git", "rev-list", "--count", "HEAD"], capture_output=True, text=True)
    count_before_val = count_before.stdout.strip() if count_before.returncode == 0 else "0"
    print(f"[INFO] Running git filter-repo ... this may take a moment (HEAD before: {head_before_val}, commits: {count_before_val})")
    cmd = ["git", "filter-repo", "--force", "--commit-callback", f"exec(open('{callback_path}').read())"]
    run = subprocess.run(cmd, text=True, capture_output=True)
    if run.returncode != 0:
        print("[ERROR] git filter-repo failed:\n" + run.stderr, file=sys.stderr)
        # Keep callback for inspection
        sys.exit(run.returncode)

    head_after = subprocess.run(["git", "rev-parse", "HEAD"], capture_output=True, text=True)
    head_after_val = head_after.stdout.strip() if head_after.returncode == 0 else "UNKNOWN"
    count_after = subprocess.run(["git", "rev-list", "--count", "HEAD"], capture_output=True, text=True)
    count_after_val = count_after.stdout.strip() if count_after.returncode == 0 else "0"
    print(f"[INFO] Rewrite completed (HEAD after: {head_after_val}, commits: {count_after_val})")

    # Basic validation sample
    sample = subprocess.run(["git", "log", "--pretty=%s", "-n", "30"], capture_output=True, text=True)
    lines = sample.stdout.strip().splitlines()
    bad = [l for l in lines if not CONVENTIONAL_RE.match(l)]
    print("[INFO] Sample of last 12 subjects after rewrite:")
    for l in lines[:12]:
        print("  ", l)
    if bad:
        print(f"[WARN] {len(bad)} of last 30 commit subjects still not matching pattern - manual review recommended")
    else:
        print("[INFO] All sampled commits match Conventional Commits pattern.")

    # Write a small report file
    try:
        with open('.rewrite_report.txt', 'w', encoding='utf-8') as rep:
            rep.write(f"HEAD before: {head_before_val}\n")
            rep.write(f"HEAD after: {head_after_val}\n")
            rep.write(f"Commits before: {count_before_val}\n")
            rep.write(f"Commits after: {count_after_val}\n\n")
            rep.write("Last 30 subjects after rewrite:\n")
            for l in lines:
                rep.write(l + "\n")
            if bad:
                rep.write(f"\nWARNING: {len(bad)} of last 30 not matching conventional pattern.\n")
    except Exception as e:
        print(f"[WARN] Could not write .rewrite_report.txt: {e}")

    print("\nNEXT STEPS:")
    print("  1. Review 'git log --oneline'.")
    print("  2. Force push when satisfied: git push --force-with-lease origin main")
    print("  3. Delete this script and callback file: rm rewrite_commits.py .tmp_rewrite_callback.py")


if __name__ == "__main__":
    main()

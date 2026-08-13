#!/usr/bin/env bash
set -euo pipefail

ai_dir="${AI_WORKSPACE:-.release-ai}"
notes="$ai_dir/release-notes.md"
prompt="$ai_dir/prompt.md"

if [[ -e "$notes" ]]; then
  echo "::error::release-notes.md unexpectedly exists before Copilot runs."
  exit 1
fi

if [[ ! -s "$prompt" ]]; then
  echo "::error::The Copilot prompt is missing or empty."
  exit 1
fi

(
  cd "$ai_dir"
  copilot \
    --prompt "$(cat prompt.md)" \
    --model=gpt-5.6-sol \
    --reasoning-effort=xhigh \
    --attachment .release-context/metadata.txt \
    --attachment .release-context/stat.txt \
    --attachment .release-context/files.txt \
    --attachment .release-context/changes.diff \
    --attachment .release-context/commits.txt \
    --available-tools='view,create,edit,apply_patch' \
    --allow-tool=read \
    --allow-tool='write(release-notes.md)' \
    --disable-builtin-mcps \
    --no-custom-instructions \
    --no-experimental \
    --no-remote \
    --no-remote-export \
    --no-auto-update \
    --no-ask-user
)

if [[ ! -s "$notes" ]]; then
  echo "::error::Copilot did not generate release notes."
  exit 1
fi

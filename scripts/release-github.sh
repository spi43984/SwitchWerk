#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

OFFICIAL_REPOSITORY="spi43984/SwitchWerk"
VERSION="${1:-}"
LATEST_RELEASE_VERSION=""

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Fehler: Befehl '$1' wurde nicht gefunden."
    exit 1
  fi
}

require_clean_worktree() {
  if [[ -n "$(git status --porcelain)" ]]; then
    echo "Fehler: Arbeitsverzeichnis ist nicht sauber."
    git status --short
    exit 1
  fi
}

require_keystore_properties() {
  if [[ ! -f keystore.properties ]]; then
    echo "Fehler: keystore.properties fehlt."
    exit 1
  fi

  local missing=0
  for key in storeFile storePassword keyAlias keyPassword; do
    if ! grep -Eq "^[[:space:]]*${key}[[:space:]]*=[[:space:]]*[^[:space:]].*$" keystore.properties; then
      echo "Fehler: keystore.properties enthält keinen Wert für '${key}'."
      missing=1
    fi
  done

  if [[ "${missing}" -ne 0 ]]; then
    exit 1
  fi

  local store_file
  store_file="$(awk -F= '/^[[:space:]]*storeFile[[:space:]]*=/{sub(/^[[:space:]]+/, "", $2); sub(/[[:space:]]+$/, "", $2); print $2; exit}' keystore.properties)"
  if [[ ! -f "${store_file}" ]]; then
    echo "Fehler: Die in keystore.properties angegebene Keystore-Datei existiert nicht."
    exit 1
  fi

  if git check-ignore -q keystore.properties; then
    :
  else
    echo "Fehler: keystore.properties wird nicht von Git ignoriert."
    exit 1
  fi
}

require_apksigner() {
  if [[ -n "${ANDROID_HOME:-}" && -d "${ANDROID_HOME}/build-tools" ]]; then
    APKSIGNER="$(find "${ANDROID_HOME}/build-tools" -name apksigner | sort -V | tail -n 1)"
  else
    APKSIGNER="$(command -v apksigner || true)"
  fi

  if [[ -z "${APKSIGNER}" ]]; then
    echo "Fehler: apksigner wurde nicht gefunden."
    exit 1
  fi
}

require_official_repository() {
  local origin_url
  origin_url="$(git remote get-url origin 2>/dev/null || true)"
  if [[ ! "${origin_url}" =~ (^|[:/])spi43984/SwitchWerk(.git)?$ ]]; then
    echo "Fehler: origin zeigt nicht auf ${OFFICIAL_REPOSITORY}."
    exit 1
  fi
}

show_latest_release() {
  local -a release_data
  local latest_release_tag
  local latest_release

  mapfile -t release_data < <(
    gh release list \
      --repo "${OFFICIAL_REPOSITORY}" \
      --exclude-drafts \
      --limit 1 \
      --json tagName,name,publishedAt \
      --jq '.[0] | .tagName, "\(.tagName) - \(.name // "ohne Titel") (veröffentlicht: \(.publishedAt))"'
  )

  latest_release_tag="${release_data[0]:-}"
  latest_release="${release_data[1]:-}"

  if [[ -z "${latest_release_tag}" || -z "${latest_release}" ]]; then
    echo "Fehler: Es wurde kein veröffentlichtes GitHub-Release gefunden."
    exit 1
  fi

  LATEST_RELEASE_VERSION="${latest_release_tag#v}"
  if ! [[ "${LATEST_RELEASE_VERSION}" =~ ^[0-9]+[.][0-9]+[.][0-9]+$ ]]; then
    echo "Fehler: Das letzte Release '${latest_release_tag}' hat kein unterstütztes Versionsformat."
    exit 1
  fi

  echo "Letztes veröffentlichtes GitHub-Release: ${latest_release}"
}

is_version_greater() {
  local candidate="$1"
  local current="$2"
  local candidate_major candidate_minor candidate_patch
  local current_major current_minor current_patch

  IFS='.' read -r candidate_major candidate_minor candidate_patch <<< "${candidate}"
  IFS='.' read -r current_major current_minor current_patch <<< "${current}"

  if (( 10#${candidate_major} != 10#${current_major} )); then
    (( 10#${candidate_major} > 10#${current_major} ))
    return
  fi
  if (( 10#${candidate_minor} != 10#${current_minor} )); then
    (( 10#${candidate_minor} > 10#${current_minor} ))
    return
  fi
  (( 10#${candidate_patch} > 10#${current_patch} ))
}

prepare_release_notes() {
  local previous_tag="$1"
  local target_tag="$2"
  local notes_file
  local editor_command
  local editor_args

  notes_file="$(mktemp "/tmp/switchwerk-release-${VERSION}.XXXXXX.md")"
  RELEASE_NOTES_FILE="${notes_file}"

  cat > "${notes_file}" <<EOF
## Änderungen

- ...

**Vollständiges Changelog:** https://github.com/${OFFICIAL_REPOSITORY}/compare/${previous_tag}...${target_tag}
EOF

  editor_command="${VISUAL:-${EDITOR:-vi}}"
  read -r -a editor_args <<< "${editor_command}"
  "${editor_args[@]}" "${notes_file}"

  if grep -Fxq -- "- ..." "${notes_file}"; then
    echo "Fehler: Release Notes enthalten noch den Platzhalter '- ...'."
    echo "Datei: ${notes_file}"
    exit 1
  fi

  if [[ ! -s "${notes_file}" ]]; then
    echo "Fehler: Release Notes sind leer."
    exit 1
  fi
}

require_command git
require_command gh
require_official_repository
show_latest_release

if [[ -z "${VERSION}" ]]; then
  if ! read -r -p "Release-Version (MAJOR.MINOR.PATCH): " VERSION; then
    echo "Fehler: Keine Release-Version eingegeben."
    exit 1
  fi
fi

if [[ -z "${VERSION}" ]]; then
  echo "Fehler: Die Release-Version darf nicht leer sein."
  exit 1
fi

if ! [[ "${VERSION}" =~ ^[0-9]+[.][0-9]+[.][0-9]+$ ]]; then
  echo "Fehler: VERSION muss dem Muster MAJOR.MINOR.PATCH entsprechen."
  exit 1
fi

echo "Neue Release-Version: v${VERSION}"

if ! is_version_greater "${VERSION}" "${LATEST_RELEASE_VERSION}"; then
  echo "Fehler: Die neue Version muss größer als ${LATEST_RELEASE_VERSION} sein."
  exit 1
fi

if ! read -r -p "Release v${VERSION} wirklich erstellen? [j/N]: " CONFIRM_RELEASE; then
  echo "Release-Erstellung abgebrochen."
  exit 1
fi
case "${CONFIRM_RELEASE}" in
  j|J|ja|Ja|JA)
    ;;
  *)
    echo "Release-Erstellung abgebrochen."
    exit 1
    ;;
esac

require_command perl
require_keystore_properties
require_apksigner

IFS='.' read -r VERSION_MAJOR VERSION_MINOR VERSION_PATCH <<< "${VERSION}"
VERSION_CODE=$(( VERSION_MAJOR * 10000 + VERSION_MINOR * 100 + VERSION_PATCH ))

TAG="v${VERSION}"
APK_SOURCE="app/build/outputs/apk/release/app-release.apk"
APK_NAME="SwitchWerk-${VERSION}.apk"
APK_UPLOAD_PATH="app/build/outputs/apk/release/${APK_NAME}"
APK_LATEST_NAME="SwitchWerk.apk"
APK_LATEST_UPLOAD_PATH="app/build/outputs/apk/release/${APK_LATEST_NAME}"
RELEASE_NOTES_FILE=""

git status
git switch main
git pull --ff-only

require_clean_worktree

if git rev-parse "${TAG}" >/dev/null 2>&1 || \
  git ls-remote --exit-code --tags origin "refs/tags/${TAG}" >/dev/null 2>&1; then
  echo "Fehler: Tag ${TAG} existiert bereits."
  exit 1
fi

if gh release view "${TAG}" --repo "${OFFICIAL_REPOSITORY}" >/dev/null 2>&1; then
  echo "Fehler: Release ${TAG} existiert bereits."
  exit 1
fi

PREVIOUS_TAG="$(
  gh release list \
    --repo "${OFFICIAL_REPOSITORY}" \
    --exclude-drafts \
    --exclude-pre-releases \
    --limit 1 \
    --json tagName \
    --jq '.[0].tagName'
)"

if [[ -z "${PREVIOUS_TAG}" ]]; then
  echo "Fehler: Es wurde kein vorheriges reguläres Release gefunden."
  exit 1
fi

perl -0pi -e "s/versionCode = \d+/versionCode = ${VERSION_CODE}/" app/build.gradle.kts
perl -0pi -e "s/versionName = \"[^\"]+\"/versionName = \"${VERSION}\"/" app/build.gradle.kts

git diff -- app/build.gradle.kts

./gradlew lintRelease
./gradlew testDebugUnitTest
./gradlew clean assembleRelease

test -f "${APK_SOURCE}"
"${APKSIGNER}" verify --verbose "${APK_SOURCE}"
cp "${APK_SOURCE}" "${APK_UPLOAD_PATH}"
cp "${APK_SOURCE}" "${APK_LATEST_UPLOAD_PATH}"

git status --short

git add app/build.gradle.kts

if ! git diff --cached --quiet; then
  git commit -m "chore: release ${VERSION}"
  git push origin main
else
  echo "Keine Änderungen an app/build.gradle.kts."
fi

require_clean_worktree
prepare_release_notes "${PREVIOUS_TAG}" "${TAG}"

git tag -a "${TAG}" -m "Release ${VERSION}"
git push origin "${TAG}"

gh release create "${TAG}" \
  "${APK_UPLOAD_PATH}" \
  "${APK_LATEST_UPLOAD_PATH}" \
  --repo "${OFFICIAL_REPOSITORY}" \
  --title "SwitchWerk ${VERSION}" \
  --notes-file "${RELEASE_NOTES_FILE}"

gh release view "${TAG}" --repo "${OFFICIAL_REPOSITORY}" --web

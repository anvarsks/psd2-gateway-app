#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ARTIFACTORY_VERSION="${1:-7.135.1}"
ARCHIVE_NAME="jfrog-artifactory-oss-${ARTIFACTORY_VERSION}-compose.tar.gz"
DOWNLOAD_URL="https://releases.jfrog.io/artifactory/bintray-artifactory/org/artifactory/oss/docker/jfrog-artifactory-oss/${ARTIFACTORY_VERSION}/${ARCHIVE_NAME}"
TARGET_DIR="${SCRIPT_DIR}/artifactory-oss-${ARTIFACTORY_VERSION}"

mkdir -p "${SCRIPT_DIR}"
cd "${SCRIPT_DIR}"

download_archive() {
  rm -f "${ARCHIVE_NAME}"
  curl -f -g -L -o "${ARCHIVE_NAME}" "${DOWNLOAD_URL}"
}

if [ ! -f "${ARCHIVE_NAME}" ]; then
  download_archive
fi

if ! tar -tzf "${ARCHIVE_NAME}" >/dev/null 2>&1; then
  echo "Downloaded archive is invalid. Re-downloading..." >&2
  download_archive
fi

if ! tar -tzf "${ARCHIVE_NAME}" >/dev/null 2>&1; then
  echo "Could not download a valid Artifactory OSS archive from:" >&2
  echo "${DOWNLOAD_URL}" >&2
  exit 1
fi

if [ ! -d "${TARGET_DIR}" ]; then
  mkdir -p "${TARGET_DIR}"
  tar -xvf "${ARCHIVE_NAME}" -C "${TARGET_DIR}" --strip-components=1
fi

cat <<EOF
Artifactory OSS bundle prepared at:
${TARGET_DIR}

Start it with:
cd "${TARGET_DIR}" && docker compose up -d
EOF

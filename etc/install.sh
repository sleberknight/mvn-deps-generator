#!/usr/bin/env bash

set -euo pipefail

usage() {
  echo "Usage: $(basename "$0") [-h]"
  echo ""
  echo "Installs mvn-deps-generator to INSTALL_DIR (default: \${HOME}/.local/bin)."
  echo ""
  echo "Options:"
  echo "  -h    Show this help message and exit"
  echo ""
  echo "Environment variables:"
  echo "  INSTALL_DIR   Directory to install into (e.g. INSTALL_DIR=/usr/local/bin ./install.sh)"
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

INSTALL_DIR="${INSTALL_DIR:-${HOME}/.local/bin}"
WRAPPER="${INSTALL_DIR}/mvn-deps-generator"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET_DIR="${SCRIPT_DIR}/../target"

JAR_SRC="$(find "${TARGET_DIR}" -maxdepth 1 -name "mvn-deps-generator-*.jar" ! -name "*original*" | head -1)"

if [[ -z "${JAR_SRC}" ]]; then
  echo "Error: no mvn-deps-generator JAR found in ${TARGET_DIR}"
  echo "Run 'mvn clean package' first."
  exit 1
fi

JAR_NAME="$(basename "${JAR_SRC}")"

mkdir -p "${INSTALL_DIR}"

cp "${JAR_SRC}" "${INSTALL_DIR}/${JAR_NAME}"

cat > "${WRAPPER}" <<EOF
#!/usr/bin/env bash
exec java -jar "${INSTALL_DIR}/${JAR_NAME}" "\$@"
EOF

chmod +x "${WRAPPER}"

echo "Installed mvn-deps-generator to ${WRAPPER}"

if [[ ":${PATH}:" != *":${INSTALL_DIR}:"* ]]; then
  echo "Note: ${INSTALL_DIR} is not in your PATH."
  echo "Add the following to your shell profile:"
  echo "  export PATH=\"${INSTALL_DIR/#$HOME/\$HOME}:\${PATH}\""
fi

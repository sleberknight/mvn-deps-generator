#!/usr/bin/env bash

set -euo pipefail

usage() {
  echo "Usage: $(basename "$0") [-h] [-n] [-u]"
  echo ""
  echo "Installs mvn-deps-generator to INSTALL_DIR (default: \${HOME}/.local/bin)."
  echo "Builds the project automatically before installing."
  echo ""
  echo "Options:"
  echo "  -h    Show this help message and exit"
  echo "  -n    Build and install the GraalVM native binary instead of the JAR wrapper"
  echo "  -u    Uninstall (removes the wrapper/binary and any installed JAR)"
  echo ""
  echo "Environment variables:"
  echo "  INSTALL_DIR   Directory to install into (e.g. INSTALL_DIR=/usr/local/bin ./install.sh)"
}

NATIVE=false
UNINSTALL=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    -h|--help) usage; exit 0 ;;
    -n|--native) NATIVE=true; shift ;;
    -u|--uninstall) UNINSTALL=true; shift ;;
    *) echo "Unknown option: $1"; usage; exit 1 ;;
  esac
done

INSTALL_DIR="${INSTALL_DIR:-${HOME}/.local/bin}"
WRAPPER="${INSTALL_DIR}/mvn-deps-generator"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="${SCRIPT_DIR}/.."
TARGET_DIR="${PROJECT_DIR}/target"

if [[ "${UNINSTALL}" == true ]]; then
  removed=false

  while IFS= read -r file; do
    rm "${file}"
    echo "Removed ${file}"
    removed=true
  done < <(find "${INSTALL_DIR}" -maxdepth 1 -name "mvn-deps-generator*" 2>/dev/null)

  if [[ "${removed}" == false ]]; then
    echo "Nothing to uninstall in ${INSTALL_DIR}"
  fi

  exit 0
fi

# Ensure a suitable Java 25 is active for the build.
# For native builds, GraalVM is required (it provides native-image).
# Falls back to searching SDKMAN candidates if the current JDK isn't Java 25.
ensure_java25() {
  local current_major
  current_major=$(java -version 2>&1 | grep -oE '"[0-9]+' | grep -oE '[0-9]+')

  local sdkman_java="${HOME}/.sdkman/candidates/java"

  if [[ "${NATIVE}" == true ]]; then
    if [[ "${current_major}" == "25" && -x "${JAVA_HOME:-}/bin/native-image" ]]; then
      return 0
    fi
    local graal_home
    graal_home=$(find "${sdkman_java}" -maxdepth 1 -name "25*-graal" -type d 2>/dev/null | sort -r | head -1)
    if [[ -z "${graal_home}" ]]; then
      echo "Error: GraalVM 25 is required for native builds but was not found."
      echo "Install GraalVM 25 and ensure it is on your PATH (e.g. via SDKMAN: sdk install java 25.0.3-graal)."
      exit 1
    fi
    export JAVA_HOME="${graal_home}"
  else
    if [[ "${current_major}" == "25" ]]; then
      return 0
    fi
    local java25_home
    java25_home=$(find "${sdkman_java}" -maxdepth 1 -name "25*" -type d 2>/dev/null | sort -r | head -1)
    if [[ -z "${java25_home}" ]]; then
      echo "Error: Java 25 is required but was not found."
      echo "Install Java 25 and ensure it is on your PATH (e.g. via SDKMAN: sdk install java 25.0.3-graal)."
      exit 1
    fi
    export JAVA_HOME="${java25_home}"
  fi

  export PATH="${JAVA_HOME}/bin:${PATH}"
  echo "Using Java from: ${JAVA_HOME}"
}

ensure_java25

mkdir -p "${INSTALL_DIR}"

if [[ "${NATIVE}" == true ]]; then
  echo "Building native binary..."
  mvn -f "${PROJECT_DIR}/pom.xml" -Pnative clean package

  cp "${TARGET_DIR}/mvn-deps-generator" "${WRAPPER}"
  chmod +x "${WRAPPER}"
else
  echo "Building JAR..."
  mvn -f "${PROJECT_DIR}/pom.xml" clean package

  JAR_SRC="$(find "${TARGET_DIR}" -maxdepth 1 -name "mvn-deps-generator-*.jar" ! -name "*original*" | head -1)"
  JAR_NAME="$(basename "${JAR_SRC}")"

  cp "${JAR_SRC}" "${INSTALL_DIR}/${JAR_NAME}"

  cat > "${WRAPPER}" <<EOF
#!/usr/bin/env bash
java_major=\$(java -version 2>&1 | grep -oE '"[0-9]+' | grep -oE '[0-9]+')
if [[ "\${java_major}" -lt 25 ]]; then
  echo "Error: mvn-deps-generator requires Java 25 or later (found Java \${java_major})."
  echo "Switch to Java 25 before running (e.g. via SDKMAN: sdk use java 25.0.3-graal)."
  exit 1
fi
exec java -jar "${INSTALL_DIR}/${JAR_NAME}" "\$@"
EOF

  chmod +x "${WRAPPER}"
fi

echo "Installed mvn-deps-generator to ${WRAPPER}"

if [[ ":${PATH}:" != *":${INSTALL_DIR}:"* ]]; then
  echo "Note: ${INSTALL_DIR} is not in your PATH."
  echo "Add the following to your shell profile:"
  echo "  export PATH=\"${INSTALL_DIR/#$HOME/\$HOME}:\${PATH}\""
fi

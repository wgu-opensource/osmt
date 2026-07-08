#!/usr/bin/env bash
#
# Test Credential Engine sync by publishing a single mock skill.
# Uses the same env vars as the OSMT sync: CREDENTIAL_ENGINE_API_KEY,
# CREDENTIAL_ENGINE_ORG_CTID, CREDENTIAL_ENGINE_REGISTRY_URL.
#
# Usage: ./bin/test-credential-engine-sync.sh
#        CREDENTIAL_ENGINE_API_KEY=xxx CREDENTIAL_ENGINE_ORG_CTID=ce-xxx ./bin/test-credential-engine-sync.sh
#
# Optional: BASE_URL for ExactAlignment (default: https://example.org)
# Optional: --app-like  Mimic app minimal payload (works with CE)
# Optional: --full      Full payload: Author, CompetencyCategory, "Curriculum & Instruction" (reproduces staging failure)
#
set -euo pipefail

APP_LIKE=false
FULL=false
for arg in "$@"; do
  if [[ "$arg" == "--app-like" ]]; then
    APP_LIKE=true
  elif [[ "$arg" == "--full" ]]; then
    FULL=true
  fi
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo_err() { echo "$*" >&2; }
exit_with_help() {
  echo_err ""
  echo_err "Required environment variables:"
  echo_err "  CREDENTIAL_ENGINE_API_KEY   - API key from Credential Engine"
  echo_err "  CREDENTIAL_ENGINE_ORG_CTID  - Your organization CTID (e.g. ce-...)"
  echo_err ""
  echo_err "Optional:"
  echo_err "  CREDENTIAL_ENGINE_REGISTRY_URL - Default: https://sandbox.credentialengine.org"
  echo_err "  BASE_URL                   - For ExactAlignment (default: https://example.org)"
  echo_err "                             - Use https://osmt.staging.prettygoodskills.com for staging"
  echo_err "  --app-like                 - Mimic app minimal payload (works with CE)"
  echo_err "  --full                     - Full payload: Author, CompetencyCategory, & in keywords (reproduces staging failure)"
  echo_err ""
  echo_err "You can set these in your shell or in an env file."
  echo_err "Env files (if present) are sourced in order:"
  echo_err "  - ${PROJECT_DIR}/api/osmt-dev-stack.env"
  echo_err "  - ${PROJECT_DIR}/api/osmt-staging.env"
  echo_err ""
  echo_err "Example:"
  echo_err "  set -a; source api/osmt-staging.env; set +a"
  echo_err "  ./bin/test-credential-engine-sync.sh"
  echo_err ""
  exit 1
}

# Source env files to load CE vars (explicit env vars take precedence)
saved_api_key="${CREDENTIAL_ENGINE_API_KEY:-}"
saved_org_ctid="${CREDENTIAL_ENGINE_ORG_CTID:-}"
for f in "${PROJECT_DIR}/api/osmt-dev-stack.env" "${PROJECT_DIR}/api/osmt-staging.env"; do
  if [[ -f "$f" && -r "$f" ]]; then
    echo "Sourcing ${f}..."
    set -a
    # shellcheck source=/dev/null
    source "$f"
    set +a
  fi
done
[[ -n "$saved_api_key" ]] && CREDENTIAL_ENGINE_API_KEY="$saved_api_key"
[[ -n "$saved_org_ctid" ]] && CREDENTIAL_ENGINE_ORG_CTID="$saved_org_ctid"

API_KEY="${CREDENTIAL_ENGINE_API_KEY:-}"
ORG_CTID="${CREDENTIAL_ENGINE_ORG_CTID:-}"
REGISTRY_URL="${CREDENTIAL_ENGINE_REGISTRY_URL:-https://sandbox.credentialengine.org}"
BASE_URL="${BASE_URL:-https://example.org}"

if [[ -z "$API_KEY" ]]; then
  echo_err "ERROR: CREDENTIAL_ENGINE_API_KEY is not set."
  exit_with_help
fi

if [[ -z "$ORG_CTID" ]]; then
  echo_err "ERROR: CREDENTIAL_ENGINE_ORG_CTID is not set."
  exit_with_help
fi

if ! command -v curl >/dev/null 2>&1; then
  echo_err "ERROR: curl is required but not found. Please install curl."
  exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
  echo_err "ERROR: jq is required for safe JSON construction."
  echo_err "Install: brew install jq (macOS) or apt install jq (Linux)"
  exit 1
fi

# Fixed UUID so repeated runs update the same competency
# Use different UUID for app-like/full to avoid overwriting OsmtTestSkill
SKILL_UUID="$([ "$APP_LIKE" = true ] || [ "$FULL" = true ] && echo "a0000000-0000-4000-a000-000000000002" || echo "a0000000-0000-4000-a000-000000000001")"

CTID="ce-${SKILL_UUID}"
PUBLISH_URL="${REGISTRY_URL%/}/assistant/competency/publish"
CANONICAL_URL="${BASE_URL%/}/api/skills/${SKILL_UUID}"

echo "Publishing mock skill to Credential Engine..."
echo "  Registry:  ${REGISTRY_URL}"
echo "  CTID:      ${CTID}"
echo "  Org:       ${ORG_CTID}"
echo "  Mode:      $([ "$FULL" = true ] && echo 'full (Author, Category, & in keywords)' || [ "$APP_LIKE" = true ] && echo 'app-like (minimal)' || echo 'minimal')"
echo "  ExactAlign: ${CANONICAL_URL}"
echo ""

if [[ "$FULL" == true ]]; then
  # Full payload: Author, CompetencyCategory, "Curriculum & Instruction" (unstaged app; should fail)
  PAYLOAD=$(jq -n \
    --arg ctid "$CTID" \
    --arg canonical "$CANONICAL_URL" \
    --arg org "$ORG_CTID" \
    '{
      Competencies: [
        {
          CTID: $ctid,
          CompetencyText: "Conduct frequent self-evaluations to reflect on needs for professional practice.",
          CompetencyLabel: "Conduct Self-Evaluations",
          Creator: [$org],
          ConceptKeyword: ["Curriculum & Instruction", "Reflective Practice", "WGUSID: 1212.1"],
          PublicationStatusType: "Published",
          Author: "Western Governors University",
          CompetencyCategory: ["Reflective Practice"],
          ExactAlignment: [$canonical]
        }
      ],
      PublishForOrganizationIdentifier: $org,
      DefaultLanguage: "en-US"
    }')
elif [[ "$APP_LIKE" == true ]]; then
  # Mimic app minimal payload: label prefix, ConceptKeyword shape, no Author/Category (works)
  PAYLOAD=$(jq -n \
    --arg ctid "$CTID" \
    --arg canonical "$CANONICAL_URL" \
    --arg org "$ORG_CTID" \
    '{
      Competencies: [
        {
          CTID: $ctid,
          CompetencyText: "Conduct frequent self-evaluations to reflect on needs for professional practice.",
          CompetencyLabel: "(osmt-dev) Conduct Self-Evaluations",
          Creator: [$org],
          ConceptKeyword: ["Curriculum and Instruction", "Reflective Practice", "WGUSID: 1212.1"],
          PublicationStatusType: "Published",
          ExactAlignment: [$canonical]
        }
      ],
      PublishForOrganizationIdentifier: $org,
      DefaultLanguage: "en-US"
    }')
else
  PAYLOAD=$(jq -n \
    --arg ctid "$CTID" \
    --arg canonical "$CANONICAL_URL" \
    --arg org "$ORG_CTID" \
    '{
      Competencies: [
        {
          CTID: $ctid,
          CompetencyText: "OsmtTestSkill. Mock skill for Credential Engine connectivity test.",
          CompetencyLabel: "OsmtTestSkill",
          Creator: [$org],
          ConceptKeyword: ["OsmtTestSkill", "osmt-test", "test-sync"],
          PublicationStatusType: "Published",
          ExactAlignment: [$canonical]
        }
      ],
      PublishForOrganizationIdentifier: $org,
      DefaultLanguage: "en-US"
    }')
fi

HTTP_CODE=$(curl -sS -w "%{http_code}" -o /tmp/ce-test-response.$$.json \
  -X POST "$PUBLISH_URL" \
  -H "Content-Type: application/json" \
  -H "Authorization: ApiToken ${API_KEY}" \
  -d "$PAYLOAD" 2>/dev/null) || HTTP_CODE="000"

if [[ -z "${HTTP_CODE:-}" || "$HTTP_CODE" == "000" ]]; then
  echo_err "Connection to Credential Engine failed. Check registry URL and network."
  rm -f /tmp/ce-test-response.$$.json
  exit 1
fi

RESPONSE=""
if [[ -f /tmp/ce-test-response.$$.json ]]; then
  RESPONSE=$(cat /tmp/ce-test-response.$$.json)
  rm -f /tmp/ce-test-response.$$.json
fi

# CE returns HTTP 200 even on logical failures; check Successful field
CE_SUCCESS="true"
if [[ -n "$RESPONSE" ]]; then
  CE_SUCCESS=$(echo "$RESPONSE" \
    | jq -r '
        if type == "array" then .[0].Successful
        elif type == "object" then .Successful
        else true end' 2>/dev/null) || CE_SUCCESS="true"
fi

if [[ "$HTTP_CODE" =~ ^2 && "$CE_SUCCESS" != "false" ]]; then
  LABEL="$([ "$FULL" = true ] && echo "Conduct Self-Evaluations" || [ "$APP_LIKE" = true ] && echo "(osmt-dev) Conduct Self-Evaluations" || echo "OsmtTestSkill")"
  FINDER_URL=$(echo "$RESPONSE" \
    | jq -r '
        if type == "array" then .[0].CredentialFinderUrl
        else .CredentialFinderUrl end' 2>/dev/null) || FINDER_URL=""
  GRAPH_URL=$(echo "$RESPONSE" \
    | jq -r '
        if type == "array" then .[0].GraphUrl
        else .GraphUrl end' 2>/dev/null) || GRAPH_URL=""
  SEARCH_URL="${REGISTRY_URL%/}/finder/search?keywords=$([ "$APP_LIKE" = true ] || [ "$FULL" = true ] && echo "Conduct" || echo "OsmtTestSkill")&searchType=competency"
  echo "SUCCESS: Mock skill published. (HTTP $HTTP_CODE)"
  echo "  Label:  ${LABEL:-OsmtTestSkill}"
  echo "  CTID:   ${CTID}"
  echo ""
  [[ -n "$FINDER_URL" && "$FINDER_URL" != "null" ]] \
    && echo "Finder URL: ${FINDER_URL}"
  [[ -n "$GRAPH_URL" && "$GRAPH_URL" != "null" ]] \
    && echo "Graph URL:  ${GRAPH_URL}"
  echo "Search:     ${SEARCH_URL}"
  echo ""
  echo "Note: indexing can take several minutes before the skill appears in search."
else
  echo_err "FAILED: Credential Engine publish was not successful."
  echo_err "  HTTP status: $HTTP_CODE"
  if [[ -n "$RESPONSE" ]]; then
    echo_err ""
    echo_err "Response:"
    echo "$RESPONSE" | jq . 2>/dev/null || echo "$RESPONSE" >&2
  fi
  echo_err ""
  echo_err "Troubleshooting:"
  echo_err "  - Verify org CTID is approved: https://apps.credentialengine.org/accounts"
  echo_err "  - API docs: https://credreg.net/registry/assistant"
  exit 1
fi

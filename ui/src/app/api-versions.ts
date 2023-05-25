import { environment } from "../environments/environment"

export function getBaseApi(): string {
  const version = getApiVersion()
  return "/api" + (version.length > 0 ? "/" : "") + version
}

function getApiVersion(): string {
  return environment.apiV2
}

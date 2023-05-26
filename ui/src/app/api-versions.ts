import { environment } from "../environments/environment"

export enum ApiVersion {
  /**
   * @deprecated
   */
  API_V2 = "",
  API_V3 = "v3"
}

export function getBaseApi(version: ApiVersion = ApiVersion.API_V3): string {
  return "/api" + (version.length > 0 ? "/" : "") + version
}

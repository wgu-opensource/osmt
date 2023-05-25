import { environment } from "../environments/environment"

export function getBaseApi(): string {
  return "/api/" + environment.apiV2
}

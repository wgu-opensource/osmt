import {ButtonAction} from "./auth-roles"

export interface IAuthService {
  // Initialize auth service synchronously.  Must be called in main thread and not via async mechanism.
  init(): void

  // Finish initial setup of auth service.  Can be done lazily.
  setup(): void

  // Start the authentication process.
  start(returnPath: string): void

  // Store the token once it is received.
  storeToken(accessToken: string): void

  // Save where to return to.
  storeReturn(returnRoute: string): void

  // Return to previously saved location, but do so asynchronously.
  restoreReturnAsync(): void

  // Return to previously saved location and clear the saved value.
  popReturn(): string | null

  // Initiate the logout sequence.
  logout(): void

  // Get the stored auth token.
  currentAuthToken(): string | null

  // Check if we have a token.
  isAuthenticated(): boolean

  // Indicate whether the backend is down.
  setServerIsDown(isDown: boolean): void

  // Get the user's roles.
  getRole(): string

  hasRole(requiredRoles: string[], userRoles: string[]): boolean

  isEnabledByRoles(buttonAction: ButtonAction): boolean
}

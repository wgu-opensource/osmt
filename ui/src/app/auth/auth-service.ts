export const STORAGE_KEY_TOKEN = "OSMT.AuthService.accessToken"
export const STORAGE_KEY_RETURN = "OSMT.AuthService.return"

export class AuthService {
  constructor() {
  }


  storeToken(accessToken: string): void {
    localStorage.setItem(STORAGE_KEY_TOKEN, accessToken)
  }

  storeReturn(returnRoute: string): void {
    localStorage.setItem(STORAGE_KEY_RETURN, returnRoute)
  }

  popReturn(): string | null {
    const ret = localStorage.getItem(STORAGE_KEY_RETURN)
    localStorage.removeItem(STORAGE_KEY_RETURN)
    return ret
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY_TOKEN)
  }

  currentAuthToken(): string | null {
    return localStorage.getItem(STORAGE_KEY_TOKEN)
  }

  isAuthenticated(): boolean {
    return this.currentAuthToken() !== null
  }
}

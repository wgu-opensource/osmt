export const STORAGE_KEY_TOKEN = "OSMT.AuthService.accessToken"

export class AuthService {
  constructor() {
  }


  storeToken(accessToken: string): void {
    localStorage.setItem(STORAGE_KEY_TOKEN, accessToken)
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

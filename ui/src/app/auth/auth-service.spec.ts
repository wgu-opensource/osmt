import { AuthService } from "./auth-service"

describe("AuthService", () => {
  // @ts-ignore
  const authService = new AuthService(null, null, null)

  beforeEach(() => {
  })
  it("should be created", () => {
    expect(authService).toBeTruthy()
  })

  it("should return true with correct roles", () => {
    const requiredRoles: string[] = ["data", "curator"]
    const userRoles: string[] = ["curator", "viewer"]
    expect(authService.hasRole(requiredRoles, userRoles)).toEqual(true)
  })

  it("should return false with incorrect roles", () => {
    const requiredRoles: string[] = ["data", "curator"]
    const userRoles: string[] = ["guest", "viewer"]
    expect(authService.hasRole(requiredRoles, userRoles)).toEqual(false)
  })
})

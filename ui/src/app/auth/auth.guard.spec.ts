import { TestBed, getTestBed } from "@angular/core/testing"
import { AuthGuard } from "./auth.guard"
import { AuthService } from "./auth-service"
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot} from "@angular/router"
import { HttpClientTestingModule } from "@angular/common/http/testing"
import { AuthServiceData, AuthServiceStub, RouterStub } from "../../../test/resource/mock-stubs"

describe("AuthGuard", () => {
  let injector: TestBed
  let authGuard: AuthGuard
  const routeMock = Object.assign({}, ActivatedRouteSnapshot.prototype, { data: {roles: "ADMIN"} })
  const stateMock = Object.assign({}, RouterStateSnapshot.prototype, { url: "/cookies" } )

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: Router, useClass: RouterStub }
      ],
      imports: [HttpClientTestingModule]
    })
    injector = getTestBed()
    // authService = injector.get(AuthService)
    authGuard = injector.get(AuthGuard)
    AuthServiceData.authenticatedFlag = true
    AuthServiceData.hasRoleFlag = true
  })

  it("should be created", () => {
    // Act and Assert
    expect(authGuard).toBeTruthy()
  })

  it("should return true", () => {
    // Act and Assert
    expect(authGuard.canActivate(routeMock, stateMock)).toEqual(true)
  })

  it("should return false when when authService is NOT authenticated", () => {
    // Arrange
    AuthServiceData.authenticatedFlag = false
    // Act and Assert
    expect(authGuard.canActivate(routeMock, stateMock)).toEqual(false)
  })

  it("should return false without appropriate roles", () => {
    // Arrange
    AuthServiceData.hasRoleFlag = false
    // Act and Assert
    expect(authGuard.canActivate(routeMock, stateMock)).toEqual(false)
  })

  it("should return true with undefined route.data.roles", () => {
    // Arrange
    const routeWithUndefinedRoles = Object.assign({}, ActivatedRouteSnapshot.prototype, {
      data: {roles: undefined}
    })
    // Act and Assert
    expect(authGuard.canActivate(routeWithUndefinedRoles, stateMock)).toEqual(true)
  })
})

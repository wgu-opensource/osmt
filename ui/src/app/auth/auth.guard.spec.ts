import { TestBed, getTestBed } from "@angular/core/testing"
import { AuthGuard } from "./auth.guard"
import { AuthService } from "./auth-service"
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot} from "@angular/router"
import { HttpClientTestingModule } from "@angular/common/http/testing"
import { AuthServiceStub, RouterStub } from "../../../test/resource/mock-stubs"
import { OSMT_ADMIN, OSMT_CURATOR } from "./auth-roles"


describe("AuthGuard", () => {
  let injector: TestBed
  let authService: AuthService
  let authGuard: AuthGuard
  const routeStateMock = Object.assign({}, RouterStateSnapshot.prototype, {
      url: "/cookies"
    }
  )

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
    authService = injector.get(AuthService)
    authGuard = injector.get(AuthGuard)
  })

  it("should be created", () => {
    // Act and Assert
    expect(authGuard).toBeTruthy()
  })

  it("should return true", () => {
    // Arrange
    const route = Object.assign({}, ActivatedRouteSnapshot.prototype, {
      data: {roles: [OSMT_ADMIN, OSMT_CURATOR]}
    })

    // Act and Assert
    expect(authGuard.canActivate(route, routeStateMock)).toEqual(true)
  })

  it("should return false", () => {
    // Arrange
    const route = Object.assign({}, ActivatedRouteSnapshot.prototype, {
      data: {roles: "WRONG_ROLE"}
    })

    // Act and Assert
    expect(authGuard.canActivate(route, routeStateMock)).toEqual(false)
  })

  it("should return true without role needed", () => {
    // Arrange
    const route = Object.assign({}, ActivatedRouteSnapshot.prototype, {
      data: {roles: ""}
    })

    // Act and Assert
    expect(authGuard.canActivate(route, routeStateMock)).toEqual(true)
  })
})

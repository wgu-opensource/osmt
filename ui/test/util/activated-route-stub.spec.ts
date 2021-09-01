import { convertToParamMap, ParamMap, Params } from "@angular/router"
import { Observable, ReplaySubject } from "rxjs"
import SpyObj = jasmine.SpyObj


/**
 * An ActivateRoute test double with a `paramMap` observable.
 * Use the `setParamMap()` method to add the next `paramMap` value.
 */
export class ActivatedRouteStubSpec {
  // Use a ReplaySubject to share previous values with subscribers
  // and pump new values into the `paramMap` observable
  private paramMap$ = new ReplaySubject<ParamMap>()
  private params$ = new ReplaySubject<Params>()
  private queryParams$ = new ReplaySubject<Params>()

  private testParams: Params = {}

  // tslint:disable-next-line:no-any
  static createRouterSpy(): any {
    return jasmine.createSpyObj("Router", ["navigate"])
  }


  constructor(initialParams?: Params) {
    this.setParamMap(initialParams)
  }

  /** Set the paramMap observables's next value */
  setParamMap(params?: Params): void {
    if (params) {
      this.params$.next(this.testParams = params)
      this.paramMap$.next(convertToParamMap(params))
    }
  }

  get params(): Observable<Params> {
    return this.params$.asObservable()
  }

  get queryParams(): Observable<Params> {
    return this.queryParams$.asObservable()
  }

  get snapshot(): object {
    return {
      params: this.testParams,
      paramMap: convertToParamMap(this.testParams)
    }
  }
}

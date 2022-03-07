import { NavigationEnd } from "@angular/router"
import { Observable } from "rxjs"


/**
 * An ActivateRoute test double with a `paramMap` observable.
 * Use the `setParamMap()` method to add the next `paramMap` value.
 */
export class RouterStubSpec {
  private ne: NavigationEnd | undefined

  setNavigationEnd(ne: NavigationEnd): void {
    this.ne = ne
  }

  get events(): Observable<NavigationEnd> {
    return new Observable(observer => {
      observer.next(this.ne)
      observer.complete()
    })
  }
}

import {FormGroup} from "@angular/forms";
import {Injectable} from "@angular/core";
import {ActivatedRouteSnapshot, CanDeactivate, RouterStateSnapshot} from "@angular/router";
import {Observable} from "rxjs";

export interface HasFormGroup {
  formGroup(): FormGroup
}

@Injectable()
export class FormDirtyGuard implements CanDeactivate<HasFormGroup> {

  canDeactivate(
    component: HasFormGroup,
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | boolean {
    const formGroup = component.formGroup()
    if (!formGroup.pristine) {
      return new Observable((observer) => {
        observer.next(confirm("Whoa, there! You have unsaved changes.\nIf you leave this page without saving, you'll lose your edits. Are you sure you want to leave?"))
        observer.complete()
      })
    }
    return true
  }
}

import {AbstractControl, ValidationErrors, ValidatorFn} from "@angular/forms";

export function notACopyValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.value) { return null }

  if (control.value.startsWith("Copy of"))  {
    return {notACopy: {value: control.value}}
  }

  return null
}



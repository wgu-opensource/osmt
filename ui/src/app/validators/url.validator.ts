import {AbstractControl, ValidationErrors, ValidatorFn} from "@angular/forms";

export function urlValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.value) { return null }

  try {
    const v = new URL(control.value)
    return null
  } catch (e: TypeError) { }

  return {invalidUrl: {value: control.value}}
}



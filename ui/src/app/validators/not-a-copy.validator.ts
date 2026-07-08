import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { isCopyName } from '../core/duplicate-name.utils';

export function notACopyValidator(
  control: AbstractControl
): ValidationErrors | null {
  if (!control.value) {
    return null;
  }

  if (isCopyName(control.value)) {
    return { notACopy: { value: control.value } };
  }

  return null;
}

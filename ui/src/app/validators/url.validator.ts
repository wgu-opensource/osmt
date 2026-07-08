import { AbstractControl, ValidationErrors } from '@angular/forms';

export function urlValidator(
  control: AbstractControl
): ValidationErrors | null {
  if (!control.value) {
    return null;
  }

  try {
    const v = new URL(control.value);
    return null;
    // eslint-disable-next-line no-empty -- legacy
  } catch (e) {}

  return { invalidUrl: { value: control.value } };
}

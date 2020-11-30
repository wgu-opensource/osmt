import {formatDate} from "@angular/common";

export function dateformat(date?: Date, locale?: string): string {
  return (date) ? formatDate(date, "MMM dd yyyy", locale || navigator.language || navigator.languages[0]) : ""
}

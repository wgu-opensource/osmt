import { DebugElement } from "@angular/core"

// Various testing helpers


/**
 * Button events to pass to `DebugElement.triggerEventHandler` for RouterLink event handler
 */
export const ButtonClickEvents = {
   left:  { button: 0 },
   right: { button: 2 }
}


/**
 * Simulate an element click.
 * It defaults to mouse left-button click event.
 */
// tslint:disable-next-line:no-any
export function click(el: DebugElement | HTMLElement, eventObj: any = ButtonClickEvents.left): void {
  if (el instanceof HTMLElement) {
    el.click()
  } else {
    el.triggerEventHandler("click", eventObj)
  }
}

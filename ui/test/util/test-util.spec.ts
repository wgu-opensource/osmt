import { DebugElement, Type } from "@angular/core"
import { TestBed } from "@angular/core/testing"

// Various testing helpers

export async function createComponent(T: Type<any>, f?: (component: any) => void): Promise<any> {
    const fixture = TestBed.createComponent(T)
    const component = fixture.componentInstance

    if (f) {
      f(component)
    }

    // 1st change detection triggers ngOnInit
    fixture.detectChanges()

    await fixture.whenStable().then(() => {
      // 2nd change detection
      fixture.detectChanges()
    })

    return Promise.resolve(component)
}

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

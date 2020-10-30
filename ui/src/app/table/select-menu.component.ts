import {Component, Directive, ElementRef, Input, NgZone, Renderer2, ViewChild} from "@angular/core"
import {HasActionDefinitions} from "./has-action-definitions";
import Popper from "popper.js";

@Component({
  selector: "app-select-menu",
  templateUrl: "./select-menu.component.html"
})
export class SelectMenuComponent extends HasActionDefinitions {
  @ViewChild("container") container?: ElementRef

  @Input() title?: string
  isOpen: boolean = false

  @Input()
  closeOnOutsideClick = true

  @Input()
  closeOnInsideClick = true

  get componentElem(): HTMLElement { return this.componentElemRef.nativeElement as HTMLElement }

  private popper?: Popper
  private windowClickListener?: () => void
  private lastTriggerElem?: HTMLElement

  constructor(private componentElemRef: ElementRef,
              private renderer: Renderer2,
              private ngZone: NgZone) {
    super()
  }

  get popperOptions(): any {
    const offset = [20, 20]
    return {
      placement: "bottom-end",
      modifiers: [
        {
          name: "offset",
          options: {
            offset,
          }
        },
        {
          name: "flip",
          options: {
            fallbackPlacements: ["top-end"],
          }
        }
      ]

    }
  }

  toggle(triggerElem: HTMLElement): void {
    if (!this.isOpen || this.lastTriggerElem !== triggerElem) {
      if (this.isOpen) this.close();
      this.open(triggerElem);
    } else if (this.isOpen) {
      this.close();
    }

  }

  open(triggerElem: HTMLElement): void {
    this.lastTriggerElem = triggerElem;

    if (!this.popper) {
      this.ngZone.runOutsideAngular(() => {
        this.popper = new Popper(triggerElem, this.componentElem, this.popperOptions)
      })
      this.isOpen = true

      this.windowClickListener = this.renderer.listen(
        "window",
        "click",
        (event) => this.handleWindowClick(event)
      )
    }
  }

  close(): void {
    if (this.popper) {
      this.cleanup()
      this.popper = undefined
      this.isOpen = false
    }
  }

  cleanup(): void {
    this.popper?.destroy()
    if (this.windowClickListener !== undefined) {
      this.windowClickListener()
      this.windowClickListener = undefined
    }
  }


  handleWindowClick(event: MouseEvent): void {
    if (this.componentElem) {

      if (this.lastTriggerElem && this.lastTriggerElem.contains(event.target as Node)) {
        return
      }

      if (this.componentElem.contains(event.target as Node)) {
        if (this.closeOnInsideClick) {
          this.close()
        }
      } else if (this.closeOnOutsideClick) {
        this.close()
      }
    }
  }
}


@Directive({
  selector: "[appSelectMenuTrigger]",
  host: {
    "(click)": "handleClick()"
  }
})
export class SelectMenuTriggerDirective {
  @Input("appSelectMenuTrigger")
  private menu?: SelectMenuComponent

  constructor(private componentElemRef: ElementRef)
  {
  }

  handleClick(): boolean {
    if (this.menu) {
      this.menu.toggle(this.componentElemRef.nativeElement as HTMLElement)
    }
    this.componentElemRef.nativeElement.setAttribute("aria-expanded", this.menu?.isOpen ? "true" : "false")
    return false
  }
}

import {AfterViewInit, Component, ElementRef, Input, NgZone, OnInit, Renderer2, ViewChild} from "@angular/core";
import {HasActionDefinitions, TableActionDefinition} from "./has-action-definitions";
import {SvgHelper, SvgIcon} from "../core/SvgHelper";
import {createPopper, Instance} from "@popperjs/core"
import {ClickService} from "../core/click.service";
import {Subscription} from "rxjs";

@Component({
  selector: "app-dots-menu",
  templateUrl: "./dots-menu.component.html"
})
export class DotsMenuComponent extends HasActionDefinitions implements AfterViewInit {
  @ViewChild("triggerButton") triggerButton!: ElementRef
  @ViewChild("actionMenu") actionMenu!: ElementRef

  @Input() title?: string

  @Input() data?: any

  @Input() closeOnOutsideClick = true

  @Input() closeOnInsideClick = true

  private popper?: Instance
  isOpen: boolean = false

  moreIcon = SvgHelper.path(SvgIcon.MORE)
  private windowClickSubscription?: Subscription;

  constructor(private componentElemRef: ElementRef,
              private renderer: Renderer2,
              private ngZone: NgZone,
              private clickService: ClickService) {
    super()
  }

  get popperOptions(): any {
    const offset = [0, 8]
    return {
      placement: "bottom-end",
      modifiers: [
        {
          name: "offset",
          options: {
            offset: offset
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

  ngAfterViewInit(): void {
      this.ngZone.runOutsideAngular(() => {
        this.popper = createPopper(this.triggerButton.nativeElement, this.actionMenu.nativeElement, this.popperOptions)
      })
  }

  handleClickTrigger(): boolean {
    this.toggle()
    return false
  }

  handleClickAction(action: TableActionDefinition): boolean {
    if (this.closeOnInsideClick) {
      this.close()
    }
    this.clickAction(action, this.data)
    return false
  }

  close(): void {
    this.isOpen = false
    this.windowClickSubscription?.unsubscribe()
  }
  open(): void {
    this.isOpen = true

    this.windowClickSubscription = this.clickService.subscribeNextClick(this.triggerButton, () => {
      this.close()
    })
  }
  toggle(): void {
    if (this.isOpen) {
      this.close()
    } else {
      this.open()
    }
  }
}

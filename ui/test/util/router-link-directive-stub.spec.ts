/* tslint:disable:no-any */
import { Directive, HostListener, Input } from "@angular/core"

@Directive({
  // tslint:disable-next-line:directive-selector
  selector: "[routerLink]"
})
// tslint:disable-next-line:directive-class-suffix
export class RouterLinkDirectiveStub {
  @Input("routerLink") linkParams: any
  navigatedTo: any = null

  @HostListener("click")
  onClick(): void {
    this.navigatedTo = this.linkParams
  }
}

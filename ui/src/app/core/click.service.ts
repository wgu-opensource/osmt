
import {ElementRef, Injectable, Renderer2, RendererFactory2} from "@angular/core";
import {Subject, Subscription} from "rxjs";
import {share} from "rxjs/operators";

@Injectable({
  providedIn: "root"
})
export class ClickService {

  private nextClickSource: Subject<any> = new Subject()
  nextClick$ = this.nextClickSource.asObservable().pipe(share())
  private windowClickListener?: any;
  private renderer: Renderer2;
  private lastTrigger?: ElementRef

  private skipOne: boolean = true

  constructor(protected rendererFactory: RendererFactory2) {
    this.renderer = rendererFactory.createRenderer(null, null)
  }

  subscribeNextClick(triggerButton: ElementRef, next: () => void): Subscription {
    this.lastTrigger = triggerButton
    this.startListening()
    return this.nextClick$.subscribe(next)
  }

  startListening(): void {
    if (this.windowClickListener === undefined) {
      this.windowClickListener = this.renderer.listen(
        "window",
        "click",
        (event) => this.handleWindowClick(event)
      )
    }
  }
  stopListening(): void {
    this.windowClickListener()
    this.windowClickListener = undefined
    this.lastTrigger = undefined
    this.skipOne = true
  }

  handleWindowClick(event: MouseEvent): void {
    if (this.skipOne) { // we immediately get a fired event for the click that spawned us
      this.skipOne = false
      return
    }

    this.nextClickSource.next()
    this.stopListening()
  }

}

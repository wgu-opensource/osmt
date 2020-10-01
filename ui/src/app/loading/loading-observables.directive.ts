import {ComponentFactoryResolver, Directive, Input, TemplateRef, ViewContainerRef} from "@angular/core"
import {Observable, forkJoin} from "rxjs"
import {LoadingComponent} from "./loading.component"

@Directive({
  selector: "[appLoadingObservables]"
})
export class LoadingObservablesDirective {
  constructor(
    private viewContainer: ViewContainerRef,
    private template: TemplateRef<unknown>,
    private componentResolver: ComponentFactoryResolver
  ) {}

  @Input() showLoader = true

  @Input() set appLoadingObservables(observables: Array<Observable<unknown> | null>) {
    if (this.showLoader) {
      this.showLoadingAnimation()
    }

    const filtered = observables.filter(o => !!o)
    if (filtered.length > 0) {
      forkJoin(filtered).subscribe( a =>
        () => {},
        error => { this.showError(error) },
        () => { this.showTemplate() },
      )
    } else {
      this.showTemplate()
    }
  }

  private showTemplate(): void {
    this.viewContainer.clear()
    this.viewContainer.createEmbeddedView(this.template)
  }

  private showError(error: unknown): void {
    this.viewContainer.clear()
    console.log("Loading Error!", error)
  }

  private showLoadingAnimation(): void {
    const factory = this.componentResolver.resolveComponentFactory(LoadingComponent)
    this.viewContainer.clear()
    const componentRef = this.viewContainer.createComponent(factory)
  }

}

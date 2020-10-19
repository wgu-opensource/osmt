import {ComponentFactoryResolver, ComponentRef, Directive, Input, TemplateRef, ViewContainerRef} from "@angular/core"
import {Observable, forkJoin} from "rxjs"
import {LoadingComponent} from "./loading.component"
import {ServerErrorComponent} from "./server-error.component";
import {AuthService} from "../auth/auth-service";

@Directive({
  selector: "[appLoadingObservables]"
})
export class LoadingObservablesDirective {
  constructor(
    private viewContainer: ViewContainerRef,
    private template: TemplateRef<unknown>,
    private componentResolver: ComponentFactoryResolver,
    private authService: AuthService
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

  private showError(error: any): void {
    const status: number = error?.status ?? 500
    console.log("Loading Error!", status, error)
    const factory = this.componentResolver.resolveComponentFactory(ServerErrorComponent)
    this.viewContainer.clear()
    const componentRef: ComponentRef<ServerErrorComponent> = this.viewContainer.createComponent<ServerErrorComponent>(factory)
    componentRef.instance.status = status
    if (status === 0) {
      this.authService.setServerIsDown(true)
    }
  }

  private showLoadingAnimation(): void {
    const factory = this.componentResolver.resolveComponentFactory(LoadingComponent)
    this.viewContainer.clear()
    const componentRef = this.viewContainer.createComponent(factory)
  }

}

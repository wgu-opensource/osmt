import {Component, ComponentRef, Input, OnInit} from "@angular/core"
import {Location} from "@angular/common"
import {forkJoin, Observable} from "rxjs"
import {ServerErrorComponent} from "../loading/server-error.component";
import {AuthService} from "../auth/auth-service";
import {Router} from "@angular/router";

@Component({
  selector: "app-blocking-loader",
  templateUrl: "./blocking-loader.component.html"
})
export class BlockingLoaderComponent implements OnInit {

  @Input() loaderVisible = false

  @Input() message?: string
  get messageVisible(): boolean {
    return this.message !== undefined
  }

  constructor(
    protected authService: AuthService,
    protected location: Location,
    protected router: Router
  ) { }

  ngOnInit(): void {}

  @Input() set observables(obs: Array<Observable<unknown> | undefined>) {
    const filtered = obs.filter(o => !!o)
    if (filtered.length > 0) {
      this.showLoader()

      forkJoin(filtered).subscribe(a => () => {},
        error => { this.showError(error) },
        () => { this.showContents() },
      )
    } else {
      this.showContents()
    }
  }

  private showError(error: any): void {
    const status: number = error?.status ?? 500
    console.log("Loading Error!", status, error)
    if (status === 401) {
      this.authService.logout()
      const returnPath = this.location.path(true)
      this.router.navigate(["/login"], {queryParams: {return: returnPath}})
      return
    }
    else if (status === 0) {
      this.authService.setServerIsDown(true)
    }
  }

  private showContents(): void {
    this.loaderVisible = false
  }
  private showLoader(): void {
    this.loaderVisible = true
  }

}

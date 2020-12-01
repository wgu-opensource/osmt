import {Component, Input, OnInit} from "@angular/core"
import {forkJoin, Observable} from "rxjs"

@Component({
  selector: "app-blocking-loader",
  templateUrl: "./blocking-loader.component.html"
})
export class BlockingLoaderComponent implements OnInit {

  private loaderVisible = true

  @Input() message?: string
  get messageVisible(): boolean {
    return this.message !== undefined
  }

  constructor() { }

  ngOnInit(): void {}

  @Input() set observables(obs: Array<Observable<unknown> | undefined>) {
    const filtered = obs.filter(o => !!o)
    if (filtered.length > 0) {
      this.showLoader()

      forkJoin(filtered).subscribe(a => () => {},
        error => {},
        () => { this.showContents() },
      )
    } else {
      this.showContents()
    }
  }

  private showContents(): void {
    this.loaderVisible = false
  }
  private showLoader(): void {
    this.loaderVisible = true
  }

}

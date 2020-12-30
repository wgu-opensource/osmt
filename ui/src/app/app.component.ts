import {Component, OnInit} from "@angular/core"
import {Title} from "@angular/platform-browser"
import {Whitelabelled} from "../whitelabel"
import {ToastService} from "./toast/toast.service";
import {DEFAULT_INTERRUPTSOURCES, Idle} from "@ng-idle/core";
import {Keepalive} from "@ng-idle/keepalive";
import {Router} from "@angular/router";

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html"
})
export class AppComponent extends Whitelabelled implements OnInit {
  blockingLoaderVisible: boolean = false

  public constructor(private titleService: Title,
                     private toastService: ToastService,
                     private idle: Idle,
                     private keepalive: Keepalive,
                     private router: Router)
  {
    super()

    this.watchForIdle()

    this.toastService.loaderSubject.subscribe(visible =>  {
      this.blockingLoaderVisible = visible
    })
  }

  ngOnInit(): void {
    this.titleService.setTitle(this.whitelabel.toolName)
  }

  watchForIdle(): void {
    this.idle.setIdle(15)
    this.idle.setTimeout(this.whitelabel.idleTimeoutInSeconds - 15)
    this.idle.setInterrupts(DEFAULT_INTERRUPTSOURCES)

    this.idle.onTimeout.subscribe(() => {
      console.log("Idle time out!")
      this.router.navigate(["/logout"], {queryParams: {timeout: true}})
    })
    this.keepalive.interval(15)
    this.idle.watch()
  }
}

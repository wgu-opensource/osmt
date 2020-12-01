import {Component, OnInit} from "@angular/core"
import {Title} from "@angular/platform-browser"
import {Whitelabelled} from "../whitelabel"
import {ToastService} from "./toast/toast.service";

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html"
})
export class AppComponent extends Whitelabelled implements OnInit {
  blockingLoaderVisible: boolean = false

  public constructor(private titleService: Title, private toastService: ToastService) {
    super()

    this.toastService.loaderSubject.subscribe(visible =>  {
      this.blockingLoaderVisible = visible
    })
  }

  ngOnInit(): void {
    this.titleService.setTitle(this.whitelabel.toolName)
  }
}

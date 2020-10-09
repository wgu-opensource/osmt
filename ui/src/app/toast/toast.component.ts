import {Component, OnInit} from "@angular/core"
import {ToastMessage, ToastService} from "./toast.service";

@Component({
  selector: "app-toast",
  templateUrl: "./toast.component.html"
})
export class ToastComponent implements OnInit {
  message?: ToastMessage

  constructor(toastService: ToastService) {
    toastService.subject.subscribe((msg) => {
      this.message = msg
    })
  }

  ngOnInit(): void {
  }

  isVisible(): boolean {
    return this.message !== undefined
  }
  dismiss(): void {
    this.message = undefined
  }

}

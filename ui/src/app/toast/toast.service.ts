import {Subject} from "rxjs"
import {Injectable} from "@angular/core"

export interface ToastMessage {
  title: string
  message: string
  isAttention: boolean
}

@Injectable({
  providedIn: "root"
})
export class ToastService {
  subject = new Subject<ToastMessage>()
  loaderSubject = new Subject<boolean>()

  constructor() { }

  showToast(title: string, message: string, isAttention: boolean = false, autoDismissMs: number | undefined = 5000): void {
    this.subject.next({
      title,
      message,
      isAttention
    })
    if (autoDismissMs !== undefined) {
      setTimeout(() => this.dismiss(), autoDismissMs)
    }
  }

  dismiss(): void {
    this.subject.next(undefined)
  }

  showBlockingLoader(): void {
    this.loaderSubject.next( true)
  }
  hideBlockingLoader(): void {
    this.loaderSubject.next( false)
  }
}

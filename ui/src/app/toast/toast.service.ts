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

  showToast(title: string, message: string, isAttention: boolean = false): void {
    this.subject.next({
      title,
      message,
      isAttention
    })
  }

  showBlockingLoader(): void {
    this.loaderSubject.next( true)
  }
  hideBlockingLoader(): void {
    this.loaderSubject.next( false)
  }
}

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

  constructor() { }

  showToast(title: string, message: string, isAttention: boolean = false): void {
    this.subject.next({
      title,
      message,
      isAttention
    })
  }
}

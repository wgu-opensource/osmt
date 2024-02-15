import { Component, Input } from "@angular/core"

@Component({
  selector: 'app-inline-error',
  templateUrl: './inline-error.component.html'
})
export class InlineErrorComponent {
  @Input() message = ""
}

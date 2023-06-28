import { Component, Input } from "@angular/core"

@Component({
  selector: 'app-inline-heading',
  templateUrl: './inline-heading.component.html'
})
export class InlineHeadingComponent {
  @Input() heading = "Heading:"
}

import {Component, Input} from "@angular/core"

@Component({
  selector: "app-system-message",
  templateUrl: "./system-message.component.html"
})
export class SystemMessageComponent {

  @Input() title = ""
  @Input() body = ""

  constructor()
  {}

}

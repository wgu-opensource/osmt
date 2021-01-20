import {Component, Input} from "@angular/core"
import {Whitelabelled} from "../../whitelabel"
import {Title} from "@angular/platform-browser"


@Component({
  selector: "app-server-error",
  templateUrl: "./server-error.component.html"
})
export class ServerErrorComponent extends Whitelabelled {
  @Input() className = ""
  @Input() status: number = 500

  constructor(protected titleService: Title) {
    super()
    this.titleService.setTitle(`Error | ${this.whitelabel.toolName}`)
  }

}

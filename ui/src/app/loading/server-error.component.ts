import {Component, Input} from '@angular/core';


@Component({
  selector: "app-server-error",
  templateUrl: "./server-error.component.html"
})
export class ServerErrorComponent {
  @Input() className = ""
  @Input() status: number = 500
}

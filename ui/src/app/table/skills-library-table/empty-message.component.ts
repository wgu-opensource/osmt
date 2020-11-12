import {Component, Input, OnInit} from "@angular/core";

@Component({
  selector: "app-empty-message",
  templateUrl: "./empty-message.component.html"
})
export class EmptyMessageComponent implements OnInit {
  @Input() count: number = 0
  @Input() isForm: boolean = false

  constructor() {
  }

  ngOnInit(): void {
  }

}

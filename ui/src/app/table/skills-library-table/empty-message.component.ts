import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-empty-message',
  templateUrl: './empty-message.component.html',
})
export class EmptyMessageComponent implements OnInit {
  @Input() count = 0;
  @Input() isForm = false;

  constructor() {}

  ngOnInit(): void {}
}

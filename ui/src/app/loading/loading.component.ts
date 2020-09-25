import {Component, Input} from '@angular/core';

@Component({
  selector: "app-loading",
  template: `<div class="lds-dual-ring">Loading...</div>`
})
export class LoadingComponent {
  @Input() className = ""
}

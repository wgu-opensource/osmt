import {Component, Input} from "@angular/core"
import {SvgHelper, SvgIcon} from "./SvgHelper"

@Component({
  selector: "app-inline-heading",
  template: `
    <div class="m-inlineHeading">
      <p class="m-inlineHeading-x-heading">{{heading}}</p>
      <p class="m-inlineHeading-x-text"><ng-content></ng-content></p>
    </div>
  `
})
export class InlineHeadingComponent {
  @Input() heading = "Heading:"
}

@Component({
  selector: "app-inline-error",
  template: `
    <p class="m-tableRow-x-message">
      <span class="m-tableRow-x-messageIcon">
        <svg class="t-icon" aria-hidden="true">
          <use [attr.xlink:href]="icon"></use>
        </svg>
      </span>
      <span class="m-tableRow-x-messageText">{{message}}</span>
    </p>`
})
export class InlineErrorComponent {
  @Input() message = ""
  icon = SvgHelper.path(SvgIcon.ERROR)
}

@Component({
  selector: "app-inline-warning",
  template: `
    <p class="m-tableRow-x-message">
      <span class="m-tableRow-x-messageIcon">
        <svg class="t-icon" aria-hidden="true">
          <use [attr.xlink:href]="icon"></use>
        </svg>
      </span>
      <span class="m-tableRow-x-messageText">{{message}}</span>
    </p>`
})
export class InlineWarningComponent {
  @Input() message = ""
  icon = SvgHelper.path(SvgIcon.WARNING)
}

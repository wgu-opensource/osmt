import {Component, Input, OnInit} from "@angular/core";
import {INamedReference} from "../ApiSkill";
import {AuditedImportSkill} from "./batch-import.component";


@Component({
  selector: "app-import-preview-table",
  templateUrl: "./import-preview-table.component.html"
})
export class ImportPreviewTableComponent implements OnInit {

  @Input() skills: AuditedImportSkill[] = []

  ngOnInit(): void {
  }

}



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
  @Input() heading: string = "Heading:"
}

@Component({
  selector: "app-named-references",
  template: `
    <a *ngIf="hasBoth" href="{{ref.id}}" target="_blank">{{ref.name}}</a>
    <a *ngIf="hasOnlyUrl" href="{{ref.id}}" target="_blank">{{ref.id}}</a>
    <span *ngIf="hasOnlyName">{{ref.name}}</span>
  `
})
export class NamedReferenceComponent {
  @Input() ref?: INamedReference

  get hasBoth(): boolean {
    return this.ref?.id !== undefined && this.ref?.name !== undefined
  }
  get hasOnlyUrl(): boolean {
    return this.ref?.id !== undefined && this.ref?.name === undefined
  }
  get hasOnlyName(): boolean {
    return this.ref?.id === undefined && this.ref?.name !== undefined
  }
}


@Component({
  selector: "app-inline-error",
  template: `
    <p class="m-tableRow-x-message">
      <span class="m-tableRow-x-messageIcon">
        <svg class="t-icon" aria-hidden="true">
          <use xlink:href="/assets/images/svg-defs.svg#icon-error"></use>
        </svg>
      </span>
      <span class="m-tableRow-x-messageText">{{message}}</span>
    </p>`
})
export class InlineErrorComponent {
  @Input() message: string = ""
}

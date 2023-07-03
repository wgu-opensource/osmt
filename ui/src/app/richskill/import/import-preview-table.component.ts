import { Component, Input } from "@angular/core";
import {INamedReference} from "../ApiSkill";
import {AuditedImportSkill} from "./batch-import.component";


@Component({
  selector: "app-import-preview-table",
  templateUrl: "./import-preview-table.component.html",
  styleUrls: ["./import-preview-table.component.scss"]
})
export class ImportPreviewTableComponent {
  @Input() skills?: AuditedImportSkill[] = []
  @Input() showChecker = false
  showExtraInformation = true
}

@Component({
  selector: "app-named-reference",
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

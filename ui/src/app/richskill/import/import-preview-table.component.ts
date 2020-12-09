import {Component, Input, OnInit} from "@angular/core";
import {ApiSkill, INamedReference} from "../ApiSkill";
import {ApiSkillUpdate} from "../ApiSkillUpdate";


@Component({
  selector: "app-import-preview-table",
  templateUrl: "./import-preview-table.component.html"
})
export class ImportPreviewTableComponent implements OnInit {

  @Input() skills: ApiSkillUpdate[] = []

  ngOnInit(): void {
    console.log("skills", this.skills)
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

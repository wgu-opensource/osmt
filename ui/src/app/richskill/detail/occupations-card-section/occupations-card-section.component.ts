import {Component, Input, OnInit} from "@angular/core"
import {IJobCode} from "../../../job-codes/Jobcode"

@Component({
  selector: "app-occupations-card-section",
  template: `
    <div>
      <div *ngIf="!!majorCodes && !isCollapsed" class="t-margin-small t-margin-top">
        <h4 class="t-type-bodyBoldCaps">Major Groups</h4>
        <div *ngFor="let code of majorCodes" class="l-flex">
          <h5 class="t-type-bodyBold t-type-noWrap">{{code.code}}</h5>
          <p>{{code.name}}</p>
        </div>
      </div>

      <div *ngIf="!!minorCodes && !isCollapsed" class="t-margin-small t-margin-top">
        <h4 class="t-type-bodyBoldCaps">Minor Groups</h4>
        <div *ngFor="let code of minorCodes" class="l-flex">
          <h5 class="t-type-bodyBold t-type-noWrap">{{code.code}}</h5>
          <p>{{code.name}}</p>
        </div>
      </div>

      <div *ngIf="!!broadCodes && !isCollapsed" class="t-margin-small t-margin-top">
        <h4 class="t-type-bodyBoldCaps">Broad Occupations</h4>
        <div *ngFor="let code of broadCodes" class="l-flex">
          <h5 class="t-type-bodyBold t-type-noWrap">{{code.code}}</h5>
          <p>{{code.name}}</p>
        </div>
      </div>

      <div class="t-margin-small t-margin-top">
        <h4 *ngIf="!!detailedCodes" class="t-type-bodyBoldCaps">Detailed Occupations</h4>
        <div *ngFor="let code of detailedCodes" class="l-flex">
          <h5 class="t-type-bodyBold t-type-noWrap">{{code.code}}</h5>
          <p>{{code.name}}</p>
        </div>
      </div>

      <div class="t-margin-small t-margin-top">
        <h4 *ngIf="!!onetCodes" class="t-type-bodyBoldCaps">O*NET Job Roles</h4>
        <div *ngFor="let code of onetCodes" class="l-flex">
          <h5 class="t-type-bodyBold t-type-noWrap">{{code.code}}</h5>
          <p>{{code.name}}</p>
        </div>
      </div>
    </div>
  `
})
export class OccupationsCardSectionComponent implements OnInit {

  @Input() codes!: IJobCode[]
  @Input() isCollapsed = false

  majorCodes!: Set<IJobCode>
  minorCodes!: Set<IJobCode>
  broadCodes!: Set<IJobCode>
  detailedCodes!: Set<IJobCode>
  onetCodes!: Set<IJobCode>

  constructor() { }

  distinctJobcodes(input: Array<IJobCode>): Array<IJobCode> {
    return input.filter((item, idx, arr) => arr.findIndex(it => it.code === item.code) === idx)
  }

  ngOnInit(): void {
    this.majorCodes = this.major()
    this.minorCodes = this.minor()
    this.broadCodes = this.broad()
    this.detailedCodes = this.detailed()
    this.onetCodes = this.onet()
  }

  major(): Set<IJobCode> {
    return new Set(this.distinctJobcodes(this.codes?.flatMap(code => code.parents?.filter(p => p.level === "Major") ?? [])))
  }

  minor(): Set<IJobCode> {
    return new Set(this.distinctJobcodes(this.codes?.flatMap(code => code.parents?.filter(p => p.level === "Minor") ?? [])))
  }

  broad(): Set<IJobCode> {
    return new Set(this.distinctJobcodes(this.codes?.flatMap(code => code.parents?.filter(p => p.level === "Broad") ?? [])))
  }
  detailed(): Set<IJobCode> {
    return new Set(this.distinctJobcodes(this.codes?.flatMap(code => code.parents?.filter(p => p.level === "Detailed") ?? [])))
  }

  onet(): Set<IJobCode> {
    return new Set(this.distinctJobcodes(this.codes?.filter(code => code.framework === "o*net")))
  }
}

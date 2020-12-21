import {Component, Input, OnInit} from "@angular/core"
import {IJobCode} from "../../../job-codes/Jobcode"

@Component({
  selector: "app-occupations-card-section",
  template: `
    <div>
      <div *ngIf="!!majorCodes && !isCollapsed">
        <h4 class="t-type-bodyBold">Major Groups</h4>
        <p *ngFor="let aMajorCode of majorCodes">{{aMajorCode}}</p>
      </div>

      <div *ngIf="!!minorCodes && !isCollapsed">
        <h4 class="t-type-bodyBold">Minor Groups</h4>
        <p *ngFor="let aMajorCode of minorCodes">{{aMajorCode}}</p>
      </div>

      <div *ngIf="!!broadCodes && !isCollapsed">
        <h4 class="t-type-bodyBold">Broad Occupations</h4>
        <p *ngFor="let aMajorCode of broadCodes">{{aMajorCode}}</p>
      </div>

      <h4 *ngIf="!!detailedCodes" class="t-type-bodyBold">Detailed Occupations</h4>
      <p *ngFor="let aMajorCode of detailedCodes">{{aMajorCode}}</p>

      <h4 *ngIf="!!onetCodes" class="t-type-bodyBold">O*NET Job Roles</h4>
      <p *ngFor="let aOnetCode of onetCodes">{{aOnetCode}}</p>
    </div>
  `
})
export class OccupationsCardSectionComponent implements OnInit {

  @Input() codes!: IJobCode[]
  @Input() isCollapsed = false

  majorCodes!: Set<string>
  minorCodes!: Set<string>
  broadCodes!: Set<string>
  detailedCodes!: Set<string>
  onetCodes!: Set<string>

  constructor() { }

  ngOnInit(): void {
    this.majorCodes = this.major()
    this.minorCodes = this.minor()
    this.broadCodes = this.broad()
    this.detailedCodes = this.detailed()
    this.onetCodes = this.onet()
  }

  major(): Set<string> {
    const x =  new Set(this.codes?.flatMap(code => {
      return code.parents?.filter(p => p.level === "Major")?.map(p => `${p.code} ${p.name ?? ""}`) ?? []
    }))
    return x
  }

  minor(): Set<string> {
    return new Set(this.codes?.flatMap(code => {
      return code.parents?.filter(p => p.level === "Minor")?.map(p => `${p.code} ${p.name ?? ""}`) ?? []
    }))
  }

  broad(): Set<string> {
    return new Set(this.codes?.flatMap(code => {
      return code.parents?.filter(p => p.level === "Broad")?.map(p => `${p.code} ${p.name ?? ""}`) ?? []
    }))
  }
  detailed(): Set<string> {
    return new Set(this.codes?.flatMap(code => {
      return code.parents?.filter(p => p.level === "Detailed")?.map(p => `${p.code} ${p.name ?? ""}`) ?? []
    }))
  }

  onet(): Set<string> {
    return new Set(this.codes?.flatMap(code => {
      if (code.framework === "o*net") {
        return `${code.code} ${code.name ?? ""}`
      } else {
        return ""
      }
    }).filter(c => c.length > 0))
  }
}

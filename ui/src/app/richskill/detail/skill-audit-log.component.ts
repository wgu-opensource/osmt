import {Component, Input} from "@angular/core";
import {RichSkillService} from "../service/rich-skill.service";
import {AccordianComponent} from "../../core/accordian.component";
import {ApiAuditLog, AuditOperationType} from "../ApiSkill";
import {Observable} from "rxjs";
import {SvgHelper, SvgIcon} from "../../core/SvgHelper";


@Component({
  selector: "app-skill-audit-log",
  templateUrl: "./skill-audit-log.component.html"
})
export class SkillAuditLogComponent extends AccordianComponent {
  @Input() uuid!: string

  resultsLoaded?: Observable<ApiAuditLog[]>
  results?: ApiAuditLog[]


  createIcon = SvgHelper.path(SvgIcon.ADD)
  editIcon = SvgHelper.path(SvgIcon.EDIT)
  publishIcon = SvgHelper.path(SvgIcon.PUBLISH)
  archiveIcon = SvgHelper.path(SvgIcon.ARCHIVE)

  constructor(protected richSkillService: RichSkillService) {
    super()
  }

  toggle(): void {
    super.toggle()
    if (this.isExpanded && this.results === undefined) {
      this.fetch()
    }
  }

  fetch(): void {
    console.log("fetching")
    this.resultsLoaded = this.richSkillService.auditLog(this.uuid)
    this.resultsLoaded.subscribe(results => {
      console.log("audit log results", results)
      this.results = results
    })
  }

  iconForEntry(entry: ApiAuditLog): string {
    switch (entry.operationType) {
      case AuditOperationType.Insert: return this.editIcon
      case AuditOperationType.Update: return this.editIcon
      case AuditOperationType.PublishStatusChange: return this.publishIcon
    }
  }

  labelForEntry(entry: ApiAuditLog): string {
    switch (entry.operationType) {
      case AuditOperationType.Insert:
      case AuditOperationType.Update: return "Edited"
      case AuditOperationType.PublishStatusChange: return this.publishIcon
    }
  }
}

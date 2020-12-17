import {Component, Input} from "@angular/core";
import {RichSkillService} from "../service/rich-skill.service";
import {AccordianComponent} from "../../core/accordian.component";
import {ApiAuditLog, AuditOperationType} from "../ApiSkill";
import {Observable} from "rxjs";
import {SvgHelper, SvgIcon} from "../../core/SvgHelper";
import {CollectionService} from "../../collection/service/collection.service";


@Component({
  selector: "app-audit-log",
  templateUrl: "./audit-log.component.html"
})
export class AuditLogComponent extends AccordianComponent {
  @Input() skillUuid?: string
  @Input() collectionUuid?: string

  resultsLoaded?: Observable<ApiAuditLog[]>
  results?: ApiAuditLog[]


  createIcon = SvgHelper.path(SvgIcon.ADD)
  editIcon = SvgHelper.path(SvgIcon.EDIT)
  publishIcon = SvgHelper.path(SvgIcon.PUBLISH)
  archiveIcon = SvgHelper.path(SvgIcon.ARCHIVE)

  constructor(
    protected richSkillService: RichSkillService,
    protected collectionService: CollectionService
  ) {
    super()
  }

  toggle(): void {
    super.toggle()
    if (this.isExpanded && this.results === undefined) {
      this.fetch()
    }
  }

  fetchLog(): Observable<ApiAuditLog[]> | undefined {
    if (this.skillUuid) {
      return this.richSkillService.auditLog(this.skillUuid)
    }
    else if (this.collectionUuid) {
      return this.collectionService.auditLog(this.collectionUuid)
    }
    return undefined
  }

  fetch(): void {
    this.resultsLoaded = this.fetchLog()
    this.resultsLoaded?.subscribe(results => {
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

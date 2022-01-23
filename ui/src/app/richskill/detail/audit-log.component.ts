import {Component, Input} from "@angular/core"
import {RichSkillService} from "../service/rich-skill.service"
import {AccordianComponent} from "../../core/accordian.component"
import {ApiAuditLog, AuditOperationType} from "../ApiSkill"
import {Observable} from "rxjs"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {CollectionService} from "../../collection/service/collection.service"
import {PublishStatus} from "../../PublishStatus"


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
  unarchiveIcon = SvgHelper.path(SvgIcon.UNARCHIVE)
  dismissIcon = SvgHelper.path(SvgIcon.DISMISS)
  shareIcon = SvgHelper.path(SvgIcon.SHARE)
  unshareIcon = SvgHelper.path(SvgIcon.UNSHARE)

  constructor(
    protected richSkillService: RichSkillService,
    protected collectionService: CollectionService
  ) {
    super()
  }

  toggle(): void {
    super.toggle()
    if (this.isExpanded) {
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
      case AuditOperationType.PublishStatusChange: switch (entry.changedFields[0]?.new) {
        case PublishStatus.Published: return entry.changedFields[0]?.old === "Archived" ? this.unarchiveIcon : this.publishIcon
        case PublishStatus.Archived: return this.archiveIcon
        case PublishStatus.Unarchived: return this.unarchiveIcon
        case PublishStatus.Deleted: return this.archiveIcon
        case PublishStatus.Draft: return this.unarchiveIcon
        default: return this.publishIcon
      }
      case AuditOperationType.ExternalSharingChange: return entry.changedFields[0]?.new === "true" ? this.shareIcon : this.unshareIcon
    }
  }

  labelForEntry(entry: ApiAuditLog): string {
    switch (entry.operationType) {
      case AuditOperationType.Insert: return "Created"
      case AuditOperationType.Update: return "Edited"
      case AuditOperationType.PublishStatusChange: switch (entry.changedFields[0]?.new) {
        case PublishStatus.Deleted: return "Archived"
        case PublishStatus.Draft: return "Unarchived"
        case PublishStatus.Published: return entry.changedFields[0]?.old === "Archived" ? "Unarchived" : "Published"
        default: return entry.changedFields[0]?.new
      }
      case AuditOperationType.ExternalSharingChange: return entry.changedFields[0]?.new === "true" ? "Shared to Search Hub" : "Unshared from Search Hub"
    }
  }

  shouldDisplayFieldChanges(entry: ApiAuditLog): boolean {
    return (entry?.operationType !== AuditOperationType.PublishStatusChange
             && entry?.operationType !== AuditOperationType.ExternalSharingChange)
  }

  visibleFieldName(fieldName: string): string {
    switch (fieldName.toLowerCase()) {
      case "statement": return "Skill Statement"
      case "publishstatus": return "Publish Status"
      case "searchingkeywords": return "Keywords"
      case "alignments": return "Alignment"
      case "jobcodes": return "Occupations"
      default: return fieldName
    }
  }
}

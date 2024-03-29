import { formatDate } from "@angular/common";
import { Component, Inject, LOCALE_ID } from "@angular/core";

import { RichSkillService } from "../richskill/service/rich-skill.service";
import { ApiTaskResult } from "../task/ApiTaskResult";
import { ToastService } from "../toast/toast.service";

import * as FileSaver from "file-saver";
import {ApiSearch} from "../richskill/service/rich-skill-search.service"
import {PublishStatus} from "../PublishStatus"

@Component({
  selector: "app-export-rsd",
  template: ``
})
export class ExportRsdComponent {
  uuid = "";
  matchingQuery: string[] = [""];

  taskUuidInProgress: string | undefined;
  intervalHandle: number | undefined;

  constructor(
    protected richSkillService: RichSkillService,
    protected toastService: ToastService,
    @Inject(LOCALE_ID) protected locale: string
  ) {
  }

  getRsdCsv(uuid: string, entityName: string): void {
    this.uuid = uuid;
    const skillExportName = entityName ? entityName : "OSMT Skill"
    this.toastService.showBlockingLoader()
    this.richSkillService.getSkillCsvByUuid(this.uuid)
      .subscribe((csv: string) => {
        const blob = new Blob([csv], {type: "text/csv;charset=utf-8;"})
        const date = formatDate(new Date(), "yyyy-MM-dd", this.locale)
        FileSaver.saveAs(blob, `RSD Skills - ${skillExportName} ${date}.csv`)
        this.toastService.hideBlockingLoader()
      })
  }

  getRsdXlsx(uuid: string, entityName: string, status?: PublishStatus): void {
    // Implementation differs from getRsdCsv, the below endpoint schedules a task
    this.matchingQuery = [uuid]
    const statuses = status ? new Set<PublishStatus>([status]) : new Set<PublishStatus>()
    this.richSkillService.exportSearchXlsx(new ApiSearch({uuids: [uuid]}), statuses)
      .subscribe((apiTask) => {
        this.richSkillService.getResultExportedXlsxLibrary(
          apiTask.id.slice(1)).subscribe(
          response => {
            this.downloadAsXlsx(response.body)
          }
        )
      })
  }

  exportSearchCsv(apiSearch: ApiSearch, matchingQuery: string[], filterByStatuses?: Set<PublishStatus>): void {
    this.matchingQuery = matchingQuery;
    this.richSkillService.exportSearchCsv(apiSearch, filterByStatuses)
      .subscribe((apiTask) => {
        this.richSkillService.getResultExportedCsvLibrary(apiTask.id.slice(1)).subscribe(
          response => {
            this.downloadAsCsv(response.body)
          }
        )
      })
  }

  exportSearchXlsx(apiSearch: ApiSearch, matchingQuery: string[], filterByStatuses?: Set<PublishStatus>): void {
    this.matchingQuery = matchingQuery;
    this.richSkillService.exportSearchXlsx(apiSearch, filterByStatuses)
      .subscribe((apiTask) => {
        this.richSkillService.getResultExportedXlsxLibrary(
            apiTask.id.slice(1)
          ).subscribe(
          response => {
            this.downloadAsXlsx(response.body)
          }
        )
      })
  }

  exportLibraryCsv(): void {
    this.matchingQuery = ["Library Export"]
    this.richSkillService.libraryExportCsv()
      .subscribe((apiTaskResult: ApiTaskResult) => {
        this.richSkillService.getResultExportedCsvLibrary(
            apiTaskResult.id.slice(1)
          ).subscribe(
          response => {
            this.downloadAsCsv(response.body)
          }
        )
      })
  }

  exportLibraryXlsx(): void {
    this.matchingQuery = ["Library Export"]
    this.richSkillService.libraryExportXlsx()
      .subscribe((apiTaskResult: ApiTaskResult) => {
        this.richSkillService.getResultExportedXlsxLibrary(
            apiTaskResult.id.slice(1)
          ).subscribe(
          response => {
            this.downloadAsXlsx(response.body)
          }
        )
      })
  }

  private downloadAsCsv(csv: string): void {
    const blob = new Blob([csv], {type: "text/csv;charset=utf-8;"})
    const date = formatDate(new Date(), "yyyy-MM-dd", this.locale)
    FileSaver.saveAs(blob, `RSD Skills - ${this.matchingQuery} - ${date}.csv`)
  }

  private downloadAsXlsx(body: string): void {
    const blob = new Blob([body], { type: "application/vnd.ms-excel;charset=utf-8;" });
    const date = formatDate(new Date(), "yyyy-MM-dd", this.locale);
    FileSaver.saveAs(blob, `RSD Skills - ${this.matchingQuery} - ${date}.xlsx`);
  }
}

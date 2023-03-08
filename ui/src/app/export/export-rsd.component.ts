import { formatDate } from "@angular/common";
import { Component, Inject, LOCALE_ID } from "@angular/core";

import { RichSkillService } from "../richskill/service/rich-skill.service";
import { ApiTaskResult } from "../task/ApiTaskResult";
import { ToastService } from "../toast/toast.service";

import * as FileSaver from "file-saver";

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

  getRsdXlsx(uuid: string, entityName: string): void {
    // this.uuid = uuid;
    // const skillExportName = entityName ? entityName : "OSMT Skill"
    // this.toastService.showBlockingLoader()
    // this.richSkillService.getSkillXlsxByUuid(this.uuid)
    //   .subscribe((xlsxExport: string) => {
    //     const blob = new Blob([xlsxExport], {type: "application/vnd.ms-excel;charset=utf-8;"})
    //     const date = formatDate(new Date(), "yyyy-MM-dd", this.locale)
    //     FileSaver.saveAs(blob, `RSD Skills Excel - ${skillExportName} - ${date}.xlsx`)
    //     this.toastService.hideBlockingLoader()
    //   })
    this.matchingQuery = [uuid]
    this.richSkillService.exportSearchXlsx(this.matchingQuery)
      .subscribe((apiTask) => {
        this.richSkillService.getResultExportedXlsxLibrary(
          apiTask.id.slice(1)).subscribe(
          response => {
            this.downloadAsXlsx(response.body)
          }
        )
      })
  }

  exportSearchCsv(uuids: string[], matchingQuery: string[]): void {
    this.matchingQuery = matchingQuery;
    this.richSkillService.exportSearchCsv(uuids)
      .subscribe((apiTask) => {
        this.richSkillService.getResultExportedCsvLibrary(apiTask.id.slice(1)).subscribe(
          response => {
            this.downloadAsCsv(response.body)
          }
        )
      })
  }

  exportSearchXlsx(uuids: string[], matchingQuery: string[]): void {
    this.matchingQuery = matchingQuery;
    this.richSkillService.exportSearchXlsx(uuids)
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

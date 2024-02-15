import { formatDate } from "@angular/common";
import { Component, Inject, LOCALE_ID } from "@angular/core";

import { CollectionService } from "../collection/service/collection.service";
import { ITaskResult } from "../task/ApiTaskResult";
import { ToastService } from "../toast/toast.service";

import * as FileSaver from "file-saver";

@Component({
  selector: "app-export-collection",
  template: ``
})
export class ExportCollectionComponent {
  uuid = "";
  entityName = "";

  taskUuidInProgress: string | undefined;
  intervalHandle: number | undefined;

  constructor(
    protected collectionService: CollectionService,
    protected toastService: ToastService,
    @Inject(LOCALE_ID) protected locale: string
  ) {};

  getCollectionCsv(uuid: string, entityName: string): void {
    this.uuid = uuid;
    this.entityName = entityName;
    this.toastService.showBlockingLoader()
    this.collectionService.requestCollectionSkillsCsv(this.uuid)
      .subscribe((taskStarted: ITaskResult) => {
        this.taskUuidInProgress = taskStarted.uuid;
        this.intervalHandle = setInterval(() => this.pollCsv(), 1000);
      });
  };

  getCollectionXlsx(uuid: string, entityName: string): void {
    this.uuid = uuid;
    this.entityName = entityName;
    this.toastService.showBlockingLoader()
    this.collectionService.requestCollectionSkillsXlsx(this.uuid)
      .subscribe((taskStarted: ITaskResult) => {
        this.taskUuidInProgress = taskStarted.uuid;
        this.intervalHandle = setInterval(() => this.pollXlsx(), 1000);
      });
  };

  pollCsv(): void {
    if (this.taskUuidInProgress === undefined) { // fail fast
      clearInterval(this.intervalHandle);
      return;
    }

    this.collectionService.getCsvTaskResultsIfComplete(this.taskUuidInProgress)
      .subscribe(({body, status}) => {
        if (status === 200) {
          this.taskUuidInProgress = undefined;

          clearInterval(this.intervalHandle);

          const blob = new Blob([body], { type: "text/csv;charset=utf-8;" });
          const date = formatDate(new Date(), "yyyy-MM-dd", this.locale);
          FileSaver.saveAs(blob, `RSD Skills - ${this.entityName} - ${date}.csv`);
          this.toastService.hideBlockingLoader()
        }
      });
  };

  pollXlsx(): void {
    if (this.taskUuidInProgress === undefined) { // fail fast
      clearInterval(this.intervalHandle);
      return;
    }

    this.collectionService.getXlsxTaskResultsIfComplete(this.taskUuidInProgress)
      .subscribe(({body, status}) => {
        if (status === 200) {
          this.taskUuidInProgress = undefined;

          clearInterval(this.intervalHandle);

          const blob = new Blob([body], { type: "application/vnd.ms-excel;charset=utf-8;" });
          const date = formatDate(new Date(), "yyyy-MM-dd", this.locale);
          FileSaver.saveAs(blob, `RSD Skills Excel - ${this.entityName} - ${date}.xlsx`);
          this.toastService.hideBlockingLoader()
        }
      });
  };
}

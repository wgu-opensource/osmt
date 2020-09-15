import { Component, OnInit } from "@angular/core"
import { TaskService } from "../../task/task-service"
import { ITaskResponse } from "../../task/TaskInProgress"

@Component({
  selector: "app-richskills-csv-export",
  templateUrl: "./rich-skills-csv-export.component.html",
  styleUrls: ["./rich-skills-csv-export.component.css"]
})
export class RichSkillsCsvExportComponent implements OnInit {

  taskUuidInProgress: string | undefined
  csvExport: string | undefined
  intervalHandle: number | undefined

  constructor(private taskService: TaskService) {

  }

  pollCsv(): void {
    if (this.taskUuidInProgress === undefined) { // fail fast
      clearInterval(this.intervalHandle)
      return
    }

    this.taskService.getTaskResultsIfComplete(this.taskUuidInProgress)
      .subscribe(response => {
        const { body, status } = response

        if (status === 200) {
          this.csvExport = body as string
          this.taskUuidInProgress = undefined

          clearInterval(this.intervalHandle)
          this.downloadFile(body, "text/csv")
        }
      })
  }

  // tslint:disable-next-line:no-any
  downloadFile(data: any, type: string): void {
    const blob = new Blob([data], { type })
    const url = window.URL.createObjectURL(blob)
    const pwa = window.open(url)
    if (!pwa || pwa.closed || typeof pwa.closed === "undefined") {
      alert( "Please disable your Pop-up blocker and try again.")
    }
  }

  startDownloadTask(): void {
    this.csvExport = undefined

    this.taskService.startAllSkillsCsvTask().subscribe((taskStarted: ITaskResponse) => {
      this.taskUuidInProgress = taskStarted.uuid
      this.intervalHandle = setInterval(() => this.pollCsv(), 1000)
    })
  }

  ngOnInit(): void {

  }

}

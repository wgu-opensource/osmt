import { Component, OnInit } from "@angular/core"
import { TaskService } from "../../task/task-service"
import { ITaskResponse } from "../../task/TaskInProgress"
import { saveAs } from 'file-saver';


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
      .subscribe(({body, status}) => {
        if (status === 200) {
          this.csvExport = body as string
          this.taskUuidInProgress = undefined

          clearInterval(this.intervalHandle)

          const blob = new Blob([body], { type: "text/csv;charset=utf-8;" })
          const filename = `OSMT Skills Library - ${new Date().toDateString()}.csv`
          saveAs(blob, filename)

        }
      })
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

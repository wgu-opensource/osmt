import {Component, ElementRef, EventEmitter, Input, Output, ViewChild} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"

export enum SelectAll {
  SELECT_ALL,
  SELECT_ALL_ON_PAGE
}

@Component({
  selector: "app-select-all",
  templateUrl: "./select-all.component.html",
  styleUrls: ["./select-all.component.scss"]
})
export class SelectAllComponent {

  selectAllOptions = SelectAll
  checkIcon = SvgHelper.path(SvgIcon.CHECK)
  @ViewChild("selectAll") select?: ElementRef

  @Input()
  selectAllEnabled?: boolean
  @Input()
  totalCount = 0
  @Input()
  totalPageCount = 0
  @Output() valueChange: EventEmitter<any> = new EventEmitter<any>()

  onClickCheckbox(selected: boolean): void {
    this.valueChange.emit({value: +this.select?.nativeElement.value, selected})
  }
}

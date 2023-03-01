import {Component, EventEmitter, HostListener, Input, OnInit, Output} from "@angular/core"
import { KeywordType } from "../../richskill/ApiSkill"
import { FormBuilder, FormGroup } from "@angular/forms"
import {FilterDropdown} from "../../models/filter-dropdown.model"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"

@Component({
  selector: "app-filter-dropdown",
  templateUrl: "./filter-dropdown.component.html",
  styleUrls: ["./filter-dropdown.component.scss"]
})
export class FilterDropdownComponent {

  @Output()
  applyFilter = new EventEmitter<FilterDropdown>()
  showInputs = false
  @Input()
  filterFg?: FormGroup
  keywordType = KeywordType
  iconDismiss = SvgHelper.path(SvgIcon.DISMISS)

  @HostListener("document:keydown.escape", ["$event"])
  onKeydownHandler(event: KeyboardEvent): void {
    this.showInputs = false
  }

  onApplyFilter(): void {
    this.showInputs = !this.showInputs
    this.applyFilter.emit(this.filterFg?.value)
  }

}

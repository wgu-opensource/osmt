import {Component, OnInit, Output, EventEmitter, Input} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../../core/SvgHelper"

@Component({
  selector: "app-create-collection-action-bar",
  template: ""
})
export class AbstractCreateCollectionActionbarComponent implements OnInit {

  @Input() collectionForm = undefined
  @Input() collectionSaved = undefined

  @Output() saveClicked = new EventEmitter<void>()
  @Output() cancelClicked = new EventEmitter<void>()

  checkOutlineIcon = SvgHelper.path(SvgIcon.CHECK_OUTLINE)
  cancelIcon = SvgHelper.path(SvgIcon.CANCEL)

  constructor() { }

  ngOnInit(): void {
  }

  handleSave(): void {
    this.saveClicked.emit()
  }

  handleCancel(): void {
    this.cancelClicked.emit()
  }
}

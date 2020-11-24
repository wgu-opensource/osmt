import {Component, OnInit, Output, EventEmitter, Input} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../../core/SvgHelper"
import {FormControl} from "@angular/forms";

@Component({
  selector: "app-create-collection-action-bar",
  template: ""
})
export class AbstractCreateCollectionActionbarComponent implements OnInit {

  @Input() collectionForm: FormControl | undefined = undefined
  @Input() collectionSaved = undefined

  @Output() cancelClicked = new EventEmitter<void>()
  @Output() scrollToTopClicked = new EventEmitter<void>()

  checkOutlineIcon = SvgHelper.path(SvgIcon.CHECK_OUTLINE)
  cancelIcon = SvgHelper.path(SvgIcon.CANCEL)

  constructor() { }

  get formValid(): boolean {
    return this.collectionForm?.valid ?? false
  }

  ngOnInit(): void {
  }

  handleCancel(): void {
    this.cancelClicked.emit()
  }
}

import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {forkJoin, Observable} from "rxjs"
import {FormGroup} from "@angular/forms"
import {SvgHelper, SvgIcon} from "../core/SvgHelper"

@Component({
  selector: "app-formfield-submit",
  templateUrl: "./form-field-submit.component.html"
})
export class FormFieldSubmit implements OnInit {

  @Input() mobileView = false

  @Input() label = "Save"
  @Input() formGroup: FormGroup = new FormGroup({})
  @Input() enabled: boolean = true
  @Input() dirty: boolean = false

  @Output() errorsOccurred = new EventEmitter()

  checkOutlineIcon = SvgHelper.path(SvgIcon.CHECK_OUTLINE)

  private _processing = false

  private _observables: Array<Observable<unknown>> = []
  @Input() set observables(value: Array<Observable<unknown>>) {
    const noObservables = value
      .filter(v => !!v)
      .length <= 0

    if (noObservables) {
      return
    }

    this._observables = value.filter(o => o !== null)
    if (this._observables.length > 0) {
      this._processing = true
      forkJoin(this._observables).subscribe(
        () => {},
        (error) => {
          console.log("Form error!", error)
          this._processing = false
          this.reportErrors(error)
        },
        () => {
          this._processing = false
        }
      )
    }
  }
  get observables(): Array<Observable<unknown>> {
    return this._observables
  }

  reportErrors(error: any): void {
    this.errorsOccurred.emit(error)
  }

  constructor() {
  }

  ngOnInit(): void {
  }

  isEnabled(): boolean {
    if (!this.enabled) return false
    const {touched, valid, pristine, dirty} = this.formGroup
    return (touched || (this.dirty || dirty)) && (this.dirty || !pristine) && valid
  }

  isDisabled(): boolean {
    return !this.isEnabled()
  }

  isProcessing(): boolean {
    return this._processing
  }
}

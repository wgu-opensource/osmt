import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {forkJoin, Observable} from "rxjs";
import {FormGroup} from "@angular/forms";

@Component({
  selector: "app-formfield-submit",
  templateUrl: "./form-field-submit.component.html"
})
export class FormFieldSubmit implements OnInit {
  @Input() label = "Save"
  @Input() formGroup: FormGroup = new FormGroup({})

  @Output() errorsOccurred = new EventEmitter()

  private _processing = false

  private _observables: Array<Observable<unknown>> = []
  @Input() set observables(value: Array<Observable<unknown>>) {
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

  isDisabled(): boolean {
    return !this.formGroup.touched || !this.formGroup.valid || this.formGroup.pristine
  }

  isProcessing(): boolean {
    return this._processing
  }
}

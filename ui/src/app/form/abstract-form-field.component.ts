import {Component, Input, OnInit} from "@angular/core"
import {FormControl} from "@angular/forms"

@Component({
  selector: "app-abstract-form-field",
  template: ""
})
export abstract class AbstractFormField<TValue> implements OnInit {

  @Input() control: FormControl = new FormControl(null)
  @Input() label: string = ""
  @Input() placeholder: string = ""
  @Input() includePlaceholder: boolean = true
  @Input() errorMessage: string = ""
  @Input() helpMessage: string = ""
  @Input() required: boolean = false
  @Input() name: string = ""

  abstract get emptyValue(): TValue

  constructor() {
    this.clearField()
  }

  ngOnInit(): void {
    this.control.valueChanges.subscribe((v: string) => this.onValueChange(v) )
  }

  get isError(): boolean {
    return this.control && (this.control.dirty || this.control.touched) && this.control.invalid
  }

  get isRequired(): boolean {
    return this.required
  }

  get controlValue(): TValue {
    return this.control.value as TValue ?? this.emptyValue
  }

  set controlValue(value: TValue|null) {
    this.setControlValue(value, true)
    this.control.markAsDirty()
    this.control.markAsTouched()
  }

  clearValue(): void {
    this.controlValue = this.emptyValue
  }

  clearField(): void {
    this.clearValue()
    this.control.reset()
  }

  protected setControlValue(value: TValue|null, emitEvent: boolean = true) {
    this.control.setValue(value ?? null, { emitEvent: emitEvent })
  }

  protected handleValueChange(newValue: string): void {
    return
  }

  protected onValueChange(newValue: string): void {
    this.handleValueChange(newValue)
  }
}

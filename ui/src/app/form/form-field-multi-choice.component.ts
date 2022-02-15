import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {FormControl} from "@angular/forms"
import {FormField} from "./form-field.component"
import {IChoice} from "./form-field-choice.component"


@Component({
  selector: "app-formfield-multi-choice",
  templateUrl: "./form-field-multi-choice.component.html"
})
export class FormFieldMultiChoiceComponent extends FormField<Set<IChoice>> implements OnInit{

  @Input() control: FormControl = new FormControl("")
  @Input() label: string = ""
  @Input() name: string = ""
  @Input() choices: IChoice[] = []
  @Input() required: boolean = false
  @Output() selectedChoicesChanged: EventEmitter<Set<IChoice>> = new EventEmitter<Set<IChoice>>()

  private choiceMap: Map<string|number, IChoice> = new Map<string|number, IChoice>()
  private selectedChoiceIds: Set<string|number> = new Set<string|number>()

  get selectedChoices(): Set<IChoice> {
   return new Set(
    [...this.choiceMap.entries()]
      .filter((e) => this.selectedChoiceIds.has(e[0]))
      .map((e) => e[1])
    )
  }

  constructor() {
    super()
  }

  ngOnInit(): void {
    super.ngOnInit()
    this.initChoices()
  }

  isChoiceSelected(choiceId: string|number): boolean {
    return this.selectedChoiceIds.has(choiceId)
  }

  onChoiceSelected(choiceId: string|number): void {
    this.selectedChoiceIds.add(choiceId)
    this.onSelectedChoicesChanged()
  }

  onChoiceDeselected(choiceId: string|number): void {
    this.selectedChoiceIds.delete(choiceId)
    this.onSelectedChoicesChanged()
  }

  private onSelectedChoicesChanged(): void {
    this.value = this.selectedChoices
    this.selectedChoicesChanged.emit(this.selectedChoices)
  }

  private initChoices(): void {
    this.choiceMap.clear()
    this.choices.forEach((c, i) => {
      c.id = c.id ?? i

      this.choiceMap.set(c.id, c)

      if (c.initiallySelected) {
        this.selectedChoiceIds.add(c.id)
      }
    })
  }
}

import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {importSkillHeaders} from "./batch-import.component";


interface MappingChanged {
  uploadedHeader: string
  property: string
}

@Component({
  selector: "app-field-mapping-table",
  templateUrl: "./field-mapping-table.component.html"
})
export class FieldMappingTableComponent implements OnInit {

  @Input() uploadedHeaders: string[] = []
  @Output() fieldMappingChanged = new EventEmitter<{[p: string]: string}>()

  currentMappings: {[p: string]: string} = {}

  ngOnInit(): void {
  }

  handleMappingChanged(change: MappingChanged): void {
    this.currentMappings[change.uploadedHeader] = change.property
    this.fieldMappingChanged.emit(this.currentMappings)
  }

  hasRequiredFields(): boolean {
    if (this.currentMappings === undefined) { return false }
    const mapped = Object.values(this.currentMappings)
    return (mapped.indexOf("skillName") !== -1 && mapped.indexOf("skillStatement") !== -1)
  }
}

@Component({
  selector: "app-field-mapping-select",
  template: `<select class="PUI" (change)="handleChange($event)">
    <option value="">Select Property</option>
    <option *ngFor="let item of headers | keyvalue" [value]="item.key">{{item.value}}</option>
  </select>`
})
export class FieldMappingSelectComponent {

  @Input() data: string = ""

  @Output() mappingChanged = new EventEmitter<MappingChanged>()

  get headers(): any {
    return importSkillHeaders
  }

  handleChange($event: Event): void {
    const target = $event.target as HTMLSelectElement
    const value = target.value
    this.mappingChanged.emit({
      uploadedHeader: this.data,
      property: value
    })
  }
}

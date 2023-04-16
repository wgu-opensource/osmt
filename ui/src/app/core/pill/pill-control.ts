import {IAlignment, INamedReference, KeywordCount} from "../../richskill/ApiSkill";

export abstract class AbstractPillControl {

  private selected: boolean = false

  abstract get primaryLabel(): string

  get secondaryLabel(): string|undefined { return undefined }

  get isSelected(): boolean {
    return this.selected
  }

  deselect() {
    this.selected = false
  }

  select() {
    this.selected = true
  }
}

export class KeywordCountPillControl extends AbstractPillControl {

  readonly keywordCount: KeywordCount

  constructor(keywordCount: KeywordCount) {
    super();
    this.keywordCount = keywordCount
  }

  get keyword(): IAlignment | INamedReference | string {
    return this.keywordCount.keyword
  }

  get count(): number {
    return this.keywordCount.count
  }

  get primaryLabel(): string {
    return `${this.keyword}`
  }

  get secondaryLabel(): string | undefined {
    return `${this.count}`
  }
}

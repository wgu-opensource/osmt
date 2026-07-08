import {
  IAlignment,
  INamedReference,
  KeywordCount,
} from '../../richskill/ApiSkill';

export abstract class AbstractPillControl {
  private selected = false;

  abstract get primaryLabel(): string;

  get linkUrl(): string | undefined {
    return undefined;
  }

  get secondaryLabel(): string | undefined {
    return undefined;
  }

  get isSelected(): boolean {
    return this.selected;
  }

  deselect() {
    this.selected = false;
  }

  select() {
    this.selected = true;
  }
}

export class KeywordCountPillControl extends AbstractPillControl {
  readonly keywordCount: KeywordCount;
  readonly categoryLinkPrefix?: string;

  constructor(keywordCount: KeywordCount, categoryLinkPrefix?: string) {
    super();
    this.keywordCount = keywordCount;
    this.categoryLinkPrefix = categoryLinkPrefix;
  }

  get keyword(): IAlignment | INamedReference | string {
    return this.keywordCount.keyword;
  }

  get count(): number {
    return this.keywordCount.count;
  }

  get primaryLabel(): string {
    const kw = this.keyword;
    if (typeof kw === 'object' && kw !== null && 'name' in kw) {
      return (kw as INamedReference).name ?? '';
    }
    if (typeof kw === 'object' && kw !== null && 'skillName' in kw) {
      return (kw as IAlignment).skillName ?? '';
    }
    return String(kw ?? '');
  }

  get linkUrl(): string | undefined {
    if (!this.categoryLinkPrefix) return undefined;
    const kw = this.keyword;
    const id =
      typeof kw === 'object' && kw !== null && 'id' in kw
        ? (kw as INamedReference).id
        : undefined;
    return id ? `${this.categoryLinkPrefix}${id}` : undefined;
  }

  get secondaryLabel(): string | undefined {
    return `${this.count}`;
  }
}

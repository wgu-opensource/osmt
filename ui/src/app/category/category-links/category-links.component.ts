import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
} from '@angular/core';
import { CategoryService } from '../service/category.service';

@Component({
  selector: 'app-category-links',
  templateUrl: './category-links.component.html',
})
export class CategoryLinksComponent implements OnInit, OnChanges {
  @Input() categories: string[] = [];
  nameToId: Map<string, number> = new Map();
  private loaded = false;

  constructor(private categoryService: CategoryService) {}

  ngOnInit(): void {
    this.loadCategoryIds();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['categories']) {
      this.loadCategoryIds();
    }
  }

  private loadCategoryIds(): void {
    if (this.categories.length === 0 || this.loaded) {
      return;
    }
    this.loaded = true;
    this.categoryService
      .getAllPaginated(1000, 0, undefined)
      .subscribe(result => {
        result.categories.forEach(c => {
          if (c.name && !this.nameToId.has(c.name)) {
            this.nameToId.set(c.name, c.id);
          }
        });
      });
  }

  getId(name: string): number | undefined {
    return this.nameToId.get(name);
  }
}

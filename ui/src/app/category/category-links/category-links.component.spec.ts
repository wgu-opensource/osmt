import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';
import { CategoryLinksComponent } from './category-links.component';
import { CategoryService } from '../service/category.service';

describe('CategoryLinksComponent', () => {
  let component: CategoryLinksComponent;
  let fixture: ComponentFixture<CategoryLinksComponent>;
  let categoryService: jasmine.SpyObj<CategoryService>;

  beforeEach(async () => {
    const categoryServiceSpy = jasmine.createSpyObj('CategoryService', [
      'getAllPaginated',
    ]);
    categoryServiceSpy.getAllPaginated.and.returnValue(
      of({
        categories: [
          { id: 1, name: 'Category A', skillCount: 5 },
          { id: 2, name: 'Category B', skillCount: 3 },
        ],
        totalCount: 2,
      })
    );

    await TestBed.configureTestingModule({
      declarations: [CategoryLinksComponent],
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [{ provide: CategoryService, useValue: categoryServiceSpy }],
    }).compileComponents();

    categoryService = TestBed.inject(
      CategoryService
    ) as jasmine.SpyObj<CategoryService>;
    fixture = TestBed.createComponent(CategoryLinksComponent);
    component = fixture.componentInstance;
    component.categories = ['Category A', 'Category B'];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load category ids', () => {
    expect(categoryService.getAllPaginated).toHaveBeenCalledWith(
      1000,
      0,
      undefined
    );
  });

  it('should resolve category id by name', () => {
    expect(component.getId('Category A')).toBe(1);
    expect(component.getId('Category B')).toBe(2);
    expect(component.getId('Unknown')).toBeUndefined();
  });
});

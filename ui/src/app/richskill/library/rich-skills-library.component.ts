import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { RichSkillService } from '../service/rich-skill.service';
import { SkillsListComponent } from '../list/skills-list.component';
import { ToastService } from '../../toast/toast.service';
import {
  ApiSearch,
  PaginatedSkills,
} from '../service/rich-skill-search.service';
import { Router } from '@angular/router';
import { determineFilters, PublishStatus } from '../../PublishStatus';
import { Title } from '@angular/platform-browser';
import { AuthService } from '../../auth/auth-service';
import { CollectionService } from '../../collection/service/collection.service';

@Component({
  selector: 'app-rich-skills-library',
  templateUrl: '../list/skills-list.component.html',
})
export class RichSkillsLibraryComponent
  extends SkillsListComponent
  implements OnInit
{
  title = 'RSD Library';
  showAdvancedFilteredSearch = true;

  constructor(
    protected router: Router,
    protected richSkillService: RichSkillService,
    protected collectionService: CollectionService,
    protected toastService: ToastService,
    protected titleService: Title,
    protected authService: AuthService,
    protected cdr: ChangeDetectorRef
  ) {
    super(
      router,
      richSkillService,
      collectionService,
      toastService,
      authService,
      cdr
    );
  }

  ngOnInit(): void {
    this.titleService.setTitle(`${this.title} | ${this.whitelabel.toolName}`);

    // Check authentication status and adjust behavior
    if (!this.authService.isAuthenticated()) {
      this.isPublicView = true;
      // Filter to only show Published and Archived skills (API automatically filters out Draft and Deleted)
      this.selectedFilters = new Set([
        PublishStatus.Published,
        PublishStatus.Archived,
      ]);
    }

    this.loadNextPage();
  }

  loadNextPage(): void {
    if (this.selectedFilters.size < 1) {
      this.setResults(new PaginatedSkills([], 0));
      return;
    }
    const apiSearch = new ApiSearch({ filtered: this.selectedKeywords });
    this.resultsLoaded = this.richSkillService.getSkillsFiltered(
      this.size,
      this.from,
      apiSearch,
      this.selectedFilters,
      this.columnSort
    );
    this.resultsLoaded.subscribe(results => {
      this.setResults(results);
    });
  }
}

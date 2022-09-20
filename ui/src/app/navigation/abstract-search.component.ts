import {FormControl, FormGroup} from "@angular/forms"
import {SearchService} from "../search/search.service"
import {ActivatedRoute} from "@angular/router"
import {AuthService} from "../auth/auth-service"
import {ButtonAction} from "../auth/auth-roles"

export class AbstractSearchComponent {
  searchForm = new FormGroup({
    search: new FormControl("")
  })

  canSkillUpdate: boolean = false
  canSkillCreate: boolean = false
  canSkillPublish: boolean = false
  canCollectionUpdate: boolean = false
  canCollectionCreate: boolean = false
  canCollectionPublish: boolean = false
  canCollectionSkillsUpdate: boolean = false

  constructor(protected searchService: SearchService, protected route: ActivatedRoute, protected  authService: AuthService) {
    this.searchService.searchQuery$.subscribe(apiSearch => {
      if (apiSearch === undefined) {
        this.clearSearch()
      }
    })

    this.route.queryParams.subscribe(params => {
      const queryString = params.q
      if (queryString && queryString.length > 0) {
        this.searchForm.setValue({search: queryString})
      }
    })
    this.setEnableFlags()
  }

  setEnableFlags(): void {
    this.canSkillUpdate = this.authService.isEnabledByRoles(ButtonAction.SkillUpdate);
    this.canSkillCreate = this.authService.isEnabledByRoles(ButtonAction.SkillCreate);
    this.canSkillPublish = this.authService.isEnabledByRoles(ButtonAction.SkillPublish);
    this.canCollectionUpdate = this.authService.isEnabledByRoles(ButtonAction.CollectionUpdate);
    this.canCollectionCreate = this.authService.isEnabledByRoles(ButtonAction.CollectionCreate);
    this.canCollectionPublish = this.authService.isEnabledByRoles(ButtonAction.CollectionPublish);
    this.canCollectionSkillsUpdate = this.authService.isEnabledByRoles(ButtonAction.CollectionSkillsUpdate);
  }

  clearSearch(): boolean {
    this.searchForm.reset()
    return false
  }

  public get searchQuery(): string {
    return this.searchForm.get("search")?.value ?? ""
  }

  handleDefaultSubmit(): boolean {
    return this.submitSkillSearch()
  }

  submitSkillSearch(): boolean {
    if (this.searchQuery.trim().length > 0) {
      this.searchService.simpleSkillSearch(this.searchQuery)
    }
    return false
  }

  submitCollectionSearch(): boolean {
    if (this.searchQuery.trim().length > 0) {
      this.searchService.simpleCollectionSearch(this.searchQuery)
    }
    return false
  }
}

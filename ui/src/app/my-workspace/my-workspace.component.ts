import {Component, Inject, LOCALE_ID, OnInit} from "@angular/core"
import {ManageCollectionComponent} from "../collection/detail/manage-collection.component"
import {RichSkillService} from "../richskill/service/rich-skill.service"
import {ToastService} from "../toast/toast.service"
import {CollectionService} from "../collection/service/collection.service"
import {ActivatedRoute, Router} from "@angular/router"
import {Title} from "@angular/platform-browser"
import {AuthService} from "../auth/auth-service"
import {TableActionDefinition} from "../table/skills-library-table/has-action-definitions"
import {PublishStatus} from "../PublishStatus"
import {ApiSearch} from "../richskill/service/rich-skill-search.service"
import {ApiCollectionUpdate} from "../collection/ApiCollection"

@Component({
  selector: "app-my-workspace",
  templateUrl: "../collection/detail/manage-collection.component.html"
})
export class MyWorkspaceComponent extends ManageCollectionComponent implements OnInit {

  constructor(
    protected router: Router,
    protected richSkillService: RichSkillService,
    protected toastService: ToastService,
    protected collectionService: CollectionService,
    protected route: ActivatedRoute,
    protected titleService: Title,
    protected authService: AuthService,
    @Inject(LOCALE_ID) protected locale: string
  ) {
    super(router, richSkillService, toastService, collectionService, route, titleService, authService, locale)
  }

  ngOnInit(): void {
    this.reloadCollection()
  }

  reloadCollection(): void {
      this.collectionService.getWorkspace().subscribe(collection => {
        this.titleService.setTitle(`${collection.name} | Collection | ${this.whitelabel.toolName}`)
        this.collection = collection
        this.uuidParam = this.collection.uuid
        this.loadNextPage()
      })
  }

  actionDefinitions(): TableActionDefinition[] {
    return [
      new TableActionDefinition({
        label: "Add RSDs to My Workspace",
        icon: this.addIcon,
        callback: () => this.addSkillsAction(),
        visible: () => true
      }),
      new TableActionDefinition({
        label: "Download as CSV",
        icon: this.downloadIcon,
        callback: () => this.generateCsv(this.collection?.name ?? ""),
        visible: () => true
      }),
      new TableActionDefinition({
        label: "Convert to Collection",
        icon: this.publishIcon,
        callback: () => this.convertToCollectionAction(),
        visible: () => true
      }),
      new TableActionDefinition({
        label: "Reset My Workspace",
        icon: this.deleteIcon,
        callback: () => this.deleteCollectionAction(),
        visible: () => (this.collection?.skills?.length ?? 0) > 0
      })
    ]
  }

   handleConfirmDeleteCollection(): void {
    this.submitSkillRemoval(new ApiSearch({uuids: this.collection?.skills.map(s => (s as any).uuid)}))
    this.template = "default"
  }

  private convertToCollectionAction(): void {
    const updateObject = new ApiCollectionUpdate({
      name: this.collection?.name,
      author: this.collection?.author,
      skills: {add: this.collection?.skills.map(s => (s as any).uuid)}
    })
    this.collectionService.createCollection(updateObject).subscribe(newCollection => {
        this.router.navigate(["/collections/" + newCollection.uuid + "/manage"])
    })
  }

}

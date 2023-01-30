import {Component, Inject, LOCALE_ID, OnInit} from "@angular/core"
import {ManageCollectionComponent} from "../collection/detail/manage-collection.component"
import {RichSkillService} from "../richskill/service/rich-skill.service"
import {ToastService} from "../toast/toast.service"
import {CollectionService} from "../collection/service/collection.service"
import {ActivatedRoute, Router} from "@angular/router"
import {Title} from "@angular/platform-browser"
import {AuthService} from "../auth/auth-service"
import {TableActionDefinition} from "../table/skills-library-table/has-action-definitions"
import {ButtonAction} from "../auth/auth-roles"
import {PublishStatus} from "../PublishStatus"
import {size} from "lodash"

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
    this.uuidParam = "00ff748a-7141-47f2-aaf5-f9b8a992505f"
  }

  ngOnInit(): void {
    console.log("child on init")
    this.reloadCollection()
  }

  actionDefinitions(): TableActionDefinition[] {
    this.collection ? this.collection.status = PublishStatus.Workspace : false
    console.log(this.collection)
    const actions = [
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
        callback: () => this.conertToCollectionAction(),
        visible: () => true
      }),
      new TableActionDefinition({
        label: "Reset Collection",
        icon: this.deleteIcon,
        callback: () => this.deleteCollectionAction(),
        visible: () => true
      })
    ]
    return actions
  }

}

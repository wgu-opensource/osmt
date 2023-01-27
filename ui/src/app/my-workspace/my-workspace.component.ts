import {Component, Inject, LOCALE_ID, OnInit} from "@angular/core"
import {ManageCollectionComponent} from "../collection/detail/manage-collection.component"
import {RichSkillService} from "../richskill/service/rich-skill.service"
import {ToastService} from "../toast/toast.service"
import {CollectionService} from "../collection/service/collection.service"
import {ActivatedRoute, Router} from "@angular/router"
import {Title} from "@angular/platform-browser"
import {AuthService} from "../auth/auth-service"

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
    this.uuidParam = "a11efb86-1650-4ecd-ad86-9c111f4b1fbb"
  }

  ngOnInit(): void {
    console.log("child on init")
    this.reloadCollection()
  }

}

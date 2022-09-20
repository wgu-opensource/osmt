import {Component, OnInit} from "@angular/core"
import {AbstractSearchComponent} from "./abstract-search.component"
import {SearchService} from "../search/search.service"
import {ActivatedRoute} from "@angular/router"
import {SvgHelper, SvgIcon} from "../core/SvgHelper"
import {AuthService} from "../auth/auth-service"

@Component({
  selector: "app-commoncontrols",
  templateUrl: "./commoncontrols.component.html"
})
export class CommoncontrolsComponent extends AbstractSearchComponent implements OnInit {

  searchIcon = SvgHelper.path(SvgIcon.SEARCH)
  dismissIcon = SvgHelper.path(SvgIcon.DISMISS)

  constructor(protected searchService: SearchService, protected route: ActivatedRoute, protected authService: AuthService) {
    super(searchService, route, authService)
  }

  ngOnInit(): void {
  }
}

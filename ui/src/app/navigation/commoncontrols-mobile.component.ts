import { Component, OnInit } from "@angular/core"
import {AbstractSearchComponent} from "./abstract-search.component";
import {SearchService} from "../search/search.service";
import {ActivatedRoute} from "@angular/router";

@Component({
  // tslint:disable-next-line:component-selector
  selector: "[app-commoncontrols-mobile]",
  templateUrl: "./commoncontrols-mobile.component.html"
})
export class CommoncontrolsMobileComponent extends AbstractSearchComponent implements OnInit {

  constructor(protected searchService: SearchService, protected route: ActivatedRoute) {
    super(searchService, route)
  }

  ngOnInit(): void {
  }

}

import { Component, OnInit } from "@angular/core"
import {AbstractSearchComponent} from "./abstract-search.component";
import {SearchService} from "../search/search.service";

@Component({
  selector: "app-commoncontrols-mobile",
  templateUrl: "./commoncontrols-mobile.component.html"
})
export class CommoncontrolsMobileComponent extends AbstractSearchComponent implements OnInit {

  constructor(protected searchService: SearchService) {
    super(searchService)
  }

  ngOnInit(): void {
  }

}

import {Component, OnInit} from "@angular/core"
import {AbstractSearchComponent} from "./abstract-search.component";
import {SearchService} from "../search/search.service";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: "app-commoncontrols",
  templateUrl: "./commoncontrols.component.html"
})
export class CommoncontrolsComponent extends AbstractSearchComponent implements OnInit {

  constructor(protected searchService: SearchService, protected route: ActivatedRoute) {
    super(searchService, route)
  }

  ngOnInit(): void {
  }

}

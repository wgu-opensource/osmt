import {Component, OnInit} from "@angular/core";
import {AbstractSearchComponent} from "../navigation/abstract-search.component";
import {SearchService} from "../search/search.service";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: "app-empty-message",
  templateUrl: "./empty-message.component.html"
})
export class CommoncontrolsMobileComponent extends AbstractSearchComponent implements OnInit {

  constructor(protected searchService: SearchService, protected route: ActivatedRoute) {
    super(searchService, route)
  }

  ngOnInit(): void {
  }

}

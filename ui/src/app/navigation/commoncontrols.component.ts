import { Component, OnInit } from "@angular/core"
import {FormControl, FormGroup} from "@angular/forms";
import {AbstractSearchComponent} from "./abstract-search.component";
import {SearchService} from "../search/search.service";

@Component({
  selector: "app-commoncontrols",
  templateUrl: "./commoncontrols.component.html"
})
export class CommoncontrolsComponent extends AbstractSearchComponent implements OnInit {

  constructor(protected searchService: SearchService) {
    super(searchService)
  }

  ngOnInit(): void {
  }

}

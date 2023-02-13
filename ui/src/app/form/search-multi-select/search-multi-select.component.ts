import {Component, OnInit} from "@angular/core"
import {KeywordSearchService} from "../../richskill/service/keyword-search.service"
import {
  FormFieldSearchMultiSelectComponent
} from "../form-field-search-select/mulit-select/form-field-search-multi-select.component"

@Component({
  selector: "app-search-multi-select",
  templateUrl: "./search-multi-select.component.html",
  styleUrls: ["./search-multi-select.component.scss"]
})
export class SearchMultiSelectComponent extends FormFieldSearchMultiSelectComponent implements OnInit {

  constructor(protected searchService: KeywordSearchService) {
    super(searchService)
  }

  ngOnInit(): void {
    super.ngOnInit()
    this.performInitialSearchAndPopulation()
  }

}

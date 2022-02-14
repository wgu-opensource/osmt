import {ApiAdvancedSearch} from "../../richskill/service/rich-skill-search.service";
import {ILibrarySummary} from "./ApiLibrary";

export class ApiSearchHubAdvancedSearch extends ApiAdvancedSearch {
  libraries?: ILibrarySummary[]

  static factory(options: ApiSearchHubAdvancedSearch): ApiSearchHubAdvancedSearch {
    return Object.assign(new ApiSearchHubAdvancedSearch(), options)
  }
}

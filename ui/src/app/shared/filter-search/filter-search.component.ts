import { ApiJobCode } from "../../metadata/job-codes/Jobcode"
import {ApiNamedReference} from "../../richskill/ApiSkill"


export class FilterSearchComponent {

  areResultsEqual(i: ApiJobCode | ApiNamedReference, result: ApiJobCode | ApiNamedReference): boolean {
    if (i instanceof  ApiNamedReference && result instanceof  ApiNamedReference) {
      return i.name === result.name
    } else if (i instanceof  ApiJobCode && result instanceof  ApiJobCode) {
      return i.code === result.code
    }
    return false
  }

  resultName(result: any): string {
    return result.name ?? result.code + " " + result.targetNodeName
  }

}

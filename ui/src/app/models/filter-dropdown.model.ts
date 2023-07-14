import {ApiNamedReference} from "../richskill/ApiSkill"
import { ApiJobCode } from "../metadata/job-code/Jobcode"

export interface FilterDropdown {
  categories: ApiNamedReference[]
  keywords: ApiNamedReference[]
  standards: ApiNamedReference[]
  alignments: ApiNamedReference[]
  certifications: ApiNamedReference[]
  occupations: ApiJobCode[]
  employers: ApiNamedReference[]
  authors: ApiNamedReference[]
}

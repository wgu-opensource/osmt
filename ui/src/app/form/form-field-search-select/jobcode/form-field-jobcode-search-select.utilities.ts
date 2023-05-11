import {IJobCode} from "../../../metadata/job-codes/Jobcode"

export const areSearchResultsEqual = (result1: IJobCode, result2: IJobCode): boolean => {
  if (result1 && result2) {
    return result1.targetNodeName === result2.targetNodeName && result1.code === result1.code
  }

  return false
}

export const isJobCode = (value: any): value is IJobCode => {
  return value instanceof Object && "targetNodeName" in value && "code" in value
}

export const labelFor = (value: IJobCode): string|null => {
  return (value) ? `${value.code}|${value.targetNodeName}` : null
}

export const searchResultFromString = (value: string): IJobCode|undefined => {
  return undefined
}

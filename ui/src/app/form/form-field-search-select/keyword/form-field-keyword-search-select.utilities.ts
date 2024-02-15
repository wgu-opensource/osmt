import {ApiAlignment, ApiNamedReference, IAlignment, INamedReference, KeywordType} from "../../../richskill/ApiSkill"

export const ALIGNMENT_KEYWORD_TYPES: KeywordType[] = [
  KeywordType.Alignment,
  KeywordType.Standard
]

export const NAMED_REFERENCE_KEYWORD_TYPES: KeywordType[] = [
  KeywordType.Certification,
  KeywordType.Employer
]

export const STRING_KEYWORD_TYPES: KeywordType[] = [
  KeywordType.Author,
  KeywordType.Category,
  KeywordType.Keyword
]

export const areAlignmentsEqual = (value1: IAlignment, value2: IAlignment): boolean => {
  const v1 = new ApiAlignment(value1)
  const v2 = new ApiAlignment(value2)
  return v1?.equals(v2) ?? false
}

export const isAlignment = (value: any): value is IAlignment => {
  return value instanceof Object && "id" in value && "skillName" in value
}

export const isAlignmentKeywordType = (type: KeywordType): boolean => {
  return type && !!ALIGNMENT_KEYWORD_TYPES.find(t => t === type)
}

export const labelForAlignment = (value: IAlignment): string|null => {
  return value?.skillName ?? value?.id ?? null
}

export const searchServiceResultToAlignment = (
  result: INamedReference|string|undefined
): IAlignment|undefined => {
  if (!!result) {
    if (isNamedReference(result)) {
      if (result.name) {
        return ApiAlignment.fromString(result.name)
      }
    } else {
      return ApiAlignment.fromString(result)
    }
  }

  return undefined
}

export const areNamedReferencesEqual = (value1: INamedReference, value2: INamedReference): boolean => {
  const v1 = new ApiNamedReference(value1)
  const v2 = new ApiNamedReference(value2)
  return v1?.equals(v2) ?? false
}

export const isNamedReference = (value: any): value is INamedReference => {
  return value instanceof Object && "id" in value && "name" in value
}

export const isNamedReferenceKeywordType = (type: KeywordType): boolean => {
  return type && !!NAMED_REFERENCE_KEYWORD_TYPES.find(t => t === type)
}

export const labelForNamedReference = (value: INamedReference): string|null => {
  return value?.name ?? value?.id ?? null
}

export const searchServiceResultToNamedReference = (
  result: INamedReference|string|undefined
): INamedReference|undefined => {
  if (!!result) {
    return (isNamedReference(result)) ? result : ApiNamedReference.fromString(result)
  }

  return undefined
}

export const areStringKeywordsEqual = (value1: string, value2: string): boolean => {
  return value1 === value2
}

export const isStringKeyword = (value: any): value is string => {
  return typeof value === "string"
}

export const isStringKeywordKeywordType = (type: KeywordType): boolean => {
  return type && !!STRING_KEYWORD_TYPES.find(t => t === type)
}

export const labelForStringKeyword = (value: string): string|null => {
  return value ?? null
}

export const searchServiceResultToStringKeyword = (
  result: INamedReference|string|undefined
): string|undefined => {
  if (!!result) {
    return (isNamedReference(result)) ? result.name : result
  }

  return undefined
}

export const areSearchResultsEqual = (
  result1: IAlignment|INamedReference|string,
  result2: IAlignment|INamedReference|string
): boolean => {
  if (!result1 || !result2) {
    return result1 === result2
  }

  if (isAlignment(result1) && isAlignment(result2)) {
    return areAlignmentsEqual(result1, result2)
  }

  if (isNamedReference(result1) && isNamedReference(result2)) {
    return areNamedReferencesEqual(result1, result2)
  }

  if (isStringKeyword(result1) && isStringKeyword(result2)) {
    return areStringKeywordsEqual(result1, result2)
  }

  return false
}

export const labelFor = (value: IAlignment|INamedReference|string): string|null => {
  if (isAlignment(value)) {
    return labelForAlignment(value)
  }

  if (isNamedReference(value)) {
    return labelForNamedReference(value)
  }

  if (isStringKeyword(value)) {
    return labelForStringKeyword(value)
  }

  return null
}

export const searchResultFromString = (
  keywordType: KeywordType,
  value: string
): IAlignment|INamedReference|string|undefined => {
  if (!keywordType || !value) {
    return  undefined
  }

  if (isAlignmentKeywordType(keywordType)) {
    return ApiAlignment.fromString(value) ?? undefined
  }

  if (isNamedReferenceKeywordType(keywordType)) {
    return ApiNamedReference.fromString(value) ?? undefined
  }

  if (isStringKeywordKeywordType(keywordType)) {
    return (value.length > 0) ? value : undefined
  }

  return undefined
}

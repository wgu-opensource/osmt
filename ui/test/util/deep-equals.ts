import * as _ from "lodash"

// This is a deep equality check, but it does *not* check type equality of the outermost layer.
// This allows for object equality when comparing an interface instantiation with a concrete instantiation.
// tslint:disable-next-line:no-any
export function deepEqualSkipOuterType(thiz: any, that: any): boolean {
  for (const key of Object.getOwnPropertyNames(thiz)) {
    const value = that[key]

    if (!_.isEqual(thiz[key], value)) {
      return false
    }
  }
  return true
}

// tslint:disable-next-line:no-any
export function mismatched(o1: any, o2: any): string {
  return `Mismatch: expected != actual (${JSON.stringify(o1)} != ${JSON.stringify(o2)})`
}

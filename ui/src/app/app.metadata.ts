declare function require(moduleName: string): any;

export const appMetadata = {
  package: require("../../package.json")
}

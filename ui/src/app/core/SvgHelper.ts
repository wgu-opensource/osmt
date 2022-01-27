export enum SvgIcon {
  ADD = "icon-add",
  ARCHIVE = "icon-archive",
  UNARCHIVE = "icon-unarchive",
  CANCEL = "icon-cancel",
  CHECK = "icon-check",
  CHECK_OUTLINE = "icon-check-outline",
  CHEVRON = "icon-chevron",
  COLLECTION = "icon-collection",
  DISMISS = "icon-dismiss",
  DOC = "icon-doc",
  DOWNLOAD = "icon-download",
  DUPLICATE = "icon-duplicate",
  EDIT = "icon-edit",
  ICON_UP = "icon-up",
  MORE = "icon-more",
  PUBLISH = "icon-publish",
  SEARCH = "icon-search",
  SHARE = "icon-share",
  UNSHARE = "icon-unshare"
}

export class SvgHelper {

  // To use this in an html template, set path as a property in the component and reference:
  //  [attr.xlink:href]="iconPathProperty"
  public static path(icon: SvgIcon): string {
    return `assets/images/svg-defs.svg#${icon}`
  }
}

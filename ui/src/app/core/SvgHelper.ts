export enum SvgIcon {
  ADD = "icon-add",
  ARCHIVE = "icon-archive",
  CHECK = "icon-check",
  CHEVRON = "icon-chevron",
  DISMISS = "icon-dismiss",
  DOWNLOAD = "icon-download",
  DUPLICATE = "icon-duplicate",
  EDIT = "icon-edit",
  ICON_UP = "icon-up",
  MORE = "icon-more",
  PUBLISH = "icon-publish"
}

export class SvgHelper {

  // To use this in an html template, set path as a property in the component and reference:
  //  [attr.xlink:href]="iconPathProperty"
  public static path(icon: SvgIcon): string {
    return `assets/images/svg-defs.svg#${icon}`
  }
}

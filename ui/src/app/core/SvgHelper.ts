export enum SvgIcon {
  DOWNLOAD = "icon-download",
  DISMISS = "icon-dismiss",
  DUPLICATE = "icon-duplicate",
  EDIT = "icon-edit",
  PUBLISH = "icon-publish",
  ARCHIVE = "icon-archive",
  ICON_UP = "icon-up"
}

export class SvgHelper {

  // To use this in an html template, set path as a property in the component and reference:
  //  [attr.xlink:href]="iconPathProperty"
  public static path(icon: SvgIcon): string {
    return `assets/images/svg-defs.svg#${icon}`
  }
}

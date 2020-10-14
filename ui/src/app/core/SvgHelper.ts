export enum SvgIcon {
  DOWNLOAD = "icon-download",
  DISMISS = "icon-dismiss",
  DUPLICATE = "icon-duplicate",
  EDIT = "icon-edit",
  PUBLISH = "icon-publish",
  ARCHIVE = "icon-archive"
}

export class SvgHelper {
  public static path(icon: SvgIcon): string {
    return `assets/images/svg-defs.svg#${icon}`
  }
}


export class QuickLinksHelper {
  focusAndScrollIntoView(target: HTMLElement, tagName?: string): boolean {
    const t = (tagName !== undefined) ? (target.getElementsByTagName(tagName)[0] as HTMLElement ?? target) : target
    console.log("found target!", t)
    if (t) {
      t.focus()
      t.scrollIntoView()
    }
    return false
  }
}

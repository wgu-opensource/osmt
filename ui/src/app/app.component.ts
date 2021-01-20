import {Component, OnInit} from "@angular/core"
import {Title} from "@angular/platform-browser"
import {Whitelabelled} from "../whitelabel"
import {ToastService} from "./toast/toast.service"
import {DEFAULT_INTERRUPTSOURCES, Idle} from "@ng-idle/core"
import {Keepalive} from "@ng-idle/keepalive"
import {NavigationEnd, Router} from "@angular/router"
import * as chroma from "chroma-js"
import {SearchService} from "./search/search.service"

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html"
})
export class AppComponent extends Whitelabelled implements OnInit {
  blockingLoaderVisible = false

  public constructor(
    private titleService: Title,
    private toastService: ToastService,
    private searchService: SearchService,
    private idle: Idle,
    private keepalive: Keepalive,
    private router: Router
  ) {
    super()

    this.watchForIdle()

    this.toastService.loaderSubject.subscribe(visible =>  {
      this.blockingLoaderVisible = visible
    })
  }

  ngOnInit(): void {
    this.titleService.setTitle(this.whitelabel.toolName)

    if (this.whitelabel.colorBrandAccent1) {
      this.setWhiteLabelColor(this.whitelabel.colorBrandAccent1)
    }

    this.initClearSearchOnNavigate()
  }

  initClearSearchOnNavigate(): void {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd && !this.isCurrentViewSearchResults()) {
        this.searchService.clearSearch()
      }
    })
  }

  /**
   * Return true if the current url is one of the common control search results pages:
   * /collections/search or /skills/search
   */
  private isCurrentViewSearchResults(): boolean {
    const pattern = /.*(?:collections|skills)\/search.*/
    const currentUrl = this.router.url
    return pattern.test(currentUrl)
  }

  watchForIdle(): void {
    this.idle.setIdle(this.whitelabel.idleTimeoutInSeconds)
    this.idle.setTimeout(1)
    this.idle.setInterrupts(DEFAULT_INTERRUPTSOURCES)

    this.idle.onTimeout.subscribe(() => {
      console.log("Idle time out!")
      this.router.navigate(["/logout"], {queryParams: {timeout: true}})
    })
    this.keepalive.interval(15)
    this.idle.watch()
  }


  setWhiteLabelColor(newBrandAccent1: string): void {
    // Set new brand color
    document.documentElement.style.setProperty("--color-brand1", newBrandAccent1)

    // Check other colors
    const rootStyles = window.getComputedStyle(document.documentElement)
    const black = rootStyles.getPropertyValue("--color-onBrandBlack")
    const defaultA11yOnBrand = rootStyles.getPropertyValue("--color-onBrandWhite")
    const contrast = chroma.contrast(defaultA11yOnBrand, newBrandAccent1)

    if (contrast < 4.5) { // If white and brand have a contrast of less than 4.5, switch, else set default
      document.documentElement.style.setProperty("--color-a11yOnBrand", black)
    } else {
      document.documentElement.style.setProperty("--color-a11yOnBrand", defaultA11yOnBrand)
    }

  }
}

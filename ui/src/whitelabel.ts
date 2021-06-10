import {IAppConfig} from "./app/models/app-config.model"
import {AppConfig} from "./app/app.config"
import * as chroma from "chroma-js";

export class Whitelabelled {
  get whitelabel(): IAppConfig {
    return AppConfig.settings
  }

  get isBrandColorContrasted(): boolean {
    const rootStyles = window.getComputedStyle(document.documentElement)
    const colorBrand1 = rootStyles.getPropertyValue("--color-brand1")
    const defaultA11yOnBrand = rootStyles.getPropertyValue("--color-onBrandWhite")
    const contrast = chroma.contrast(defaultA11yOnBrand, colorBrand1)
    return contrast < 4.5  // If white and brand have a contrast of less than 4.5
  }

  get siteLogoUrl(): string {
    return (this.whitelabel.siteLogoUrl ? this.whitelabel.siteLogoUrl : "/assets/images/logo-dark.svg")
  }
}


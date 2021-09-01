import { Injectable } from "@angular/core"
import { environment } from "../../environments/environment"
import { appMetadata } from "../app.metadata"
import { IAppConfig } from "../models/app-config.model"


@Injectable()
/* tslint:disable:no-any */
export class EnvironmentService {

  public environment: IAppConfig;

  constructor() {
    console.log("OSMT-UI Version: ", appMetadata.package.version)

    if // there is an externally provided env use it
    ((window as any).__env) {
      this.environment = ((window as any).__env) as IAppConfig
      console.log("External environment config used")
    } else if (!environment.production) {  // This should only be pulled in during 'ng test'.
      this.environment = environment as any
      console.warn(">>>>> Testing environment config used <<<<<")
    } else { // use local default environment
      throw new Error("Missing env script!")
    }
  }
}

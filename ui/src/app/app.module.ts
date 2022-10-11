import {BrowserModule, Title} from "@angular/platform-browser"
import {APP_INITIALIZER, NgModule} from "@angular/core"
import {CommonModule} from "@angular/common"
import {HttpClientModule} from "@angular/common/http"
import {ReactiveFormsModule} from "@angular/forms"
import {NgIdleKeepaliveModule} from "@ng-idle/keepalive"
import {CoreModule} from "./core/core.module";
import {AppRoutingModule} from "./app-routing.module"
import {AppComponent} from "./app.component"
import {AppConfig} from "./app.config";
import {AuthService} from "./auth/auth-service";
import {AuthGuard} from "./auth/auth.guard";

export function initializeApp(
  appConfig: AppConfig,
  authService: AuthService
): () => void {
  // AppConfig.settings is initialized lazily (on the next line), but authService must be initialized sooner.
  authService.init()
  return () => appConfig.load()
}

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    NgIdleKeepaliveModule.forRoot(),
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    CommonModule,
    CoreModule
  ],
  providers: [
    AuthService,
    AuthGuard,
    { provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [AppConfig, AuthService], multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}

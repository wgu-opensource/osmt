import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { environment } from './environments/environment';

if (environment.production) {
  enableProdMode();
}

// Load runtime config and attach to window.__env before bootstrapping.
// If fetch fails, fall back to compiled environment.
fetch('/config/config.json')
  .then(res => {
    if (!res.ok) {
      throw new Error('no runtime config');
    }
    return res.json();
  })
  .then(cfg => {
    (window as any).__env = cfg;
  })
  .catch(() => {
    // ignore and use compiled environment
  })
  .finally(() => {
    platformBrowserDynamic()
      .bootstrapModule(AppModule)
      .catch(err => console.error(err));
  });

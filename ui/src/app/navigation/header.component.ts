import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { Whitelabelled } from '../../whitelabel';
import { AuthService } from '../auth/auth-service';
import { ActivatedRoute, Router } from '@angular/router';
import { AppConfig } from '../app.config';
import { ButtonAction } from '../auth/auth-roles';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
})
export class HeaderComponent extends Whitelabelled implements OnInit {
  menuExpanded = false;

  get canHaveWorkspace(): boolean {
    return this.authService.isEnabledByRoles(ButtonAction.MyWorkspace);
  }

  get canSyncManage(): boolean {
    return this.authService.isEnabledByRoles(ButtonAction.SyncManage);
  }

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private location: Location
  ) {
    super();
  }

  ngOnInit(): void {}

  isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  showPublicNavbar(): boolean {
    const url = this.location.path();
    const pattern = /(api\/)?(skills|collections)\/[-0-9a-f]{36}$/; // exclude public canonical URL paths
    return !this.isAuthenticated() || url.match(pattern) !== null;
  }

  handleClickMenu(): boolean {
    this.menuExpanded = !this.menuExpanded;
    return false;
  }

  get categoriesActive(): boolean {
    return window.location.pathname.startsWith('/categories');
  }

  get collectionsActive(): boolean {
    return window.location.pathname.startsWith('/collections');
  }

  get myWorkspaceActive(): boolean {
    return this.router.url.startsWith('/my-workspace');
  }

  get syncActive(): boolean {
    return this.router.url.startsWith('/admin/sync');
  }

  get skillsActive(): boolean {
    return this.router.url.startsWith('/skills');
  }

  handleQuicklink(elementId: string): boolean {
    const el = document.getElementById(elementId) as HTMLElement;
    if (el) {
      el.focus();
      el.scrollIntoView();
    }

    return false;
  }

  getCurrentUrl(): string {
    return this.location.path();
  }

  shouldShowSearchBar(): boolean {
    const url = this.location.path();
    if (url.startsWith('/admin/sync')) {
      return false;
    }
    if (!this.showPublicNavbar()) {
      return true; // Show for authenticated users
    }
    // Show for unauthenticated users on skills page
    return (
      !this.isAuthenticated() &&
      (url === '/skills' || url.startsWith('/skills?'))
    );
  }
}

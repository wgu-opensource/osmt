import {Component, OnInit} from "@angular/core"
import {Router} from "@angular/router"
import {ToastService} from "../../toast/toast.service"
import {Title} from "@angular/platform-browser"
import {AuthService} from "../../auth/auth-service"
import {CategoryListComponent} from "../list/category-list.component"
import {CategoryService} from "../service/category.service"

@Component({
  selector: "app-categories",
  templateUrl: "../list/category-list.component.html"
})
export class CategoryLibraryComponent extends CategoryListComponent implements OnInit {
  title = "Categories"

  constructor(
    protected router: Router,
    protected categoryService: CategoryService,
    protected toastService: ToastService,
    protected titleService: Title,
    protected authService: AuthService
  ) {
    super(router, categoryService, toastService, authService)
  }

  ngOnInit(): void {
    this.titleService.setTitle(`Categories | ${this.whitelabel.toolName}`)
    this.loadNextPage()
  }
}

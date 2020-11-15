import {Component, OnInit} from "@angular/core"
import {Location} from "@angular/common"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {AbstractControl, FormControl, FormGroup, Validators} from "@angular/forms"
import {AppConfig} from "../../app.config"
import {CollectionService} from "../service/collection.service"
import {ActivatedRoute, Router} from "@angular/router";
import {ApiCollection, ICollectionUpdate} from "../ApiCollection";
import {Observable} from "rxjs";
import {ToastService} from "../../toast/toast.service";
import {Title} from "@angular/platform-browser";
import {ApiNamedReference, INamedReference} from "../../richskill/ApiSkill";

@Component({
  selector: "app-create-collection",
  templateUrl: "./collection-form.component.html"
})
export class CollectionFormComponent implements OnInit {

  collectionForm = new FormGroup(this.getFormDefinitions())
  collectionUuid: string | null = null
  existingCollection?: ApiCollection

  collectionLoaded?: Observable<ApiCollection>
  collectionSaved?: Observable<ApiCollection>

  iconCollection = SvgHelper.path(SvgIcon.COLLECTION)

  constructor(
    private collectionService: CollectionService,
    private loc: Location,
    private router: Router,
    private route: ActivatedRoute,
    private toastService: ToastService,
    private titleService: Title
  ) { }

  ngOnInit(): void {
    this.collectionUuid = this.route.snapshot.paramMap.get("uuid")

    if (this.collectionUuid) {
      this.collectionLoaded = this.collectionService.getCollectionByUUID(this.collectionUuid)
      this.collectionLoaded.subscribe(collection => { this.setCollection(collection) })
    }

    if (this.isAuthorEditable()) {
      this.collectionForm.controls.author.setValue(AppConfig.settings.defaultAuthorValue)
    }

    this.titleService.setTitle(this.pageTitle())
  }

  pageTitle(): string {
    return `${this.collectionUuid !== null ? "Edit" : "Create"} Collection`
  }

  getFormDefinitions(): {[key: string]: AbstractControl} {
    const fields = {
      collectionName: new FormControl(""),
    }
    if (this.isAuthorEditable()) {
      // @ts-ignore
      fields.author = new FormControl(AppConfig.settings.defaultAuthorValue, Validators.required)
    }
    return fields
  }

  namedReferenceForString(value: string): ApiNamedReference | undefined {
    const str = value.trim()
    if (str.length < 1) {
      return undefined
    } else if (str.indexOf("://") !== -1) {
      return new ApiNamedReference({id: str})
    } else {
      return new ApiNamedReference({name: str})
    }
  }
  stringFromNamedReference(ref?: INamedReference): string {
    return ref?.name ?? ref?.id ?? ""
  }

  setCollection(collection: ApiCollection): void {
    this.existingCollection = collection
    const fields = {
      collectionName: collection.name
    }
    if (AppConfig.settings.editableAuthor) {
      // @ts-ignore
      fields.author = this.stringFromNamedReference(collection.author)
    }
    this.collectionForm.setValue(fields)
  }

  isAuthorEditable(): boolean {
    return AppConfig.settings.editableAuthor
  }

  updateObject(): ICollectionUpdate {
    const formValues = this.collectionForm.value
    return {
      name: formValues.collectionName,
      author: this.namedReferenceForString(formValues.author ?? ""),
    }
  }

  handleSaved(): void {
    const updateObject = this.updateObject()

    if (this.collectionUuid) {
      this.collectionSaved = this.collectionService.updateCollection(this.collectionUuid, updateObject)
    } else {
      this.collectionSaved = this.collectionService.createCollection(updateObject)
    }

    if (this.collectionSaved) {
      this.collectionSaved.subscribe(collection => {
        this.collectionForm.markAsPristine()
        this.router.navigate([`/collections/${collection.uuid}/manage`])
      })
    }
  }

  handleCancel(): void {
    this.loc.back()
  }
}

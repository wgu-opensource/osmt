import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { SvgHelper, SvgIcon } from '../../core/SvgHelper';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  Validators,
} from '@angular/forms';
import { AppConfig } from '../../app.config';
import { CollectionService } from '../service/collection.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiCollection, ICollectionUpdate } from '../ApiCollection';
import { Observable } from 'rxjs';
import { ToastService } from '../../toast/toast.service';
import { Title } from '@angular/platform-browser';
import { ApiNamedReference } from '../../richskill/ApiSkill';
import { HasFormGroup } from '../../core/abstract-form.component';
import { Whitelabelled } from '../../../whitelabel';
import { notACopyValidator } from '../../validators/not-a-copy.validator';
import { generateDuplicateName } from '../../core/duplicate-name.utils';

@Component({
  selector: 'app-create-collection',
  templateUrl: './collection-form.component.html',
})
export class CollectionFormComponent
  extends Whitelabelled
  implements OnInit, HasFormGroup
{
  collectionForm = new FormGroup(this.getFormDefinitions());
  collectionUuid: string | null = null;
  existingCollection?: ApiCollection;
  isDuplicating = false;

  collectionLoaded?: Observable<ApiCollection>;
  collectionSaved?: Observable<ApiCollection>;

  iconCollection = SvgHelper.path(SvgIcon.COLLECTION);

  get nameLabel(): string {
    return this.collectionUuid ? 'Collection Name' : 'New Collection Name';
  }

  constructor(
    protected collectionService: CollectionService,
    protected loc: Location,
    protected router: Router,
    protected route: ActivatedRoute,
    protected toastService: ToastService,
    protected titleService: Title
  ) {
    super();
  }

  formGroup(): FormGroup {
    return this.collectionForm;
  }

  ngOnInit(): void {
    this.collectionUuid = this.route.snapshot.paramMap.get('uuid');
    const routeUrl = this.router.url ?? '';
    this.isDuplicating = routeUrl.indexOf('/duplicate') >= 0;

    if (this.isDuplicating) {
      const nameCtrl = this.collectionForm.controls.collectionName;
      nameCtrl.setValidators(
        Validators.compose([Validators.required, notACopyValidator])
      );
      nameCtrl.updateValueAndValidity({ emitEvent: false });
    }

    if (this.collectionUuid) {
      this.collectionLoaded = this.collectionService.getCollectionByUUID(
        this.collectionUuid
      );
      this.collectionLoaded.subscribe(collection => {
        this.setCollection(collection);
      });
    }

    if (!this.isDuplicating) {
      this.collectionForm.controls.author.setValue(
        AppConfig.settings.defaultAuthorValue
      );
    }

    this.titleService.setTitle(
      `${this.pageTitle()} | ${this.whitelabel.toolName}`
    );
  }

  get collectionNameErrorMessage(): string {
    const ctrl = this.collectionForm.controls.collectionName;
    return ctrl?.hasError('notACopy')
      ? 'Change the name to remove the "(Copy ...)" suffix'
      : 'Name required';
  }

  pageTitle(): string {
    if (this.isDuplicating) {
      return 'Duplicate Collection';
    }
    return `${this.collectionUuid !== null ? 'Edit' : 'Create'} Collection`;
  }

  getFormDefinitions(): { [key: string]: AbstractControl } {
    const fields = {
      collectionName: new FormControl('', Validators.required),
      description: new FormControl(''),
      author: new FormControl(
        AppConfig.settings.defaultAuthorValue,
        Validators.required
      ),
    };
    return fields;
  }

  namedReferenceForString(value: string): ApiNamedReference | undefined {
    const str = value.trim();
    if (str.length < 1) {
      return undefined;
    } else if (str.indexOf('://') !== -1) {
      return new ApiNamedReference({ id: str });
    } else {
      return new ApiNamedReference({ name: str });
    }
  }

  setCollection(collection: ApiCollection): void {
    this.existingCollection = collection;
    const fields = {
      collectionName: this.isDuplicating
        ? generateDuplicateName(collection.name)
        : collection.name,
      description: collection.description ?? '',
      author: collection.author ?? AppConfig.settings.defaultAuthorValue,
    };
    this.collectionForm.setValue(fields);
    if (this.isDuplicating) {
      this.collectionForm.controls.collectionName.markAsTouched();
    }
  }

  isAuthorEditable(): boolean {
    return AppConfig.settings.editableAuthor;
  }

  updateObject(): ICollectionUpdate {
    const formValues = this.collectionForm.value;
    return {
      name: formValues.collectionName,
      description: formValues.description,
      author: formValues.author,
    };
  }

  onSubmit(): void {
    const updateObject = this.updateObject();

    if (this.isDuplicating && this.collectionUuid) {
      this.collectionSaved = this.collectionService.duplicateCollection(
        this.collectionUuid,
        updateObject
      );
    } else if (this.collectionUuid) {
      this.collectionSaved = this.collectionService.updateCollection(
        this.collectionUuid,
        updateObject
      );
    } else {
      this.collectionSaved =
        this.collectionService.createCollection(updateObject);
    }

    if (this.collectionSaved) {
      this.collectionSaved.subscribe(collection => {
        this.collectionForm.markAsPristine();
        this.router.navigate([`/collections/${collection.uuid}/manage`]);
      });
    }
  }

  handleCancel(): void {
    this.loc.back();
  }

  focusFormField(fieldName: string): boolean {
    const containerId = `formfield-container-${fieldName}`;
    const el = document.getElementById(containerId);
    if (el) {
      el.scrollIntoView();

      const fieldId = `formfield-${fieldName}`;
      const field = document.getElementById(fieldId);
      if (field) {
        field.focus();
      }
      return true;
    }
    return false;
  }

  scrollToTop(): boolean {
    this.focusFormField('collectionName');
    return false;
  }

  scrollToTopHidden(): boolean {
    const form = this.collectionForm;
    if (!form) {
      return false;
    }

    return !(form.touched && !form.pristine && form.valid);
  }

  handleClickMissingFields(): boolean {
    this.collectionForm.markAllAsTouched();
    for (const fieldName of ['collectionName', 'description', 'author']) {
      const control = this.collectionForm.controls[fieldName];
      if (control && !control.valid) {
        return this.focusFormField(fieldName);
      }
    }
    return false;
  }
}

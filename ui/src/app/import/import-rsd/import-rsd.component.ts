import {Component, Inject, LOCALE_ID} from '@angular/core';
import {RichSkillService} from "../../richskill/service/rich-skill.service";
import {ToastService} from "../../toast/toast.service";
import {CollectionService} from "../../collection/service/collection.service";

@Component({
  selector: 'app-import-rsd',
  template: ``
})
export class ImportRsdComponent {

  uuid = "";
  matchingQuery: string[] = [""];

  taskUuidInProgress: string | undefined;
  intervalHandle: number | undefined;

  constructor(
    protected collectionService: CollectionService,
    protected toastService: ToastService,
    @Inject(LOCALE_ID) protected locale: string
  ) {
  }



}

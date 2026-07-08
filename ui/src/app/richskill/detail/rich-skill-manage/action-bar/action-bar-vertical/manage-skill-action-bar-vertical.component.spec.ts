import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Component, Type } from '@angular/core';
import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { first } from 'rxjs/operators';
import { AppConfig } from 'src/app/app.config';
import { AuthService } from 'src/app/auth/auth-service';
import { EnvironmentService } from 'src/app/core/environment.service';
import { RichSkillService } from 'src/app/richskill/service/rich-skill.service';
import { ToastService } from 'src/app/toast/toast.service';
import {
  AuthServiceStub,
  CollectionServiceStub,
  RichSkillServiceStub,
} from 'test/resource/mock-stubs';
import { CollectionService } from 'src/app/collection/service/collection.service';
import { ManageSkillActionBarVerticalComponent } from './manage-skill-action-bar-vertical.component';
import any = jasmine.any;

@Component({
  template: ` <app-manage-skill-action-bar-vertical
    [skillUuid]="mySkillUuid"
    [skillName]="mySkillName"
    [skillPublicUrl]="mySkillPublicUrl"
    [archived]="myArchived"
    [published]="myPublished"
  >
  </app-manage-skill-action-bar-vertical>`,
})
class TestHostComponent {
  mySkillUuid = '1234';
  mySkillName = 'my skill name';
  mySkillPublicUrl = 'mockUrl';
  myArchived = false;
  myPublished: boolean | string = false;
}

export function createComponent(T: Type<TestHostComponent>): Promise<void> {
  hostFixture = TestBed.createComponent(T);
  hostComponent = hostFixture.componentInstance;

  const debugEl = hostFixture.debugElement.query(
    By.directive(ManageSkillActionBarVerticalComponent)
  );
  childComponent = debugEl.componentInstance;

  // 1st change detection triggers ngOnInit which gets a hero
  hostFixture.detectChanges();

  return hostFixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    hostFixture.detectChanges();
  });
}

let hostFixture: ComponentFixture<TestHostComponent>;
let hostComponent: TestHostComponent;
let childComponent: ManageSkillActionBarVerticalComponent;

describe('ManageSkillActionBarVerticalComponent', () => {
  let toastService: ToastService;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ManageSkillActionBarVerticalComponent, TestHostComponent],
      imports: [
        FormsModule, // Required for ([ngModel])
        RouterTestingModule, // Required for routerLink
        HttpClientTestingModule, // Needed to avoid the toolName race condition below
      ],
      providers: [
        EnvironmentService, // Needed to avoid the toolName race condition below
        AppConfig, // Needed to avoid the toolName race condition below
        ToastService,
        { provide: RichSkillService, useClass: RichSkillServiceStub },
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: CollectionService, useClass: CollectionServiceStub },
      ],
    }).compileComponents();

    const appConfig = TestBed.inject(AppConfig);
    AppConfig.settings = appConfig.defaultConfig(); // This avoids the race condition on reading the config's whitelabel.toolName

    toastService = TestBed.inject(ToastService);

    createComponent(TestHostComponent);
  }));

  afterEach(() => {
    AppConfig.settings.publicInstanceUrl = '';
    hostComponent.myPublished = false;
    hostComponent.mySkillPublicUrl = 'mockUrl';
  });

  it('should be created', () => {
    expect(hostComponent).toBeTruthy();
  });

  it('onAddToCollection should return', () => {
    // Arrange
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.stub();

    // Act
    childComponent.onAddToCollection();

    // Assert
    expect(router.navigate).toHaveBeenCalledWith(
      ['/collections/add-skills'],
      any(Object)
    );
  });

  it('publishLinkText should return', () => {
    // Arrange
    const expected = 'Publish';

    // Act
    const result = childComponent.publishLinkText();

    // Assert
    expect(result).toEqual(expected);
  });

  it('publishLinkDestination should return', () => {
    // Arrange
    const expected = '';

    // Act
    const result = childComponent.publishLinkDestination();

    // Assert
    expect(result).toEqual(expected);
  });

  it('handleArchive should return', () => {
    // Arrange
    let clicked = false;
    childComponent.reloadSkill.pipe(first()).subscribe(() => {
      clicked = true;
      return;
    });

    // Act
    childComponent.handleArchive();

    // Assert
    expect(clicked).toBeTruthy();
  });

  it('handleUnarchive should return', () => {
    // Arrange
    let clicked = false;
    childComponent.reloadSkill.pipe(first()).subscribe(() => {
      clicked = true;
      return;
    });

    // Act
    childComponent.handleUnarchive();

    // Assert
    expect(clicked).toBeTruthy();
  });

  it('handlePublish should return', () => {
    // Arrange
    let clicked = false;
    childComponent.reloadSkill.pipe(first()).subscribe(() => {
      clicked = true;
      return;
    });

    spyOn(window, 'confirm').and.returnValue(true);

    // Act
    childComponent.handlePublish();

    // Assert
    expect(clicked).toBeTruthy();
  });

  it('handlePublish opens published skill URL when already published', () => {
    hostComponent.myPublished = '2020-06-25T14:58:46.313Z';
    hostComponent.mySkillPublicUrl =
      'https://staff.example.com/api/skills/1234';
    AppConfig.settings.publicInstanceUrl = 'https://public.example.com';
    hostFixture.detectChanges();
    spyOn(window, 'open');
    childComponent.handlePublish();
    expect(window.open).toHaveBeenCalledWith(
      'https://public.example.com/api/skills/1234',
      '_blank'
    );
  });

  it('handleCopyPublicUrl should return', async () => {
    // Arrange
    const clipboardWriteTextSpy = spyOn(
      navigator.clipboard,
      'writeText'
    ).and.returnValue(Promise.resolve());
    const showToastSpy = spyOn(toastService, 'showToast').and.callFake(() => {
      return;
    });

    // Act
    childComponent.handleCopyPublicURL();

    await clipboardWriteTextSpy;

    // Assert
    expect(clipboardWriteTextSpy).toHaveBeenCalledWith('mockUrl');
    expect(showToastSpy).toHaveBeenCalledWith(
      'Success!',
      'URL copied to clipboard'
    );
  });

  it('handleAddToWorkspace adds skill uuid and shows toast', () => {
    const collectionService = TestBed.inject(CollectionService);
    const updateSpy = spyOn(
      collectionService,
      'updateSkillsWithResult'
    ).and.callThrough();
    spyOn(toastService, 'showBlockingLoader');
    spyOn(toastService, 'hideBlockingLoader');
    const showToastSpy = spyOn(toastService, 'showToast');

    childComponent.handleAddToWorkspace();

    expect(updateSpy).toHaveBeenCalled();
    const [, skillUpdate] = updateSpy.calls.mostRecent().args;
    expect(skillUpdate.add?.uuids).toEqual(['1234']);
    expect(showToastSpy).toHaveBeenCalledWith(
      'Success!',
      'You added 7 RSDs to the workspace.'
    );
  });
});

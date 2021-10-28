import { Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { ToastComponent } from "./toast.component"
import { ToastMessage, ToastService } from "./toast.service"

// An example of a component-level test

export function createComponent(T: Type<ToastComponent>): Promise<void> {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance

  // 1st change detection triggers ngOnInit which gets a hero
  fixture.detectChanges()

  return fixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    fixture.detectChanges()
  })
}


let component: ToastComponent
let fixture: ComponentFixture<ToastComponent>


describe("ToastComponent", () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        ToastComponent
      ],
      providers: [
        ToastService
        // { provide: ToastService, useClass: ToastServiceStub },
      ]
    })
    .compileComponents()

    createComponent(ToastComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("should set message", () => {
    const service: ToastService = TestBed.get(ToastService)
    const message: ToastMessage = {
      isAttention: true,
      message: "my first message",
      title: "message one"
    }
    service.showToast(message.title, message.message, message.isAttention)
    fixture.detectChanges()
    expect(component.message).toEqual(message)

    component.dismiss()
    fixture.detectChanges()
    expect(component.message).toBeFalsy()
  })

  it("should be visible/invisible", () => {
    const service: ToastService = TestBed.get(ToastService)
    const message: ToastMessage = {
      isAttention: true,
      message: "my first message",
      title: "message one"
    }
    service.showToast(message.title, message.message, message.isAttention)
    fixture.detectChanges()
    expect(component.isToastVisible()).toBeTruthy()

    component.dismiss()
    fixture.detectChanges()
    expect(component.isToastVisible()).toBeFalsy()
  })
})

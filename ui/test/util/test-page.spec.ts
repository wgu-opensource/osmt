import { ComponentFixture } from "@angular/core/testing"
import { Router } from "@angular/router"
// import * as jasmine from 'node_modules/jasmine';


/**
 * Reusable class for interacting with the DOM.
 *
 * The naming convention for element IDs is:
 *   componentName-elementType-elementName
 *
 * For example, on the EditUserComponent, there is an <button> with the title='Save User':
 *   editUser-button-saveUser
 */
export class TestPage<T> {
  protected fixture: ComponentFixture<T>

  // Getter properties wait to query the DOM until called.  For example:
  //   get buttons()     { return this.queryAll<HTMLButtonElement>('button'); }
  //   get lastName()   { return this.query<HTMLInputElement>('#component-input-lastName'); }


  navigateSpy: jasmine.Spy

  constructor(fixture: ComponentFixture<T>) {
    this.fixture = fixture

    // get the navigate spy from the injected router spy object
    // tslint:disable-next-line:no-angle-bracket-type-assertion no-any
    const routerSpy = <any> fixture.debugElement.injector.get(Router)
    this.navigateSpy = routerSpy.navigate
  }


      /* ----- Query Helpers ----- */

  protected query<R>(selector: string): R {
    return this.fixture.nativeElement.querySelector(selector)
  }

  protected queryAll<R>(selector: string): R[] {
    return this.fixture.nativeElement.querySelectorAll(selector)
  }
}

import { Component, DebugElement } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ClickOutsideDirective } from './click-outside.directive';

@Component({
  template: `
    <div class="container" clickOutside (clickOutside)="onClickOutside()">
      <button class="inside-button">Inside Button</button>
    </div>
    <button class="outside-button">Outside Button</button>
  `,
  standalone: true,
  imports: [ClickOutsideDirective]
})
class TestComponent {
  onClickOutside() {}
}

describe('ClickOutsideDirective', () => {
  let component: TestComponent;
  let fixture: ComponentFixture<TestComponent>;
  let directive: ClickOutsideDirective;
  let directiveElement: DebugElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;
    directiveElement = fixture.debugElement.query(By.directive(ClickOutsideDirective));
    directive = directiveElement.injector.get(ClickOutsideDirective);
    fixture.detectChanges();
  });

  it('should create an instance', () => {
    expect(directive).toBeTruthy();
  });

  it('should have clickOutside output', () => {
    expect(directive.clickOutside).toBeDefined();
    expect(directive.clickOutside.emit).toBeDefined();
  });

  describe('Click detection', () => {
    it('should emit clickOutside event when clicking outside the element', () => {
      spyOn(directive.clickOutside, 'emit');
      spyOn(component, 'onClickOutside');
      
      const outsideButton = fixture.debugElement.query(By.css('.outside-button'));
      outsideButton.nativeElement.click();
      
      expect(directive.clickOutside.emit).toHaveBeenCalled();
      expect(component.onClickOutside).toHaveBeenCalled();
    });

    it('should not emit clickOutside event when clicking inside the element', () => {
      spyOn(directive.clickOutside, 'emit');
      spyOn(component, 'onClickOutside');
      
      const insideButton = fixture.debugElement.query(By.css('.inside-button'));
      insideButton.nativeElement.click();
      
      expect(directive.clickOutside.emit).not.toHaveBeenCalled();
      expect(component.onClickOutside).not.toHaveBeenCalled();
    });

    it('should not emit clickOutside event when clicking on the element itself', () => {
      spyOn(directive.clickOutside, 'emit');
      spyOn(component, 'onClickOutside');
      
      const container = fixture.debugElement.query(By.css('.container'));
      container.nativeElement.click();
      
      expect(directive.clickOutside.emit).not.toHaveBeenCalled();
      expect(component.onClickOutside).not.toHaveBeenCalled();
    });
  });

  describe('Element reference', () => {
    it('should have access to the host element', () => {
      expect(directive['elementRef']).toBeDefined();
      expect(directive['elementRef'].nativeElement).toBeDefined();
    });

    it('should correctly identify the host element', () => {
      const hostElement = directive['elementRef'].nativeElement;
      expect(hostElement.classList.contains('container')).toBe(true);
    });
  });

  describe('Event handling', () => {
    it('should handle multiple outside clicks', () => {
      spyOn(directive.clickOutside, 'emit');
      spyOn(component, 'onClickOutside');
      
      const outsideButton = fixture.debugElement.query(By.css('.outside-button'));
      
      // Click multiple times
      outsideButton.nativeElement.click();
      outsideButton.nativeElement.click();
      outsideButton.nativeElement.click();
      
      expect(directive.clickOutside.emit).toHaveBeenCalledTimes(3);
      expect(component.onClickOutside).toHaveBeenCalledTimes(3);
    });

    it('should handle mixed inside and outside clicks', () => {
      spyOn(directive.clickOutside, 'emit');
      spyOn(component, 'onClickOutside');
      
      const insideButton = fixture.debugElement.query(By.css('.inside-button'));
      const outsideButton = fixture.debugElement.query(By.css('.outside-button'));
      
      // Click inside
      insideButton.nativeElement.click();
      expect(directive.clickOutside.emit).not.toHaveBeenCalled();
      
      // Click outside
      outsideButton.nativeElement.click();
      expect(directive.clickOutside.emit).toHaveBeenCalledTimes(1);
      
      // Click inside again
      insideButton.nativeElement.click();
      expect(directive.clickOutside.emit).toHaveBeenCalledTimes(1);
    });
  });

  describe('Edge cases', () => {
    it('should handle clicks on elements without content', () => {
      const emptyContainer = fixture.debugElement.query(By.css('.container'));
      emptyContainer.nativeElement.innerHTML = '';
      fixture.detectChanges();
      
      spyOn(directive.clickOutside, 'emit');
      
      const outsideButton = fixture.debugElement.query(By.css('.outside-button'));
      outsideButton.nativeElement.click();
      
      expect(directive.clickOutside.emit).toHaveBeenCalled();
    });

    it('should handle clicks on deeply nested elements inside', () => {
      const container = fixture.debugElement.query(By.css('.container'));
      container.nativeElement.innerHTML = `
        <div class="nested">
          <div class="deeply-nested">
            <button class="deep-button">Deep Button</button>
          </div>
        </div>
      `;
      fixture.detectChanges();
      
      spyOn(directive.clickOutside, 'emit');
      
      const deepButton = fixture.debugElement.query(By.css('.deep-button'));
      deepButton.nativeElement.click();
      
      expect(directive.clickOutside.emit).not.toHaveBeenCalled();
    });
  });
}); 
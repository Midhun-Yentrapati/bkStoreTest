import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FooterComponent } from './footer.component';
import { RouterTestingModule } from '@angular/router/testing';

describe('FooterComponent', () => {
  let component: FooterComponent;
  let fixture: ComponentFixture<FooterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FooterComponent, RouterTestingModule]
    }).compileComponents();

    fixture = TestBed.createComponent(FooterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('scrollToCategories', () => {
    it('should prevent default event behavior', () => {
      const mockEvent = { preventDefault: jasmine.createSpy() } as any;
      
      component.scrollToCategories(mockEvent);
      
      expect(mockEvent.preventDefault).toHaveBeenCalled();
    });

    it('should scroll to categories section when element exists', () => {
      const mockEvent = { preventDefault: jasmine.createSpy() } as any;
      const mockElement = { scrollIntoView: jasmine.createSpy() };
      
      spyOn(document, 'getElementById').and.returnValue(mockElement as any);
      
      component.scrollToCategories(mockEvent);
      
      expect(document.getElementById).toHaveBeenCalledWith('browse-categories');
      expect(mockElement.scrollIntoView).toHaveBeenCalledWith({ behavior: 'smooth' });
    });

    it('should handle case when categories section does not exist', () => {
      const mockEvent = { preventDefault: jasmine.createSpy() } as any;
      
      spyOn(document, 'getElementById').and.returnValue(null);
      
      expect(() => {
        component.scrollToCategories(mockEvent);
      }).not.toThrow();
      
      expect(document.getElementById).toHaveBeenCalledWith('browse-categories');
    });
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminDashboardCardsComponent } from './admin-dashboard-cards.component';

describe('AdminDashboardCardsComponent', () => {
  let component: AdminDashboardCardsComponent;
  let fixture: ComponentFixture<AdminDashboardCardsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminDashboardCardsComponent]
    }).compileComponents();
    fixture = TestBed.createComponent(AdminDashboardCardsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should accept input bindings', () => {
    component.title = 'Test Title';
    component.description = 'Test Description';
    fixture.detectChanges();
    expect(component.title).toBe('Test Title');
    expect(component.description).toBe('Test Description');
  });

  it('should emit cardClicked when onCardClick is called', () => {
    spyOn(component.cardClicked, 'emit');
    component.title = 'CardTitle';
    component.onCardClick();
    expect(component.cardClicked.emit).toHaveBeenCalledWith('CardTitle');
  });

  it('should emit cardClicked with empty string if title is empty', () => {
    spyOn(component.cardClicked, 'emit');
    component.title = '';
    component.onCardClick();
    expect(component.cardClicked.emit).toHaveBeenCalledWith('');
  });

  it('should have default input values', () => {
    expect(component.title).toBe('');
    expect(component.description).toBe('');
  });
});

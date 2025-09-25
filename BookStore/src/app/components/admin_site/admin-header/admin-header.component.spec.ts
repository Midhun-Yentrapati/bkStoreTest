import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AdminHeaderComponent } from './admin-header.component';

describe('AdminHeaderComponent', () => {
  let component: AdminHeaderComponent;
  let fixture: ComponentFixture<AdminHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminHeaderComponent, HttpClientTestingModule]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { AddressManagementComponent } from './address-management.component';

describe('AddressManagementComponent', () => {
  let component: AddressManagementComponent;
  let fixture: ComponentFixture<AddressManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddressManagementComponent, HttpClientTestingModule],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: { subscribe: () => {} }
          }
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddressManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

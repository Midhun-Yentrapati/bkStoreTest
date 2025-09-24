import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { CartComponent } from './cart.component';
import { CartService } from '../../../services/cart.service';
import { CartItemWithDetails } from '../../../services/cart.service';
import { BookModel } from '../../../models/book.model';

describe('CartComponent', () => {
  let component: CartComponent;
  let fixture: ComponentFixture<CartComponent>;
  let mockCartService: jasmine.SpyObj<CartService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockBook: BookModel = {
    id: '1',
    title: 'Test Book',
    author: 'Test Author',
    description: 'Test Description',
    categories: ['Fiction'],
    price: 29.99,
    stock_display: 10,
    stock_actual: 10,
    image_urls: ['test-image.jpg']
  };

  const mockCartItem: CartItemWithDetails = {
    id: '1',
    book: mockBook,
    bookId: '1',
    userId: 'user1',
    quantity: 2,
    addedAt: '2024-01-01'
  };

  beforeEach(async () => {
    mockCartService = jasmine.createSpyObj('CartService', ['removeFromCart', 'updateQuantity']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [CartComponent],
      providers: [
        { provide: CartService, useValue: mockCartService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CartComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should calculate total price correctly', () => {
    component.cartItems = [mockCartItem];
    
    component.calculateTotalPrice();
    
    expect(component.totalPrice).toBe(59.98); // 29.99 * 2
  });

  it('should validate promo code format', () => {
    component.promoCode = 'valid123';
    component.validatePromoCode();
    expect(component.promoCodeValid).toBeTrue();
    
    component.promoCode = 'invalid@code';
    component.validatePromoCode();
    expect(component.promoCodeValid).toBeFalse();
  });

  it('should apply valid promo code', fakeAsync(() => {
    component.promoCode = 'welcome10';
    component.promoCodeValid = true;
    
    component.applyPromoCode();
    
    // Wait for the timeout to complete
    tick(1000);
    
    expect(component.appliedPromoCode).toBe('welcome10');
  }));
}); 
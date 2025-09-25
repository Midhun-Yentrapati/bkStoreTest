import { Routes } from '@angular/router';
import { BookDetailComponent } from './components/customer_site/book-detail/book-detail.component';
import { HomeComponent } from './pages/home/home.component';
import { SearchResultsComponent } from './components/customer_site/search-results/search-results.component';
import { AboutComponent } from './components/customer_site/about/about.component';
import { ContactComponent } from './components/customer_site/contact/contact.component';
import { RegisterComponent } from './pages/register/register.component';
import { LoginComponent } from './pages/login/login.component';
import { ProfileComponent } from './pages/profile/profile.component';
import { SettingsComponent } from './pages/settings/settings.component';
import { CartComponent } from './components/customer_site/cart/cart.component';
import { WishlistComponent } from './components/customer_site/wishlist/wishlist.component';
import { AuthGuard } from './guards/auth.guard';

// Admin components imports
import { ViewAllComponent } from './components/customer_site/view-all/view-all.component';
import { AddComponent } from './components/admin_site/add/add.component';
import { EditPageComponent } from './components/admin_site/edit-page/edit-page.component';
import { AdminOrdersComponent } from './components/admin_site/admin-orders/admin-orders.component';
import { LowStockComponent } from './components/admin_site/low-stock/low-stock.component';
import { AdminLoginComponent } from './components/admin_site/admin-login/admin-login.component';
import { AdminSignupComponent } from './components/admin_site/admin-signup/admin-signup.component';
import { ForgotPasswordComponent } from './components/customer_site/forgot-password/forgot-password.component';
import { AdminDashboardMainComponent } from './components/admin_site/admin-dashboard-main/admin-dashboard-main.component';
import { CategoryManagementComponent } from './components/admin_site/category-management/category-management.component';
import { ViewUsersComponent } from './components/admin_site/view-users/view-users.component';
import { AddUserComponent } from './components/admin_site/add-user/add-user.component';
import { AdminLayoutComponent } from './components/admin_site/admin-layout/admin-layout.component';
import { AddressManagementComponent } from './components/customer_site/address-management/address-management.component';
import { CheckoutComponent } from './components/customer_site/checkout/checkout.component';
import { OrderHistoryComponent } from './components/customer_site/order-history/order-history.component';
import { OrderTrackingComponent } from './components/customer_site/order-tracking/order-tracking.component';
import { PaymentComponent } from './components/customer_site/payment/payment.component';
import { SectionManagementComponent } from './components/admin_site/section-management/section-management.component';
import { PrivacyPolicyComponent } from './components/customer_site/privacy-policy/privacy-policy.component';
import { TermsOfServiceComponent } from './components/customer_site/terms-of-service/terms-of-service.component';
import { CouponManagementComponent } from './components/admin_site/coupon-management/coupon-management.component';
import { ManageReviewsComponent } from './components/admin_site/manage-reviews/manage-reviews.component';

export const routes: Routes = [
  // Client-side routes
  { path: '', component: HomeComponent },
  { path: 'book/:id', component: BookDetailComponent, data: { renderMode: 'dynamic' } },
  { path: 'search/:query', component: SearchResultsComponent, data: { renderMode: 'dynamic' } },

  // E-commerce routes (protected)
  { path: 'cart', component: CartComponent, canActivate: [AuthGuard] },
  { path: 'wishlist', component: WishlistComponent, canActivate: [AuthGuard] },
  { path: 'checkout', component: CheckoutComponent, canActivate: [AuthGuard] },
  { path: 'payment', component: PaymentComponent, canActivate: [AuthGuard] },
  { path: 'address-management', component: AddressManagementComponent, canActivate: [AuthGuard] },
  { path: 'orders', component: OrderHistoryComponent, canActivate: [AuthGuard] },
  { path: 'track-order/:id', component: OrderTrackingComponent, canActivate: [AuthGuard], data: { renderMode: 'dynamic' } },

  // Information pages
  { path: 'about', component: AboutComponent },
  { path: 'contact', component: ContactComponent },
  { path: 'privacy-policy', component: PrivacyPolicyComponent },
  { path: 'terms-of-service', component: TermsOfServiceComponent },

  // Authentication routes  
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },

  // User-specific routes (protected)
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'settings', component: SettingsComponent, canActivate: [AuthGuard] },

  // Admin routes (prefixed with /admin)
  { path: 'admin', redirectTo: '/admin/login', pathMatch: 'full' },
  { path: 'admin/login', component: AdminLoginComponent },
  { path: 'admin/signup', component: AdminSignupComponent },
  { path: 'admin/forgot-password', component: ForgotPasswordComponent },
  
  // Protected admin routes with layout
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: 'dashboard', component: AdminDashboardMainComponent },
      { path: 'inventory', component: ViewAllComponent },
      { path: 'add-book', component: AddComponent },
      { path: 'edit-book/:id', component: EditPageComponent, data: { renderMode: 'dynamic' } },
      { path: 'categories', component: CategoryManagementComponent },
      { path: 'users', component: ViewUsersComponent },
      { path: 'add-user', component: AddUserComponent },
      { path: 'orders', component: AdminOrdersComponent },
      { path: 'low-stock', component: LowStockComponent },
      { path: 'section-management', component: SectionManagementComponent },
      { path: 'coupons', component: CouponManagementComponent },
      { path: 'reviews', component: ManageReviewsComponent },
      { path: 'jwt-test', loadComponent: () => import('./components/admin_site/jwt-test/jwt-test.component').then(m => m.JwtTestComponent) }
    ]
  },

  // Legacy admin redirects
  { path: 'admin-login', redirectTo: '/admin/login' },
  { path: 'admin-main', redirectTo: '/admin/dashboard' },
  { path: 'viewInventory', redirectTo: '/admin/inventory' },
  { path: 'view-all', redirectTo: '/admin/inventory' },
  { path: 'add', redirectTo: '/admin/add-book' },

  // Catch-all route - must be last
  { path: '**', redirectTo: '' }
];

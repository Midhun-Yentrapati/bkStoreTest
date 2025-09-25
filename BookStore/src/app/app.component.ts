import { Component, Renderer2, OnInit, Inject, PLATFORM_ID, HostListener, effect, OnDestroy } from '@angular/core';
import { Router, NavigationEnd, RouterOutlet } from '@angular/router';
import { HeaderComponent } from './components/customer_site/header/header.component';
import { NgIf, isPlatformBrowser } from '@angular/common';
import { filter } from 'rxjs/operators';
import { FooterComponent } from './components/customer_site/footer/footer.component';
import { LoginPromptComponent } from './components/customer_site/login-prompt/login-prompt.component';
import { NotificationComponent } from './components/customer_site/notification/notification.component';
import { AuthService } from './services/auth.service';



@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, FooterComponent, LoginPromptComponent, NotificationComponent, NgIf],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'BookVerse';

  showLoginPrompt = false;
  private hasUserInteracted = false;
  isAdminRoute = false;
  private routerSubscription: any; // Add this line to store the subscription

  constructor(
    private renderer: Renderer2, 
    @Inject(PLATFORM_ID) private platformId: Object,

    private authService: AuthService,
    private router: Router
  ) {
    // Use effect to monitor auth initialization for better SSR compatibility
    effect(() => {
      // Only show login prompt if:
      // 1. Auth service is initialized
      // 2. No user is logged in (neither customer nor admin)
      // 3. User hasn't interacted yet
      // 4. Not on admin route
      if (this.authService.isInitialized() && 
          !this.authService.isLoggedIn() && 
          !this.hasUserInteracted &&
          !this.isAdminRoute) {
        this.showLoginPrompt = true;
      } else {
        // Hide login prompt if user is logged in or on admin route
        this.showLoginPrompt = false;
      }
    });
  }

  ngOnInit(){

      
      // Subscribe to router events to detect admin routes
      this.routerSubscription = this.router.events.pipe(
        filter(event => event instanceof NavigationEnd)
      ).subscribe((event: NavigationEnd) => {
        this.isAdminRoute = event.urlAfterRedirects.startsWith('/admin');
        // Hide login prompt when navigating to admin routes
        if (this.isAdminRoute) {
          this.showLoginPrompt = false;
        }
      });
      
      // Check initial route
      this.isAdminRoute = this.router.url.startsWith('/admin');
      // Hide login prompt if starting on admin route
      if (this.isAdminRoute) {
        this.showLoginPrompt = false;
      }
    }

    ngOnDestroy(): void {
      if (this.routerSubscription) {
        this.routerSubscription.unsubscribe();
      }
    }

    // Listen for first user interaction (mouse movement, click, scroll, or key press)
    @HostListener('document:mousemove', ['$event'])
    @HostListener('document:click', ['$event'])
    @HostListener('document:scroll', ['$event'])
    @HostListener('document:keydown', ['$event'])
    onUserInteraction() {
      if (isPlatformBrowser(this.platformId) && 
          !this.hasUserInteracted && 
          !this.authService.isLoggedIn() && 
          !this.showLoginPrompt &&
          this.authService.isInitialized() &&
          !this.isAdminRoute) {
        this.showLoginPrompt = true;
        this.hasUserInteracted = true;
      }
    }

    closeLoginPrompt(){
      this.showLoginPrompt = false;
      this.hasUserInteracted = true; // Mark as interacted to prevent showing again
    }
  



}

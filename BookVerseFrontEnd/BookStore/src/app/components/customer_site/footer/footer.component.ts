import { Component } from '@angular/core';
import { RouterModule } from '@angular/router'; // 1. Import RouterModule

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterModule], // 2. Add RouterModule to the imports array
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent {
  
  scrollToCategories(event: Event): void {
    event.preventDefault();
    const categoriesSection = document.getElementById('browse-categories');
    if (categoriesSection) {
      categoriesSection.scrollIntoView({ behavior: 'smooth' });
    }
  }
}

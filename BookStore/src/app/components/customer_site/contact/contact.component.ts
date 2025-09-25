import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [FormsModule, NgIf],
  templateUrl: './contact.component.html',
  styleUrl: './contact.component.css'
})
export class ContactComponent {
  contactData = {
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    subject: '',
    message: '',
    newsletter: false
  };

  isSubmitting = false;

  onSubmit() {
    if (this.isSubmitting) return;
    
    this.isSubmitting = true;
    
    // Simulate form submission
    setTimeout(() => {
      console.log('Contact form submitted:', this.contactData);
      alert('Thank you for your message! We will get back to you soon.');
      
      // Reset form
      this.contactData = {
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        subject: '',
        message: '',
        newsletter: false
      };
      
      this.isSubmitting = false;
    }, 2000);
  }
}

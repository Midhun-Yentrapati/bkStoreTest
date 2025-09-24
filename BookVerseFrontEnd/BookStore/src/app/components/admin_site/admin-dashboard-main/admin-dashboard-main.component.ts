// src/app/admin-dashboard-main/admin-dashboard-main.component.ts
import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AdminDashboardPageComponent } from '../admin-dashboard-page/admin-dashboard-page.component';// Import Book and BookService

@Component({
  selector: 'app-admin-dashboard-main',
  standalone: true,
  imports: [CommonModule, RouterOutlet, AdminDashboardPageComponent],
  providers: [],
  templateUrl: './admin-dashboard-main.component.html',
  styleUrl: './admin-dashboard-main.component.css'
})
export class AdminDashboardMainComponent implements OnInit {
  username: string = '';


  constructor() {}

  ngOnInit(): void {
    this.username = sessionStorage.getItem('loggedInUsername') || 'Default Admin User';

    console.log('AdminDashboardMainComponent: Initializing.', this.username);
  }


}
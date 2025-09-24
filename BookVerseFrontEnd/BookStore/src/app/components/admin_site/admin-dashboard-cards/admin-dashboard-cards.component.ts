import { CommonModule } from '@angular/common';
import { Component, Input, Output,EventEmitter } from '@angular/core';

@Component({
  selector: 'app-admin-dashboard-cards',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard-cards.component.html',
  styleUrl: './admin-dashboard-cards.component.css'
})
export class AdminDashboardCardsComponent {
  @Input() title : string = '';
  @Input() description : string = '';
  @Output() cardClicked = new EventEmitter<string>();

  onCardClick() : void{
    this.cardClicked.emit(this.title);
  }
}

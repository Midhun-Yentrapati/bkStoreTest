import { Component, Input, Output, EventEmitter, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BookModel } from '../../../models/book.model';
import { ShareService } from '../../../services/share.service';

@Component({
  selector: 'app-share-modal',
  imports: [CommonModule],
  templateUrl: './share-modal.component.html',
  styleUrl: './share-modal.component.css'
})
export class ShareModalComponent implements OnInit, OnChanges {
  @Input() book: BookModel | null = null;
  @Input() isOpen: boolean = false;
  @Output() closeModal = new EventEmitter<void>();
  @Output() shareSuccess = new EventEmitter<string>();

  shareLink: string = '';
  isWebShareSupported: boolean = false;
  copySuccess: boolean = false;

  constructor(private shareService: ShareService) {}

  ngOnInit() {
    this.isWebShareSupported = this.shareService.isWebShareSupported();
    if (this.book) {
      this.shareLink = this.shareService.generateBookShareLink(this.book);
    }
  }

  ngOnChanges() {
    if (this.book) {
      this.shareLink = this.shareService.generateBookShareLink(this.book);
    }
  }

  async copyToClipboard() {
    if (this.book) {
      const success = await this.shareService.shareBookLink(this.book);
      if (success) {
        this.copySuccess = true;
        this.shareSuccess.emit('Link copied to clipboard!');
        setTimeout(() => {
          this.copySuccess = false;
        }, 2000);
      }
    }
  }

  async shareWithWebAPI() {
    if (this.book) {
      const success = await this.shareService.shareWithWebAPI(this.book);
      if (success) {
        this.shareSuccess.emit('Shared successfully!');
        this.closeModal.emit();
      }
    }
  }

  shareToSocial(platform: 'facebook' | 'twitter' | 'linkedin' | 'whatsapp') {
    if (this.book) {
      this.shareService.shareToSocialMedia(this.book, platform);
      this.shareSuccess.emit(`Shared to ${platform}!`);
    }
  }

  shareViaEmail() {
    if (this.book) {
      const emailUrl = this.shareService.generateEmailShareUrl(this.book);
      window.location.href = emailUrl;
      this.shareSuccess.emit('Email client opened!');
    }
  }

  onBackdropClick(event: Event) {
    if (event.target === event.currentTarget) {
      this.closeModal.emit();
    }
  }
}

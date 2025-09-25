import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BookModel } from '../models/book.model';

@Injectable({
  providedIn: 'root'
})
export class ShareService {
  private baseUrl: string;

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    // Get the current domain for generating share links (only in browser)
    if (isPlatformBrowser(this.platformId)) {
      this.baseUrl = window.location.origin;
    } else {
      // Fallback for SSR
      this.baseUrl = 'http://localhost:4200';
    }
  }

  /**
   * Generate a shareable link for a book
   * @param book The book to share
   * @returns The complete shareable URL
   */
  generateBookShareLink(book: BookModel): string {
    return `${this.baseUrl}/book/${book.id}`;
  }

  /**
   * Copy text to clipboard
   * @param text The text to copy
   * @returns Promise<boolean> indicating success
   */
  async copyToClipboard(text: string): Promise<boolean> {
    try {
      if (navigator.clipboard && window.isSecureContext) {
        await navigator.clipboard.writeText(text);
        return true;
      } else {
        // Fallback for older browsers
        const textArea = document.createElement('textarea');
        textArea.value = text;
        textArea.style.position = 'fixed';
        textArea.style.left = '-999999px';
        textArea.style.top = '-999999px';
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();
        
        const result = document.execCommand('copy');
        document.body.removeChild(textArea);
        return result;
      }
    } catch (error) {
      console.error('Failed to copy to clipboard:', error);
      return false;
    }
  }

  /**
   * Share book link to clipboard
   * @param book The book to share
   * @returns Promise<boolean> indicating success
   */
  async shareBookLink(book: BookModel): Promise<boolean> {
    const shareLink = this.generateBookShareLink(book);
    return await this.copyToClipboard(shareLink);
  }

  /**
   * Generate social media share URLs
   * @param book The book to share
   * @param platform The social media platform
   * @returns The share URL for the platform
   */
  generateSocialShareUrl(book: BookModel, platform: 'facebook' | 'twitter' | 'linkedin' | 'whatsapp'): string {
    const shareLink = this.generateBookShareLink(book);
    const shareText = `Check out "${book.title}" by ${book.author} - ₹${book.price}`;
    const encodedUrl = encodeURIComponent(shareLink);
    const encodedText = encodeURIComponent(shareText);

    switch (platform) {
      case 'facebook':
        return `https://www.facebook.com/sharer/sharer.php?u=${encodedUrl}`;
      case 'twitter':
        return `https://twitter.com/intent/tweet?url=${encodedUrl}&text=${encodedText}`;
      case 'linkedin':
        return `https://www.linkedin.com/sharing/share-offsite/?url=${encodedUrl}`;
      case 'whatsapp':
        return `https://wa.me/?text=${encodedText}%20${encodedUrl}`;
      default:
        return shareLink;
    }
  }

  /**
   * Open social media share in new window
   * @param book The book to share
   * @param platform The social media platform
   */
  shareToSocialMedia(book: BookModel, platform: 'facebook' | 'twitter' | 'linkedin' | 'whatsapp'): void {
    const shareUrl = this.generateSocialShareUrl(book, platform);
    window.open(shareUrl, '_blank', 'width=600,height=400');
  }

  /**
   * Check if Web Share API is supported
   * @returns boolean indicating if Web Share API is available
   */
  isWebShareSupported(): boolean {
    return 'share' in navigator;
  }

  /**
   * Use native Web Share API if available
   * @param book The book to share
   * @returns Promise<boolean> indicating success
   */
  async shareWithWebAPI(book: BookModel): Promise<boolean> {
    if (!this.isWebShareSupported()) {
      return false;
    }

    try {
      const shareData = {
        title: book.title,
        text: `Check out "${book.title}" by ${book.author} - ₹${book.price}`,
        url: this.generateBookShareLink(book)
      };

      await navigator.share(shareData);
      return true;
    } catch (error) {
      console.error('Web Share API failed:', error);
      return false;
    }
  }

  /**
   * Generate email share link
   * @param book The book to share
   * @returns The mailto URL
   */
  generateEmailShareUrl(book: BookModel): string {
    const shareLink = this.generateBookShareLink(book);
    const subject = encodeURIComponent(`Check out "${book.title}" by ${book.author}`);
    const body = encodeURIComponent(
      `I found this amazing book that you might like!\n\n` +
      `"${book.title}" by ${book.author}\n` +
      `Price: ₹${book.price}\n\n` +
      `Check it out here: ${shareLink}`
    );
    
    return `mailto:?subject=${subject}&body=${body}`;
  }
}

import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CategoryColorService {
  // Map of category names to their corresponding colors (background, text, border)
  private categoryColorMap: { [key: string]: { bg: string; text: string; border: string } } = {
    // Fantasy themes - purple/violet tones
    'fantasy': { bg: 'bg-purple-100', text: 'text-purple-800', border: 'border-purple-300' },
    'sci-fi': { bg: 'bg-indigo-100', text: 'text-indigo-800', border: 'border-indigo-300' },
    
    // Fiction themes - blue tones
    'fiction': { bg: 'bg-blue-100', text: 'text-blue-800', border: 'border-blue-300' },
    'historical fiction': { bg: 'bg-sky-100', text: 'text-sky-800', border: 'border-sky-300' },
    
    // Non-fiction themes - green tones
    'non-fiction': { bg: 'bg-emerald-100', text: 'text-emerald-800', border: 'border-emerald-300' },
    'biography': { bg: 'bg-green-100', text: 'text-green-800', border: 'border-green-300' },
    'self-help': { bg: 'bg-teal-100', text: 'text-teal-800', border: 'border-teal-300' },
    
    // Romance themes - pink/red tones
    'romance': { bg: 'bg-pink-100', text: 'text-pink-800', border: 'border-pink-300' },
    'drama': { bg: 'bg-rose-100', text: 'text-rose-800', border: 'border-rose-300' },
    
    // Mystery/Thriller themes - yellow/orange tones
    'mystery': { bg: 'bg-amber-100', text: 'text-amber-800', border: 'border-amber-300' },
    'thriller': { bg: 'bg-orange-100', text: 'text-orange-800', border: 'border-orange-300' },
    'crime': { bg: 'bg-yellow-100', text: 'text-yellow-800', border: 'border-yellow-300' },
    
    // Horror/Dark themes - gray/red tones
    'horror': { bg: 'bg-red-100', text: 'text-red-800', border: 'border-red-300' },
    'supernatural': { bg: 'bg-slate-200', text: 'text-slate-800', border: 'border-slate-400' },
    
    // Special categories
    'bestseller': { bg: 'bg-lime-100', text: 'text-lime-800', border: 'border-lime-300' },
    'newly launched': { bg: 'bg-cyan-100', text: 'text-cyan-800', border: 'border-cyan-300' },
    'special offers': { bg: 'bg-fuchsia-100', text: 'text-fuchsia-800', border: 'border-fuchsia-300' },
    'highly rated': { bg: 'bg-yellow-100', text: 'text-yellow-800', border: 'border-yellow-300' },
    
    // Default for any unmatched category
    'default': { bg: 'bg-gray-100', text: 'text-gray-800', border: 'border-gray-300' }
  };

  constructor() { }

  /**
   * Get the color classes for a specific category
   * @param category The category name (case-insensitive)
   * @returns Object containing background, text and border color classes
   */
  getCategoryColors(category: string): { bg: string; text: string; border: string } {
    const normalizedCategory = category.toLowerCase().trim();
    return this.categoryColorMap[normalizedCategory] || this.categoryColorMap['default'];
  }

  /**
   * Get all Tailwind classes for a category badge
   * @param category The category name
   * @returns String of combined Tailwind classes for the category badge
   */
  getCategoryBadgeClasses(category: string): string {
    const colors = this.getCategoryColors(category);
    // Base classes that all badges will have
    const baseClasses = 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium';
    return `${baseClasses} ${colors.bg} ${colors.text} ${colors.border}`;
  }
} 
import { TestBed } from '@angular/core/testing';
import { CategoryColorService } from './category-color.service';

describe('CategoryColorService', () => {
  let service: CategoryColorService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CategoryColorService]
    });
    service = TestBed.inject(CategoryColorService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getCategoryColors', () => {
    it('should return correct colors for fantasy category', () => {
      const colors = service.getCategoryColors('fantasy');
      expect(colors.bg).toBe('bg-purple-100');
      expect(colors.text).toBe('text-purple-800');
      expect(colors.border).toBe('border-purple-300');
    });

    it('should return correct colors for fiction category', () => {
      const colors = service.getCategoryColors('fiction');
      expect(colors.bg).toBe('bg-blue-100');
      expect(colors.text).toBe('text-blue-800');
      expect(colors.border).toBe('border-blue-300');
    });

    it('should return correct colors for non-fiction category', () => {
      const colors = service.getCategoryColors('non-fiction');
      expect(colors.bg).toBe('bg-emerald-100');
      expect(colors.text).toBe('text-emerald-800');
      expect(colors.border).toBe('border-emerald-300');
    });

    it('should return correct colors for romance category', () => {
      const colors = service.getCategoryColors('romance');
      expect(colors.bg).toBe('bg-pink-100');
      expect(colors.text).toBe('text-pink-800');
      expect(colors.border).toBe('border-pink-300');
    });

    it('should return correct colors for mystery category', () => {
      const colors = service.getCategoryColors('mystery');
      expect(colors.bg).toBe('bg-amber-100');
      expect(colors.text).toBe('text-amber-800');
      expect(colors.border).toBe('border-amber-300');
    });

    it('should return correct colors for horror category', () => {
      const colors = service.getCategoryColors('horror');
      expect(colors.bg).toBe('bg-red-100');
      expect(colors.text).toBe('text-red-800');
      expect(colors.border).toBe('border-red-300');
    });

    it('should return correct colors for bestseller category', () => {
      const colors = service.getCategoryColors('bestseller');
      expect(colors.bg).toBe('bg-lime-100');
      expect(colors.text).toBe('text-lime-800');
      expect(colors.border).toBe('border-lime-300');
    });

    it('should return correct colors for newly launched category', () => {
      const colors = service.getCategoryColors('newly launched');
      expect(colors.bg).toBe('bg-cyan-100');
      expect(colors.text).toBe('text-cyan-800');
      expect(colors.border).toBe('border-cyan-300');
    });

    it('should return correct colors for special offers category', () => {
      const colors = service.getCategoryColors('special offers');
      expect(colors.bg).toBe('bg-fuchsia-100');
      expect(colors.text).toBe('text-fuchsia-800');
      expect(colors.border).toBe('border-fuchsia-300');
    });

    it('should return correct colors for highly rated category', () => {
      const colors = service.getCategoryColors('highly rated');
      expect(colors.bg).toBe('bg-yellow-100');
      expect(colors.text).toBe('text-yellow-800');
      expect(colors.border).toBe('border-yellow-300');
    });

    it('should return default colors for unknown category', () => {
      const colors = service.getCategoryColors('UnknownCategory');
      expect(colors.bg).toBe('bg-gray-100');
      expect(colors.text).toBe('text-gray-800');
      expect(colors.border).toBe('border-gray-300');
    });

    it('should handle case-insensitive category names', () => {
      const colors1 = service.getCategoryColors('FANTASY');
      const colors2 = service.getCategoryColors('fantasy');
      expect(colors1).toEqual(colors2);
    });

    it('should handle whitespace in category names', () => {
      const colors1 = service.getCategoryColors('  fantasy  ');
      const colors2 = service.getCategoryColors('fantasy');
      expect(colors1).toEqual(colors2);
    });

    it('should handle empty category name', () => {
      const colors = service.getCategoryColors('');
      expect(colors.bg).toBe('bg-gray-100');
      expect(colors.text).toBe('text-gray-800');
      expect(colors.border).toBe('border-gray-300');
    });
  });

  describe('getCategoryBadgeClasses', () => {
    it('should return complete badge classes for fantasy category', () => {
      const badgeClasses = service.getCategoryBadgeClasses('fantasy');
      expect(badgeClasses).toContain('inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium');
      expect(badgeClasses).toContain('bg-purple-100');
      expect(badgeClasses).toContain('text-purple-800');
      expect(badgeClasses).toContain('border-purple-300');
    });

    it('should return complete badge classes for fiction category', () => {
      const badgeClasses = service.getCategoryBadgeClasses('fiction');
      expect(badgeClasses).toContain('inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium');
      expect(badgeClasses).toContain('bg-blue-100');
      expect(badgeClasses).toContain('text-blue-800');
      expect(badgeClasses).toContain('border-blue-300');
    });

    it('should return complete badge classes for unknown category', () => {
      const badgeClasses = service.getCategoryBadgeClasses('UnknownCategory');
      expect(badgeClasses).toContain('inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium');
      expect(badgeClasses).toContain('bg-gray-100');
      expect(badgeClasses).toContain('text-gray-800');
      expect(badgeClasses).toContain('border-gray-300');
    });

    it('should always include base classes', () => {
      const badgeClasses = service.getCategoryBadgeClasses('fantasy');
      const baseClasses = 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium';
      expect(badgeClasses).toContain(baseClasses);
    });
  });

  describe('Color consistency', () => {
    it('should maintain consistent color schemes across related categories', () => {
      // Fantasy themes should use purple/indigo
      const fantasyColors = service.getCategoryColors('fantasy');
      const sciFiColors = service.getCategoryColors('sci-fi');
      
      expect(fantasyColors.bg).toContain('purple');
      expect(sciFiColors.bg).toContain('indigo');
    });

    it('should use appropriate color schemes for content types', () => {
      // Fiction should use blue tones
      const fictionColors = service.getCategoryColors('fiction');
      const historicalFictionColors = service.getCategoryColors('historical fiction');
      
      expect(fictionColors.bg).toContain('blue');
      expect(historicalFictionColors.bg).toContain('sky');
    });

    it('should use warm colors for engaging content', () => {
      // Romance and drama should use warm colors
      const romanceColors = service.getCategoryColors('romance');
      const dramaColors = service.getCategoryColors('drama');
      
      expect(romanceColors.bg).toContain('pink');
      expect(dramaColors.bg).toContain('rose');
    });

    it('should use bright colors for special categories', () => {
      // Special categories should use bright, attention-grabbing colors
      const bestsellerColors = service.getCategoryColors('bestseller');
      const newlyLaunchedColors = service.getCategoryColors('newly launched');
      
      expect(bestsellerColors.bg).toContain('lime');
      expect(newlyLaunchedColors.bg).toContain('cyan');
    });
  });

  describe('Edge cases', () => {
    it('should handle null category gracefully', () => {
      const colors = service.getCategoryColors(null as any);
      expect(colors.bg).toBe('bg-gray-100');
      expect(colors.text).toBe('text-gray-800');
      expect(colors.border).toBe('border-gray-300');
    });

    it('should handle undefined category gracefully', () => {
      const colors = service.getCategoryColors(undefined as any);
      expect(colors.bg).toBe('bg-gray-100');
      expect(colors.text).toBe('text-gray-800');
      expect(colors.border).toBe('border-gray-300');
    });

    it('should handle very long category names', () => {
      const longCategory = 'This is a very long category name that should still work';
      const colors = service.getCategoryColors(longCategory);
      expect(colors.bg).toBe('bg-gray-100');
      expect(colors.text).toBe('text-gray-800');
      expect(colors.border).toBe('border-gray-300');
    });

    it('should handle special characters in category names', () => {
      const specialCategory = 'Fantasy & Adventure!@#$%^&*()';
      const colors = service.getCategoryColors(specialCategory);
      expect(colors.bg).toBe('bg-gray-100');
      expect(colors.text).toBe('text-gray-800');
      expect(colors.border).toBe('border-gray-300');
    });
  });
}); 
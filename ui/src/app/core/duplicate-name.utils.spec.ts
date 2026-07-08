import { generateDuplicateName, isCopyName } from './duplicate-name.utils';

describe('duplicate-name utils', () => {
  describe('generateDuplicateName', () => {
    it('should generate name with timestamp', () => {
      const date = new Date(2024, 0, 15, 9, 30); // Jan 15, 2024 09:30
      const result = generateDuplicateName('My Collection', date);
      expect(result).toBe('My Collection (Copy 2024-01-15 09:30)');
    });

    it('should pad single digit month/day/hour/minute with leading zero', () => {
      const date = new Date(2024, 2, 5, 3, 5); // Mar 5, 2024 03:05
      const result = generateDuplicateName('Test', date);
      expect(result).toBe('Test (Copy 2024-03-05 03:05)');
    });

    it('should remove existing (Copy ...) suffix to avoid double parens', () => {
      const date = new Date(2024, 5, 10, 14, 0); // Jun 10, 2024 14:00
      const result = generateDuplicateName(
        'My Collection (Copy 2024-01-01 10:00)',
        date
      );
      expect(result).toBe('My Collection (Copy 2024-06-10 14:00)');
    });

    it('should handle names with parentheses that are not Copy suffix', () => {
      const date = new Date(2024, 0, 15, 9, 30);
      const result = generateDuplicateName('My Collection (Draft)', date);
      expect(result).toBe('My Collection (Draft) (Copy 2024-01-15 09:30)');
    });

    it('should trim whitespace from base name', () => {
      const date = new Date(2024, 0, 15, 9, 30);
      const result = generateDuplicateName('  My Collection  ', date);
      expect(result).toBe('My Collection (Copy 2024-01-15 09:30)');
    });
  });

  describe('isCopyName', () => {
    it('should return true for names with (Copy ...) suffix', () => {
      expect(isCopyName('My Collection (Copy 2024-01-15 09:30)')).toBe(true);
      expect(isCopyName('Test (Copy 2023-12-25 00:00)')).toBe(true);
    });

    it('should return false for names without (Copy ...) suffix', () => {
      expect(isCopyName('My Collection')).toBe(false);
      expect(isCopyName('My Collection (Draft)')).toBe(false);
      expect(isCopyName('Copy of My Collection')).toBe(false);
    });

    it('should return false for empty string', () => {
      expect(isCopyName('')).toBe(false);
    });
  });
});

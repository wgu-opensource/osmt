const COPY_SUFFIX_REGEX = /\s*\(Copy[^)]*\)$/;

export function generateDuplicateName(
  originalName: string,
  date: Date = new Date()
): string {
  // Remove any existing (Copy ...) suffix to avoid double parens
  const baseName = originalName.replace(COPY_SUFFIX_REGEX, '').trim();

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');

  return `${baseName} (Copy ${year}-${month}-${day} ${hours}:${minutes})`;
}

export function isCopyName(name: string): boolean {
  return COPY_SUFFIX_REGEX.test(name);
}

import { AppConfig } from '../app.config';

/**
 * When split deployment is configured, replaces the origin of an absolute API
 * canonical URL with {@link AppConfig.settings.publicInstanceUrl}.
 */
export function replaceCanonicalOrigin(
  canonicalUrl: string,
  publicInstanceUrl: string
): string {
  const trimmed = canonicalUrl?.trim() ?? '';
  if (!trimmed) {
    return '';
  }
  const pub = publicInstanceUrl?.trim() ?? '';
  if (!pub) {
    return trimmed;
  }
  try {
    const parsed = new URL(trimmed);
    const base = pub.replace(/\/$/, '');
    new URL(base);
    return `${base}${parsed.pathname}${parsed.search}${parsed.hash}`;
  } catch {
    return trimmed;
  }
}

/** Applies runtime {@link AppConfig.settings.publicInstanceUrl}. */
export function resolveCanonicalPublicUrl(canonicalUrl: string): string {
  return replaceCanonicalOrigin(
    canonicalUrl,
    AppConfig.settings?.publicInstanceUrl ?? ''
  );
}

function hasHttpScheme(url: string): boolean {
  return /^https?:\/\//i.test(url);
}

/**
 * Public canonical skill URL for copy/display: prefers resolved absolute API id,
 * else builds `/api/skills/{uuid}` on the public instance when configured.
 */
export function canonicalSkillPublicUrl(skillId: string, uuid: string): string {
  const resolved = resolveCanonicalPublicUrl(skillId?.trim() ?? '');
  if (hasHttpScheme(resolved)) {
    return resolved;
  }
  const pub = AppConfig.settings?.publicInstanceUrl?.trim() ?? '';
  const id = uuid?.trim() ?? '';
  if (pub && id) {
    const base = pub.replace(/\/$/, '');
    return `${base}/api/skills/${id}`;
  }
  return skillId?.trim() ?? '';
}

/**
 * Public canonical collection URL for copy/display.
 */
export function canonicalCollectionPublicUrl(
  collectionId: string,
  uuid: string
): string {
  const resolved = resolveCanonicalPublicUrl(collectionId?.trim() ?? '');
  if (hasHttpScheme(resolved)) {
    return resolved;
  }
  const pub = AppConfig.settings?.publicInstanceUrl?.trim() ?? '';
  const id = uuid?.trim() ?? '';
  if (pub && id) {
    const base = pub.replace(/\/$/, '');
    return `${base}/api/collections/${id}`;
  }
  return collectionId?.trim() ?? '';
}

/**
 * Browser URL for opening the published skill SPA from authoring tools.
 */
export function openPublishedSkillBrowserUrl(
  skillUuid: string,
  skillCanonicalUrl: string
): string {
  const trimmed = skillCanonicalUrl?.trim() ?? '';
  if (hasHttpScheme(trimmed)) {
    return resolveCanonicalPublicUrl(trimmed);
  }
  const pub = AppConfig.settings?.publicInstanceUrl?.trim() ?? '';
  const id = skillUuid?.trim() ?? '';
  if (pub && id) {
    const base = pub.replace(/\/$/, '');
    return `${base}/skills/${id}`;
  }
  return `skills/${id}`;
}

/**
 * Browser URL for opening the published collection SPA from authoring tools.
 */
export function openPublishedCollectionBrowserUrl(
  collectionUuid: string,
  collectionCanonicalUrl: string
): string {
  const trimmed = collectionCanonicalUrl?.trim() ?? '';
  if (hasHttpScheme(trimmed)) {
    return resolveCanonicalPublicUrl(trimmed);
  }
  const pub = AppConfig.settings?.publicInstanceUrl?.trim() ?? '';
  const id = collectionUuid?.trim() ?? '';
  if (pub && id) {
    const base = pub.replace(/\/$/, '');
    return `${base}/collections/${id}`;
  }
  return `/collections/${id}`;
}

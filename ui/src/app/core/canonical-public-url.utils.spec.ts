import { AppConfig } from '../app.config';
import { DefaultAppConfig } from '../models/app-config.model';
import {
  canonicalCollectionPublicUrl,
  canonicalSkillPublicUrl,
  openPublishedCollectionBrowserUrl,
  openPublishedSkillBrowserUrl,
  replaceCanonicalOrigin,
  resolveCanonicalPublicUrl,
} from './canonical-public-url.utils';

describe('canonical-public-url utils', () => {
  const staffSkill = 'https://osmt-staff.example.com/api/skills/uuid-1';
  const publicBase = 'https://osmt.example.com';

  beforeEach(() => {
    AppConfig.settings = new DefaultAppConfig();
  });

  describe('replaceCanonicalOrigin', () => {
    it('returns empty for blank canonical URL', () => {
      expect(replaceCanonicalOrigin('', publicBase)).toBe('');
      expect(replaceCanonicalOrigin('   ', publicBase)).toBe('');
    });

    it('returns unchanged when public instance is blank', () => {
      expect(replaceCanonicalOrigin(staffSkill, '')).toBe(staffSkill);
    });

    it('replaces origin and preserves path', () => {
      expect(replaceCanonicalOrigin(staffSkill, publicBase)).toBe(
        'https://osmt.example.com/api/skills/uuid-1'
      );
    });

    it('normalizes trailing slash on public base', () => {
      expect(replaceCanonicalOrigin(staffSkill, `${publicBase}/`)).toBe(
        'https://osmt.example.com/api/skills/uuid-1'
      );
    });
  });

  describe('resolveCanonicalPublicUrl', () => {
    it('uses AppConfig.settings.publicInstanceUrl', () => {
      AppConfig.settings.publicInstanceUrl = publicBase;
      expect(resolveCanonicalPublicUrl(staffSkill)).toBe(
        'https://osmt.example.com/api/skills/uuid-1'
      );
    });
  });

  describe('canonicalSkillPublicUrl', () => {
    it('returns non-http id unchanged without public base', () => {
      expect(canonicalSkillPublicUrl('local-id', 'uuid-1')).toBe('local-id');
    });

    it('builds api URL under public instance without absolute id', () => {
      AppConfig.settings.publicInstanceUrl = publicBase;
      expect(canonicalSkillPublicUrl('local-id', 'uuid-1')).toBe(
        'https://osmt.example.com/api/skills/uuid-1'
      );
    });
  });

  describe('canonicalCollectionPublicUrl', () => {
    it('returns non-http id unchanged without public base', () => {
      expect(canonicalCollectionPublicUrl('id1', 'uuid1')).toBe('id1');
    });

    it('builds api URL under public instance without absolute id', () => {
      AppConfig.settings.publicInstanceUrl = publicBase;
      expect(canonicalCollectionPublicUrl('id1', 'uuid1')).toBe(
        'https://osmt.example.com/api/collections/uuid1'
      );
    });
  });

  describe('openPublishedSkillBrowserUrl', () => {
    it('uses relative skills path without public base', () => {
      expect(openPublishedSkillBrowserUrl('uuid-1', '')).toBe('skills/uuid-1');
    });

    it('opens on public origin when configured', () => {
      AppConfig.settings.publicInstanceUrl = publicBase;
      expect(openPublishedSkillBrowserUrl('uuid-1', '')).toBe(
        'https://osmt.example.com/skills/uuid-1'
      );
    });

    it('rewrites absolute canonical URL when configured', () => {
      AppConfig.settings.publicInstanceUrl = publicBase;
      expect(
        openPublishedSkillBrowserUrl(
          'uuid-1',
          'https://osmt-staff.example.com/api/skills/uuid-1'
        )
      ).toBe('https://osmt.example.com/api/skills/uuid-1');
    });
  });

  describe('openPublishedCollectionBrowserUrl', () => {
    it('uses absolute path without public base', () => {
      expect(openPublishedCollectionBrowserUrl('uuid1', '')).toBe(
        '/collections/uuid1'
      );
    });

    it('opens on public origin when configured', () => {
      AppConfig.settings.publicInstanceUrl = publicBase;
      expect(openPublishedCollectionBrowserUrl('uuid1', '')).toBe(
        'https://osmt.example.com/collections/uuid1'
      );
    });
  });
});

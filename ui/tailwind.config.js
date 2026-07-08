/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        // Palette (fixed)
        brand1: 'var(--color-brand1)',
        onBrandWhite: 'var(--color-onBrandWhite)',
        onBrandBlack: 'var(--color-onBrandBlack)',
        lime: '#3ba547',
        tangerine: '#f68d2e',
        cherry: '#bd2b41',
        blossom: '#e27e8d',
        seaBlue: '#16a1c0',
        grape: '#7f72c6',
        punch: '#bb2a74',
        mint: '#1b9da0',
        night: '#1f1f1f',
        charcoal: '#333',
        onyx: '#2b2b2b',
        smoke: '#6b6b6b',
        steel: '#949494',
        ash: '#e7e7e5',
        haze: '#f1f1ef',
        // Semantic (use CSS vars for theme/dark mode/whitelabel)
        background: {
          100: 'var(--color-background100)',
          200: 'var(--color-background200)',
          300: 'var(--color-background300)',
          500: 'var(--color-background500)',
        },
        text: {
          1: 'var(--color-text1)',
          2: 'var(--color-text2)',
          3: 'var(--color-text3)',
          4: 'var(--color-text4)',
        },
        interactive: {
          1: 'var(--color-interactive1)',
          2: 'var(--color-interactive2)',
        },
        attention: 'var(--color-attention)',
        focus: 'var(--color-focus)',
        positive: 'var(--color-positive)',
        warning: 'var(--color-warning)',
        a11yOnBrand: 'var(--color-a11yOnBrand)',
      },
      spacing: {
        extraSmall: '8px',
        small: '16px',
        medium: '32px',
        mediumTablet: '40px',
        large: '48px',
        largeTablet: '64px',
        container: '1024px',
      },
      maxWidth: {
        container: '1024px',
      },
      fontSize: {
        heading1: ['32px', { lineHeight: '40px' }],
        heading1Tablet: ['48px', { lineHeight: '56px' }],
        heading2: ['24px', { lineHeight: '32px' }],
        heading2Tablet: ['32px', { lineHeight: '40px' }],
        heading3: ['14px', { lineHeight: '24px' }],
        body: ['16px', { lineHeight: '24px' }],
        bodyLarge: ['16px', { lineHeight: '24px' }],
        bodyLargeTablet: ['24px', { lineHeight: '32px' }],
        small: ['12px', { lineHeight: '16px' }],
        extraSmall: ['8px', { lineHeight: '10px' }],
        button: ['14px', { lineHeight: '16px' }],
      },
      fontWeight: {
        semibold: '600',
      },
      screens: {
        // WGU pattern lib breakpoints: mobile <480, tablet 768+, desktop 1024+, hd 1440+
        tablet: '768px',
        desktop: '1024px',
        hd: '1440px',
        // Max-width variants: max-tablet = below 768px, max-desktop = below 1024px
        'max-mobile': { max: '479px' },
        'max-tablet': { max: '767px' },
        'max-desktop': { max: '1023px' },
        'max-hd': { max: '1439px' },
      },
      transitionDuration: {
        DEFAULT: '0.12s',
      },
      transitionTimingFunction: {
        DEFAULT: 'cubic-bezier(0.42, 0, 0.58, 1)',
      },
    },
  },
  plugins: [],
};

import { SvgHelper, SvgIcon } from './SvgHelper';

describe('SvgHelper', () => {
  it('path points at svg-defs with symbol id', () => {
    expect(SvgHelper.path(SvgIcon.EXTERNAL_LINK)).toBe(
      'assets/images/svg-defs.svg#icon-external-link'
    );
    expect(SvgHelper.path(SvgIcon.UNPUBLISH)).toBe(
      'assets/images/svg-defs.svg#icon-unpublish'
    );
  });

  it('extraPath points at svg-extra-defs with symbol id', () => {
    expect(SvgHelper.extraPath('icon-refresh-sync')).toBe(
      'assets/images/svg-extra-defs.svg#icon-refresh-sync'
    );
  });
});

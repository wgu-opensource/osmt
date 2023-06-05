import { AppPage } from './app.po.ts';

describe('workspace-project App', () => {
  let page: AppPage;

  it('should display welcome message', () => {
    let page = new AppPage();
    page.navigateTo();
    expect(page.getTitleText()).toEqual('ui app is running!');
  });
});

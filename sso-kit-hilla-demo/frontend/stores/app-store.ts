import { Subscription } from '@hilla/frontend';
import { RouterLocation } from '@vaadin/router';
import User from 'Frontend/generated/dev/hilla/sso/endpoint/User';
import { AuthEndpoint } from 'Frontend/generated/endpoints';
import { makeAutoObservable } from 'mobx';

export class AppStore {
  applicationName = 'sso-kit-hilla-demo';

  // The location, relative to the base path, e.g. "hello" when viewing "/hello"
  location = '';

  currentViewTitle = '';

  user: User | undefined = undefined;

  // A list of authentication providers. You can build the login URL as
  // `/oauth2/authorization/${client}` for each element in this array.
  registeredProviders: string[] = [];

  logoutUrl: string | undefined = undefined;

  backChannelLogoutEnabled = false;

  private logoutSubscription: Subscription<string> | undefined;

  backChannelLogoutHappened = false;

  constructor() {
    makeAutoObservable(this);
  }

  setLocation(location: RouterLocation) {
    const serverSideRoute = location.route?.path == '(.*)';
    if (location.route && !serverSideRoute) {
      this.location = location.route.path;
    } else if (location.pathname.startsWith(location.baseUrl)) {
      this.location = location.pathname.substr(location.baseUrl.length);
    } else {
      this.location = location.pathname;
    }
    if (serverSideRoute) {
      this.currentViewTitle = document.title; // Title set by server
    } else {
      this.currentViewTitle = (location?.route as any)?.title || '';
    }
  }

  async fetchAuthInfo() {
    const authInfo = await AuthEndpoint.getAuthInfo();
    this.user = authInfo.user;
    this.logoutUrl = authInfo.logoutUrl;
    this.registeredProviders = authInfo.registeredProviders;
    this.backChannelLogoutEnabled = authInfo.backChannelLogoutEnabled;

    if (this.user && this.backChannelLogoutEnabled) {
      this.logoutSubscription = await AuthEndpoint.backChannelLogout();

      this.logoutSubscription.onNext(async () => {
        this.backChannelLogoutHappened = true;
      });
    }
  }

  clearUserInfo() {
    this.user = undefined;
    this.logoutUrl = undefined;
    this.backChannelLogoutHappened = false;

    if (this.logoutSubscription) {
      this.logoutSubscription.cancel();
      this.logoutSubscription = undefined;
    }
  }

  get loggedIn() {
    return !!this.user;
  }

  isUserInRole(role: string) {
    return this.user?.roles?.includes(role);
  }
}

export const appStore = new AppStore();

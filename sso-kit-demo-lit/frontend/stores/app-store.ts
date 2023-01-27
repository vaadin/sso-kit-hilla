import { Subscription } from '@hilla/frontend';
import { RouterLocation } from '@vaadin/router';
import User from 'Frontend/generated/dev/hilla/sso/endpoint/User';
import Message from 'Frontend/generated/dev/hilla/sso/starter/BackChannelLogoutSubscription/Message';
import { BackChannelLogoutEndpoint, SingleSignOnEndpoint, UserEndpoint } from 'Frontend/generated/endpoints';
import { makeAutoObservable } from 'mobx';

export class AppStore {
  applicationName = 'sso-kit-demo-lit';

  // The location, relative to the base path, e.g. "hello" when viewing "/hello"
  location = '';

  currentViewTitle = '';

  user: User | undefined = undefined;

  // The default login URL
  loginLink: string | undefined = undefined;

  // The URL which will be called to log out from the SSO provider
  logoutLink: string | undefined = undefined;

  // If true, the app will listen to back-channel logout events
  backChannelLogoutEnabled = false;

  // If true, the user has been logged out from the SSO provider
  backChannelLogoutHappened = false;

  // The subscription to the back-channel logout event
  private logoutSubscription: Subscription<Message> | undefined;

  constructor() {
    makeAutoObservable(this);
  }

  async fetchAuthInfo() {
    const authInfo = await SingleSignOnEndpoint.fetchAll();
    this.loginLink = authInfo.loginLink;
    this.logoutLink = authInfo.logoutLink;
    this.backChannelLogoutEnabled = authInfo.backChannelLogoutEnabled;

    this.user = await UserEndpoint.getAuthenticatedUser();

    if (this.user && this.backChannelLogoutEnabled) {
      this.logoutSubscription = BackChannelLogoutEndpoint.subscribe();

      this.logoutSubscription.onNext(() => {
        this.backChannelLogoutHappened = true;
      });
    }
  }

  clearUserInfo() {
    this.user = undefined;
    this.logoutLink = undefined;
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
}

export const appStore = new AppStore();

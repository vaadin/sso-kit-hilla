import { logout } from '@hilla/frontend';
import '@vaadin-component-factory/vcf-nav';
import '@vaadin/app-layout';
import { AppLayout } from '@vaadin/app-layout';
import '@vaadin/app-layout/vaadin-drawer-toggle';
import '@vaadin/avatar';
import '@vaadin/confirm-dialog';
import '@vaadin/icon';
import '@vaadin/menu-bar';
import '@vaadin/scroller';
import '@vaadin/tabs';
import '@vaadin/tabs/vaadin-tab';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset';
import { html } from 'lit';
import { customElement } from 'lit/decorators.js';
import { router } from '../index';
import { hasAccess, views } from '../routes';
import { appStore } from '../stores/app-store';
import { Layout } from './view';

interface RouteInfo {
  path: string;
  title: string;
  icon: string;
}

@customElement('main-layout')
export class MainLayout extends Layout {
  render() {
    return html`
      <vaadin-app-layout primary-section="drawer">
        <header slot="drawer">
          <h1 class="text-l m-0">${appStore.applicationName}</h1>
        </header>
        <vaadin-scroller slot="drawer" scroll-direction="vertical">
          <!-- vcf-nav is not yet an official component -->
          <!-- For documentation, visit https://github.com/vaadin/vcf-nav#readme -->
          <vcf-nav aria-label="${appStore.applicationName}">
            ${this.getMenuRoutes().map(
              (viewRoute) => html`
                <vcf-nav-item path=${router.urlForPath(viewRoute.path)}>
                  <span class="${viewRoute.icon} nav-item-icon" slot="prefix" aria-hidden="true"></span>
                  ${viewRoute.title}
                </vcf-nav-item>
              `
            )}
          </vcf-nav>
        </vaadin-scroller>

        <footer slot="drawer">
          ${appStore.user
            ? html`
              <div className="flex items-center gap-m">
                ${appStore.user.fullName}
              </div>
              <vaadin-button @click="${this.logout}">Sign out</vaadin-button>
            `
            : appStore.registeredProviders.map(
              client => html`<a router-ignore href="/oauth2/authorization/${client}">Sign in with ${client}</a>`
            )}
        </footer>

        <vaadin-drawer-toggle slot="navbar" aria-label="Menu toggle"></vaadin-drawer-toggle>
        <h2 slot="navbar" class="text-l m-0">${appStore.currentViewTitle}</h2>

        <vaadin-confirm-dialog
          header="Logged out"
          cancel-button-visible
          @confirm="${this.loginAgain}"
          @cancel="${this.stayOnPage}"
          .opened="${appStore.backChannelLogoutHappened}"
        >
          <p>You have been logged out. Do you want to log in again?</p>
        </vaadin-confirm-dialog>

        <slot></slot>
      </vaadin-app-layout>
    `;
  }

  connectedCallback() {
    super.connectedCallback();
    this.classList.add('block', 'h-full');
    this.reaction(
      () => appStore.location,
      () => {
        AppLayout.dispatchCloseOverlayDrawerEvent();
      }
    );
  }

  private async logout() {
    await logout(); // Logout on the server
    location.href = appStore.logoutUrl!; // Logout on the provider
  }

  private async stayOnPage() {
    await logout(); // Logout on the server
    appStore.clearUserInfo(); // Logout on the client
  }

  private async loginAgain() {
    await logout(); // Logout on the server
    location.href = appStore.loginUrl!;
  }

  private getMenuRoutes(): RouteInfo[] {
    return views.filter((route) => route.title).filter((route) => hasAccess(route)) as RouteInfo[];
  }
}

import { appStore } from 'Frontend/stores/app-store';
import { html } from 'lit';
import { customElement } from 'lit/decorators.js';
import { View } from '../../views/view';

@customElement('about-view')
export class AboutView extends View {
  render() {
    return html`<div>
      <img style="width: 200px;" src="images/empty-plant.png" />
      <p>Username: ${appStore.user!.preferredUsername}</p>
      <p>Full name: ${appStore.user!.fullName}</p>
      <p>Email: ${appStore.user!.email}</p>
      <p>Roles: ${appStore.user!.roles?.join(', ')}</p>
    </div>`;
  }

  connectedCallback() {
    super.connectedCallback();
    this.classList.add(
      'flex',
      'flex-col',
      'h-full',
      'items-center',
      'justify-center',
      'p-l',
      'text-center',
      'box-border'
    );
  }
}

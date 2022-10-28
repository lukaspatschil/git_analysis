import {LitElement, css, html} from 'lit';
import {customElement} from 'lit/decorators.js';

@customElement('participation-chart')
export class ParticipationChart extends LitElement {
  // Define scoped styles right with your component, in plain CSS
  static styles = css`
    :host {
      color: blue;
    }
  `;

  render() {
    return html`<p>Hello, test!</p>`;
  }
}

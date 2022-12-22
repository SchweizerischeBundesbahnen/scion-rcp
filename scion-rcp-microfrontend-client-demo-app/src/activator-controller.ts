import {MessageClient, MicrofrontendPlatformClient} from '@scion/microfrontend-platform';
import {Beans} from '@scion/toolkit/bean-manager';

/**
 * Dummy activator that does nothing yet.
 */
class ActivatorController {

  public async init(): Promise<void> {
    await MicrofrontendPlatformClient.connect('client-app');
    await Beans.get(MessageClient).publish('activator/ready');
  }
}

new ActivatorController().init();

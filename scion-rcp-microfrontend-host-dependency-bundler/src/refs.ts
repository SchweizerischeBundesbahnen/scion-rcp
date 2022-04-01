import {IntentClient, MessageClient, MicrofrontendPlatform, OutletRouter} from '@scion/microfrontend-platform';
import {Beans} from '@scion/toolkit/bean-manager';

window['__SCION_RCP'] = window['__SCION_RCP'] || {};
window['__SCION_RCP'].refs = {
  get MicrofrontendPlatform() {
    return MicrofrontendPlatform;
  },
  get MessageClient() {
    return Beans.get(MessageClient);
  },
  get IntentClient() {
    return Beans.get(IntentClient);
  },
  get OutletRouter() {
    return Beans.get(OutletRouter);
  },
};

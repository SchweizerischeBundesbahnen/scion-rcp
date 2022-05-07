import {IntentClient, IntentInterceptor, ManifestService, MessageClient, MessageInterceptor, MicrofrontendPlatform, OutletRouter, QualifierMatcher, TopicMatcher} from '@scion/microfrontend-platform';
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
  get ManifestService() {
    return Beans.get(ManifestService);
  },
  get Beans() {
    return Beans;
  },
  get MessageInterceptor() {
    return MessageInterceptor;
  },
  get IntentInterceptor() {
    return IntentInterceptor;
  },
  get TopicMatcher() {
    return TopicMatcher;
  },
  get QualifierMatcher() {
    return QualifierMatcher;
  },
};


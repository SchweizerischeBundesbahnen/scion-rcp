import {IntentClient, IntentInterceptor, MicrofrontendPlatform, MicrofrontendPlatformHost, MicrofrontendPlatformClient, ManifestService, MessageClient, MessageInterceptor, OutletRouter, QualifierMatcher, TopicMatcher} from '@scion/microfrontend-platform';
import {Beans} from '@scion/toolkit/bean-manager';
import {UUID} from '@scion/toolkit/uuid';

window['__SCION_RCP'] = window['__SCION_RCP'] || {};
window['__SCION_RCP'].refs = {
  get MicrofrontendPlatform() {
    return MicrofrontendPlatform;
  },
  get MicrofrontendPlatformHost() {
    return MicrofrontendPlatformHost;
  },
  get MicrofrontendPlatformClient() {
    return MicrofrontendPlatformClient;
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
  get UUID() {
    return UUID;
  },
};


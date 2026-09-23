import {IntentClient, IntentInterceptor, MicrofrontendPlatform, MicrofrontendPlatformHost, MicrofrontendPlatformClient, ManifestService, MessageClient, MessageInterceptor, OutletRouter, QualifierMatcher, TopicMatcher} from '@scion/microfrontend-platform';
import {Beans} from '@scion/toolkit/bean-manager';
import {UUID} from '@scion/toolkit/uuid';

const refs = {
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

// Bridges typed refs to the untyped JS snippets executed by the Java host, preserving other
// properties (e.g. `storage`/`helpers`) added to the same namespace by helpers.js.
const scionRcpWindow = (window as unknown as {__SCION_RCP?: {refs?: typeof refs}});
scionRcpWindow.__SCION_RCP = scionRcpWindow.__SCION_RCP || {};
scionRcpWindow.__SCION_RCP.refs = refs;


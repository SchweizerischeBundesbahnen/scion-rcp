// ======= SCRIPT PLACEHOLDERS BEGIN =======
const refs = {IntentInterceptor: /@@refs.IntentInterceptor@@/, Beans: /@@refs.Beans@@/, QualifierMatcher: /@@refs.QualifierMatcher@@/, UUID: /@@refs.UUID@@/};
const helpers = {fromJson: /@@helpers.fromJson@@/, toJson: /@@helpers.toJson@@/};
const storage = /@@storage@@/;
const interceptorCallback = window['/@@interceptorCallback@@/'];
const type = helpers.fromJson('/@@type@@/');
const qualifier = helpers.fromJson('/@@qualifier@@/');
// ======= SCRIPT PLACEHOLDERS END =======

const qualifierMatcher = qualifier && new refs.QualifierMatcher(qualifier, {evalAsterisk: true, evalOptional: true});

// Register the interceptor in the SCION Microfrontend Platform.
refs.Beans.register(refs.IntentInterceptor, {
  multi: true,
  useValue: new class {
    intercept(message, next) {
      // Test intent type
      if (message.intent.type !== type) {
        return next.handle(message); // continue the chain of interceptors
      }

      // Test intent qualifier, but only if passed a qualifier
      if (qualifierMatcher && !qualifierMatcher.matches(message.intent.qualifier)) {
        return next.handle(message); // continue the chain of interceptors
      }

      return new Promise(resolve => {
        const nextCallbackName = `${refs.UUID.randomUUID()}-next`;
        storage[nextCallbackName] = resolve;
        interceptorCallback(helpers.toJson(message), nextCallbackName);
      })
      .then(result => {
        if (result instanceof Error) {
          return Promise.reject(result);
        }
        else if (result !== null) {
          return next.handle(result); // continue the chain of interceptors
        }
        else {
          return Promise.resolve();
        }
      });
    }
  },
});

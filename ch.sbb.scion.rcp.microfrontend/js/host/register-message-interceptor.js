// ======= SCRIPT PLACEHOLDERS BEGIN =======
const refs = {MessageInterceptor: /@@refs.MessageInterceptor@@/, Beans: /@@refs.Beans@@/, TopicMatcher: /@@refs.TopicMatcher@@/, UUID: /@@refs.UUID@@/};
const helpers = {fromJson: /@@helpers.fromJson@@/, toJson: /@@helpers.toJson@@/};
const storage = /@@storage@@/;
const interceptorCallback = window['/@@interceptorCallback@@/'];
const topic = helpers.fromJson('/@@topic@@/');
// ======= SCRIPT PLACEHOLDERS END =======

const topicMatcher = new refs.TopicMatcher(topic);

// Register the interceptor in the SCION Microfrontend Platform.
refs.Beans.register(refs.MessageInterceptor, {
  multi: true,
  useValue: new class {
    intercept(message, next) {
      const topicMatch = topicMatcher.match(message.topic);
      if (!topicMatch.matches) {
        next.handle(message); // continue the chain of interceptors
        return;
      }

      new Promise(resolve => {
        const nextCallbackName = `${refs.UUID.randomUUID()}-next`;
        storage[nextCallbackName] = resolve;
        interceptorCallback(helpers.toJson({...message, params: topicMatch.params}), nextCallbackName);
      })
      .then(result => {
        if (result instanceof Error) {
          // TODO: https://github.com/SchweizerischeBundesbahnen/scion-microfrontend-platform/issues/147
          //       Error is not reported back to the caller.
          throw result;
        }
        else if (result !== null) {
          next.handle(result); // continue the chain of interceptors
        }
      });
    }
  },
});

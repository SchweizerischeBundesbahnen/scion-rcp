const type = ${helpers.fromJson}('${type}');
const qualifier = ${helpers.fromJson}('${qualifier}');
const qualifierMatcher = qualifier && new ${QualifierMatcher }(qualifier, { evalAsterisk: true, evalOptional: true });

        // Register the interceptor in the SCION Microfrontend Platform.
        ${Beans}.register(${IntentInterceptor}, {
  useValue: new class {
    intercept(message, next) {
      // Test intent type
      if (message.intent.type !== type) {
        next.handle(message); // continue the chain of interceptors
        return;
      }

      // Test intent qualifier, but only if passed a qualifier
      if (qualifierMatcher && !qualifierMatcher.matches(message.intent.qualifier)) {
        next.handle(message); // continue the chain of interceptors
        return;
      }

      new Promise(resolve => {
        const nextCallbackName = '${interceptorCallback}_nextCallback';
              ${storage} [nextCallbackName] = resolve;
        window['${interceptorCallback}'](${helpers.toJson}(message), nextCallbackName);
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
  }, multi: true
});
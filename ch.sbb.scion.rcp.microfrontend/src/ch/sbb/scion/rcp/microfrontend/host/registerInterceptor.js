const topic = ${helpers.fromJson}('${topic}');
const topicMatcher = new ${TopicMatcher}(topic);

// Register the interceptor in the SCION Microfrontend Platform.
${Beans}.register(${MessageInterceptor}, {useValue: new class {
  intercept(message, next) {
    const topicMatch = topicMatcher.match(message.topic);
    if (!topicMatch.matches) {
      next.handle(message); // continue the chain of interceptors
      return;
    }

    new Promise(resolve => {
      const nextCallbackName = '${interceptorCallback}_nextCallback';
      ${storage}[nextCallbackName] = resolve;
      window['${interceptorCallback}'](${helpers.toJson}({...message, params: topicMatch.params}), nextCallbackName);
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
}, multi: true});
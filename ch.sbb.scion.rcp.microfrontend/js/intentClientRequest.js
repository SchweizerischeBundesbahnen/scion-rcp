(() => {
  const intent = ${ helpers.fromJson }('${intent}');
  const body = ${ helpers.fromJson }('${body}') ?? null;
  const options = {
    headers: ${ helpers.fromJson }('${options.headers}') ?? undefined,
          };
  return ${ refs.IntentClient }.request$(intent, body, options);
}) ()
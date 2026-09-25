import {MessageClient, MessageHeaders, MicrofrontendPlatformClient} from '@scion/microfrontend-platform';
import {fromEvent, Subscription} from 'rxjs';
import {Beans} from '@scion/toolkit/bean-manager';

class ClientController {

  public async init(): Promise<void> {
    this.createConnectSection();
    this.createPublishSection();
    this.createSubscribeSection();
  }

  private async createConnectSection(): Promise<void> {
    // we know that this element exists in the DOM, so we can use the non-null assertion operator.
    const connectStatus = document.querySelector<HTMLButtonElement>('output#connect-status')!;
    try {
      await MicrofrontendPlatformClient.connect('client-app');
      connectStatus.classList.add('connected');
    }
    catch (error) {
      console.error(error);
      connectStatus.classList.add('disconnected');
      this.setConnectError(error);
    }
  }

  private createPublishSection(): void {
    // we know that these elements exist in the DOM, so we can use the non-null assertion operator.
    const publishButton = document.querySelector<HTMLButtonElement>('button#publish')!;
    const topic = document.querySelector<HTMLInputElement>('input#publish-topic')!;
    const message = document.querySelector<HTMLInputElement>('textarea#message')!;
    const retain = document.querySelector<HTMLInputElement>('input#retain')!;

    fromEvent(publishButton, 'click').subscribe(async () => {
      publishButton.disabled = true;
      this.setPublishError('');
      try {

        await Beans.get(MessageClient).publish(topic.value, message.value, {retain: retain.checked});
      }
      catch (error) {
        console.error(error);
        this.setPublishError(error);
      }
      finally {
        publishButton.disabled = false;
      }
    });
  }

  private createSubscribeSection(): void {
    // we know that these elements exist in the DOM, so we can use the non-null assertion operator.
    const subscribeButton = document.querySelector<HTMLButtonElement>('button#subscribe')!;
    const topic = document.querySelector<HTMLInputElement>('input#subscribe-topic')!;
    const messagesElement = document.querySelector<HTMLInputElement>('ul#messages')!;
    let subscription: Subscription | null = null;

    fromEvent(subscribeButton, 'click').subscribe(async () => {
      messagesElement.innerHTML = '';

      if (subscription) {
        subscription.unsubscribe();
        subscribeButton.innerText = 'Subscribe';
        subscription = null;
        return;
      }

      subscribeButton.innerText = 'Unsubscribe';
      messagesElement.innerHTML = '';
      this.setSubscribeError('');
      try {
        subscription = Beans.get(MessageClient).observe$<string>(topic.value)
          .subscribe({
            next: message => {
              const messageElement = document.createElement('li');
              messageElement.classList.add('message');
              messageElement.innerHTML = `
                <span>${typeof message.body === 'object' ? JSON.stringify(message.body) : message.body}</span>
                <span>${new Date(message.headers.get(MessageHeaders.Timestamp) as Date).toLocaleTimeString()}`;
              messagesElement.prepend(messageElement);
            },
            error: error => {
              console.error(error);
              this.setSubscribeError(error);
            },
          });
      }
      catch (error) {
        console.error(error);
        this.setSubscribeError(error);
        subscribeButton.innerText = 'Subscribe';
      }
    });
  }

  private setConnectError(error: any): void {
    document.querySelector<HTMLOutputElement>('output#connect-status')!.value = this.getErrorMessage(error);
  }

  private setPublishError(error: any): void {
    document.querySelector<HTMLOutputElement>('output#publish-error')!.value = this.getErrorMessage(error);
  }

  private setSubscribeError(error: any): void {
    document.querySelector<HTMLOutputElement>('output#subscribe-error')!.value = this.getErrorMessage(error);
  }

  private getErrorMessage(error: any): string {
    return error instanceof Error ? error.message : String(error);
  }
}

new ClientController().init();

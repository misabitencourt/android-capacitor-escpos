import { WebPlugin } from '@capacitor/core';

import type { AndroidEscPosPlugin } from './definitions';

export class AndroidEscPosWeb extends WebPlugin implements AndroidEscPosPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}

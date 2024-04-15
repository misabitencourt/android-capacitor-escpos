import { registerPlugin } from '@capacitor/core';

import type { AndroidEscPosPlugin } from './definitions';

const AndroidEscPos = registerPlugin<AndroidEscPosPlugin>('AndroidEscPos', {
  web: () => import('./web').then(m => new m.AndroidEscPosWeb()),
});

export * from './definitions';
export { AndroidEscPos };

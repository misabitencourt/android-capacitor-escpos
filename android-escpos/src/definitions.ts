export interface AndroidEscPosPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}

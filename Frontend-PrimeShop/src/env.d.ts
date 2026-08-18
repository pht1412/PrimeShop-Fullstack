// Declare vite environment typings so `import.meta.env` is recognized by TypeScript
interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string;
  // add other VITE_... vars here if needed
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

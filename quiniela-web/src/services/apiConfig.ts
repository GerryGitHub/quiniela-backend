export function getApiUrl(): string {
  return import.meta.env.VITE_API_URL || 'https://api.gjapps.com';
}

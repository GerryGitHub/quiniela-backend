const STORAGE_KEY = 'api_url_mode';

const URLS = {
  local: 'http://localhost:8080',
  oracle: 'http://163.192.151.218:8080',
};

export function getApiUrl(): string {
  if (typeof window === 'undefined') return URLS.local;
  const mode = (localStorage.getItem(STORAGE_KEY) as 'local' | 'oracle') || 'local';
  return URLS[mode];
}
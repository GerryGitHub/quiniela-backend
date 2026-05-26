import { useState } from 'react';

const STORAGE_KEY = 'api_url_mode';

export function ApiToggle() {
  const [mode, setMode] = useState<'local' | 'vps'>(() => {
    return (localStorage.getItem(STORAGE_KEY) as 'local' | 'vps') || 'local';
  });

  const handleModeChange = (newMode: 'local' | 'vps') => {
    if (newMode === mode) return;
    localStorage.setItem(STORAGE_KEY, newMode);
    setMode(newMode);
    window.location.reload();
  };

  return (
    <div style={{
      position: 'fixed',
      bottom: 10,
      right: 10,
      zIndex: 9999,
      background: '#333',
      padding: '4px 8px',
      borderRadius: 4,
      fontSize: 12,
    }}>
      <span style={{ color: '#fff', marginRight: 8 }}>API:</span>
      <button
        onClick={() => handleModeChange('local')}
        disabled={mode === 'local'}
        style={{
          background: mode === 'local' ? '#4CAF50' : '#555',
          color: 'white',
          border: 'none',
          padding: '4px 8px',
          borderRadius: 4,
          cursor: mode === 'local' ? 'default' : 'pointer',
          marginRight: 4,
          opacity: mode === 'local' ? 0.5 : 1,
        }}
      >
        Local
      </button>
      <button
        onClick={() => handleModeChange('vps')}
        disabled={mode === 'vps'}
        style={{
          background: mode === 'vps' ? '#2196F3' : '#555',
          color: 'white',
          border: 'none',
          padding: '4px 8px',
          borderRadius: 4,
          cursor: mode === 'vps' ? 'default' : 'pointer',
          opacity: mode === 'vps' ? 0.5 : 1,
        }}
      >
        VPS
      </button>
    </div>
  );
}
import { useEffect, useRef } from 'react';

export default function Spinner({ size = 20 }: { size?: number }) {
  const styleRef = useRef<HTMLStyleElement | null>(null);
  
  useEffect(() => {
    if (!styleRef.current) {
      const style = document.createElement('style');
      style.textContent = `@keyframes spin { to { transform: rotate(360deg); } }`;
      document.head.appendChild(style);
      styleRef.current = style;
    }
  }, []);

  return (
    <span
      style={{
        display: 'inline-block',
        width: size,
        height: size,
        border: '2px solid rgba(255,255,255,0.3)',
        borderRadius: '50%',
        borderTopColor: 'white',
        animation: 'spin 1s linear infinite',
      }}
    />
  );
}
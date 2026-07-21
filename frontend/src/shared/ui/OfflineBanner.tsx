import { useState, useEffect, useCallback } from 'react';
import { cn } from '../lib/utils';
import { WifiOff, RefreshCw, CloudOff } from 'lucide-react';

type ConnectionStatus = 'online' | 'offline' | 'reconnecting';

interface OfflineBannerProps {
  /** Délai avant de considérer l'utilisateur comme hors ligne (ms) */
  offlineTimeout?: number;
  /** Intervalle de reconnexion (ms) */
  retryInterval?: number;
  /** Nombre maximum de tentatives de reconnexion */
  maxRetries?: number;
  /** Fonction de vérification personnalisée (ex: ping API) */
  checkConnection?: () => Promise<boolean>;
  /** Classes supplémentaires */
  className?: string;
}

function OfflineBanner({
  offlineTimeout = 3000,
  retryInterval = 5000,
  maxRetries = 10,
  checkConnection,
  className,
}: OfflineBannerProps) {
  const [status, setStatus] = useState<ConnectionStatus>('online');
  const [retryCount, setRetryCount] = useState(0);
  const [dismissed, setDismissed] = useState(false);

  const verifyConnection = useCallback(async () => {
    try {
      if (checkConnection) {
        return await checkConnection();
      }
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 5000);
      const response = await fetch('/api/v1/actuator/health', {
        method: 'HEAD',
        signal: controller.signal,
      });
      clearTimeout(timeoutId);
      return response.ok;
    } catch {
      return false;
    }
  }, [checkConnection]);

  // Écoute les événements navigateur + vérification périodique
  useEffect(() => {
    const handleOnline = () => {
      setStatus('reconnecting');
      setRetryCount(0);
      verifyConnection().then((isConnected) => {
        if (isConnected) {
          setStatus('online');
          setDismissed(false);
        }
      });
    };

    const handleOffline = () => {
      setStatus('offline');
      setRetryCount(0);
      setDismissed(false);
    };

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    // Vérification périodique silencieuse (toutes les 30s)
    const periodicCheck = setInterval(async () => {
      if (status === 'online') {
        const isConnected = await verifyConnection();
        if (!isConnected) {
          setStatus('offline');
          setDismissed(false);
        }
      }
    }, 30000);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
      clearInterval(periodicCheck);
    };
  }, [offlineTimeout, verifyConnection, status]);

  // Tentative de reconnexion automatique (limitée à maxRetries)
  useEffect(() => {
    if (status !== 'offline' || retryCount >= maxRetries) return;

    const retryId = setInterval(async () => {
      setRetryCount((prev) => prev + 1);
      setStatus('reconnecting');
      const isConnected = await verifyConnection();
      if (isConnected) {
        setStatus('online');
        setRetryCount(0);
        setDismissed(false);
        clearInterval(retryId);
      } else {
        setStatus('offline');
        if (retryCount >= maxRetries) {
          clearInterval(retryId);
        }
      }
    }, retryInterval);

    return () => clearInterval(retryId);
  }, [status, retryInterval, maxRetries, retryCount, verifyConnection]);

  if (status === 'online') return null;

  return (
    <>
      {/* Barre de notification principale */}
      {!dismissed && (
        <div
          className={cn(
            'fixed top-0 left-0 right-0 z-[60] animate-slide-in-down',
            className,
          )}
          role="alert"
          aria-live="assertive"
        >
          <div
            className={cn(
              'flex items-center justify-center gap-2 px-4 py-2.5 text-sm font-medium transition-all duration-[var(--brand-duration-mid)]',
              {
                'bg-[var(--stock-warning-bg)] text-[var(--stock-warning)]': status === 'offline',
                'bg-[var(--brand-primary-bg)] text-[var(--brand-primary)]': status === 'reconnecting',
              },
            )}
          >
            {status === 'offline' ? (
              <>
                <WifiOff className="h-4 w-4 shrink-0" />
                <span>
                  Vous êtes hors ligne — tentative de reconnexion...
                </span>
                <button
                  onClick={() => setDismissed(true)}
                  className="ml-2 underline hover:no-underline text-xs opacity-70 hover:opacity-100"
                >
                  Ignorer
                </button>
              </>
            ) : (
              <>
                <RefreshCw className="h-4 w-4 shrink-0 animate-spin" />
                <span>
                  Reconnexion en cours{retryCount > 0 ? ` (tentative ${retryCount}/${maxRetries})` : ''}...
                </span>
              </>
            )}
          </div>
        </div>
      )}

      {/* Indicateur persistant en bas à gauche (même si dismissé) */}
      {status === 'offline' && (
        <div
          className="fixed bottom-4 left-4 flex items-center gap-2 rounded-full bg-[var(--stock-warning-bg)] text-[var(--stock-warning)] px-3 py-1.5 text-xs shadow-lg z-[60]"
          title="Connexion perdue"
        >
          <CloudOff className="h-3.5 w-3.5" />
          <span>Hors ligne{retryCount > 0 ? ` (${retryCount}/${maxRetries})` : ''}</span>
        </div>
      )}

      {/* Indicateur persistent en bas à gauche quand en reconnexion */}
      {status === 'reconnecting' && (
        <div
          className="fixed bottom-4 left-4 flex items-center gap-2 rounded-full bg-[var(--brand-primary-bg)] text-[var(--brand-primary)] px-3 py-1.5 text-xs shadow-lg z-[60]"
          title="Reconnexion en cours"
        >
          <RefreshCw className="h-3.5 w-3.5 animate-spin" />
          <span>Reconnexion{retryCount > 0 ? ` (${retryCount}/${maxRetries})` : ''}</span>
        </div>
      )}
    </>
  );
}

OfflineBanner.displayName = 'OfflineBanner';

export { OfflineBanner };
export type { OfflineBannerProps, ConnectionStatus };

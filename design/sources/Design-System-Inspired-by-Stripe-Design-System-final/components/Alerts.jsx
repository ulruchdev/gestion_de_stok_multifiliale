/**
 * StockMaster CM - Alert Components
 * Design System Inspired by Stripe
 * 
 * Composants d'alerte et de feedback du Design System
 * Compatible avec Babel Standalone pour utilisation autonome
 * 
 * Usage with Babel Standalone:
 * <script type="text/babel" src="components/Alerts.jsx"></script>
 * Then use: window.Alert, window.Toast, window.EmptyState, etc.
 * 
 * Note: Utilise window.Button et window.Card au lieu d'importer
 */

// ============================================================================
// REACT REFERENCE
// ============================================================================

// Reference to window.React for Babel Standalone compatibility
const React = window.React;

// ============================================================================
// UTILITY STYLES
// ============================================================================

/**
 * Applique les styles de base avec les tokens CSS
 */
const baseStyles = {
  boxSizing: 'border-box',
  margin: 0,
  padding: 0,
};

/**
 * Classe utilitaire pour le texte standard
 */
const textStyles = {
  fontFamily: 'var(--font-body, system-ui, -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif)',
  fontSize: 'var(--text-body, 16px)',
  fontWeight: 'var(--text-body-weight, 400)',
  lineHeight: 'var(--text-body-lineheight, 1.5)',
  color: 'var(--fg, #262626)',
};

// ============================================================================
// ALERTS & NOTIFICATIONS
// ============================================================================

/**
 * Alerte generique
 * @param {Object} props - Props du composant
 * @param {string} props.type - Type d'alerte (success, warning, error, info)
 * @param {string} props.title - Titre de l'alerte
 * @param {string} props.message - Message de l'alerte
 * @param {function} props.onClose - Handler de fermeture
 * @param {string} props.className - Classes CSS additionnelles
 * @param {elementType} props.icon - Composant icône
 */
const Alert = ({ 
  type = 'info', 
  title,
  message,
  onClose,
  className = '', 
  icon: Icon,
  ...props 
}) => {
  const alertConfig = {
    success: {
      backgroundColor: 'var(--success-bg, #f6ffed)',
      color: 'var(--success, #15be53)',
      borderColor: 'var(--success-border, #b7eb8f)',
      iconColor: 'var(--success, #15be53)',
    },
    warning: {
      backgroundColor: 'var(--warning-bg, #fffbe6)',
      color: 'var(--warning, #faad14)',
      borderColor: 'var(--warning-border, #ffe58f)',
      iconColor: 'var(--warning, #faad14)',
    },
    error: {
      backgroundColor: 'var(--error-bg, #fff2f0)',
      color: 'var(--error, #ff4d4f)',
      borderColor: 'var(--error-border, #ffccc7)',
      iconColor: 'var(--error, #ff4d4f)',
    },
    info: {
      backgroundColor: 'var(--info-bg, #e6f4ff)',
      color: 'var(--info, #1677ff)',
      borderColor: 'var(--info-border, #91caff)',
      iconColor: 'var(--info, #1677ff)',
    },
  };

  const config = alertConfig[type];

  return (
    <div
      className={`alert alert-${type} ${className}`.trim()}
      style={{
        ...baseStyles,
        display: 'flex',
        alignItems: 'flex-start',
        gap: 'var(--space-sm, 8px)',
        padding: 'var(--space-md, 12px)',
        backgroundColor: config.backgroundColor,
        border: `1px solid ${config.borderColor}`,
        borderRadius: 'var(--radius-sm, 4px)',
        color: config.color,
        fontSize: 'var(--text-body-sm, 14px)',
        lineHeight: 1.5,
      }}
      role="alert"
      data-od-id="alert"
      {...props}
    >
      {Icon && (
        <div 
          style={{
            flexShrink: 0,
            color: config.iconColor,
          }}
          data-od-id="alert-icon"
        >
          <Icon size={20} />
        </div>
      )}
      
      <div
        style={{
          flex: 1,
        }}
        data-od-id="alert-content"
      >
        {title && (
          <div
            style={{
              fontWeight: 600,
              marginBottom: 'var(--space-xxs, 2px)',
            }}
            data-od-id="alert-title"
          >
            {title}
          </div>
        )}
        {message && (
          <div data-od-id="alert-message">
            {message}
          </div>
        )}
      </div>
      
      {onClose && (
        <window.Button
          variant="ghost"
          size="sm"
          onClick={onClose}
          aria-label="Fermer"
          data-od-id="alert-close"
          style={{
            color: config.color,
            minWidth: 'auto',
            padding: 'var(--space-xxs, 2px)',
          }}
        >
          &times;
        </window.Button>
      )}
    </div>
  );
};

Alert.propTypes = {
  type: PropTypes.oneOf(['success', 'warning', 'error', 'info']),
  title: PropTypes.string,
  message: PropTypes.string,
  onClose: PropTypes.func,
  className: PropTypes.string,
  icon: PropTypes.elementType,
};

/**
 * Toast notification (positionnee)
 * @param {Object} props - Props du composant
 * @param {string} props.type - Type de toast (success, warning, error, info)
 * @param {string} props.message - Message du toast
 * @param {function} props.onClose - Handler de fermeture
 * @param {string} props.position - Position (top-right, top-left, bottom-right, bottom-left)
 * @param {number} props.duration - Durée d'affichage en ms (0 = pas de fermeture auto)
 */
const Toast = ({ 
  type = 'info', 
  message,
  onClose,
  position = 'top-right',
  duration = 5000,
  ...props 
}) => {
  const [isVisible, setIsVisible] = React.useState(true);

  React.useEffect(() => {
    if (duration > 0) {
      const timer = setTimeout(() => {
        setIsVisible(false);
        onClose?.();
      }, duration);
      return () => clearTimeout(timer);
    }
  }, [duration, onClose]);

  if (!isVisible) return null;

  const positionStyles = {
    'top-right': {
      position: 'fixed',
      top: 'var(--space-lg, 16px)',
      right: 'var(--space-lg, 16px)',
      zIndex: 9999,
    },
    'top-left': {
      position: 'fixed',
      top: 'var(--space-lg, 16px)',
      left: 'var(--space-lg, 16px)',
      zIndex: 9999,
    },
    'bottom-right': {
      position: 'fixed',
      bottom: 'var(--space-lg, 16px)',
      right: 'var(--space-lg, 16px)',
      zIndex: 9999,
    },
    'bottom-left': {
      position: 'fixed',
      bottom: 'var(--space-lg, 16px)',
      left: 'var(--space-lg, 16px)',
      zIndex: 9999,
    },
  };

  return (
    <div
      style={positionStyles[position]}
      data-od-id="toast-container"
    >
      <Alert
        type={type}
        message={message}
        onClose={onClose}
        {...props}
      />
    </div>
  );
};

Toast.propTypes = {
  type: PropTypes.oneOf(['success', 'warning', 'error', 'info']),
  message: PropTypes.string.isRequired,
  onClose: PropTypes.func,
  position: PropTypes.oneOf(['top-right', 'top-left', 'bottom-right', 'bottom-left']),
  duration: PropTypes.number,
};

// ============================================================================
// EMPTY STATES
// ============================================================================

/**
 * Etat vide
 * @param {Object} props - Props du composant
 * @param {elementType} props.icon - Composant icône
 * @param {string} props.title - Titre
 * @param {string} props.description - Description
 * @param {node} props.action - Action à afficher
 * @param {string} props.className - Classes CSS additionnelles
 */
const EmptyState = ({ 
  icon: Icon,
  title,
  description,
  action,
  className = '',
  ...props 
}) => {
  return (
    <window.Card
      variant="minimal"
      padding="xxl"
      className={`empty-state ${className}`.trim()}
      data-od-id="empty-state"
      {...props}
    >
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          textAlign: 'center',
          gap: 'var(--space-md, 12px)',
        }}
      >
        {Icon && (
          <div
            style={{
              color: 'var(--muted, #93cba8)',
              opacity: 0.5,
            }}
            data-od-id="empty-icon"
          >
            <Icon size={64} />
          </div>
        )}
        
        {title && (
          <h3
            style={{
              ...textStyles,
              fontSize: 'var(--text-hmd, 20px)',
              fontWeight: 600,
              color: 'var(--fg, #262626)',
              margin: 0,
            }}
            data-od-id="empty-title"
          >
            {title}
          </h3>
        )}
        
        {description && (
          <p
            style={{
              ...textStyles,
              fontSize: 'var(--text-body-sm, 14px)',
              color: 'var(--muted, #93cba8)',
              margin: 0,
              maxWidth: '400px',
            }}
            data-od-id="empty-description"
          >
            {description}
          </p>
        )}
        
        {action && (
          <div data-od-id="empty-action">
            {action}
          </div>
        )}
      </div>
    </Card>
  );
};

EmptyState.propTypes = {
  icon: PropTypes.elementType,
  title: PropTypes.string,
  description: PropTypes.string,
  action: PropTypes.node,
  className: PropTypes.string,
};

/**
 * Etat de chargement (Skeleton)
 * @param {Object} props - Props du composant
 * @param {string} props.variant - Variante (text, circular, rectangular)
 * @param {string|number} props.width - Largeur
 * @param {string|number} props.height - Hauteur
 * @param {number} props.lines - Nombre de lignes (pour variant text)
 * @param {string} props.className - Classes CSS additionnelles
 */
const Skeleton = ({ 
  variant = 'text', 
  width = '100%', 
  height,
  lines = 1,
  className = '',
  ...props 
}) => {
  const baseHeight = variant === 'text' ? '1em' : height;

  const skeletonStyles = {
    ...baseStyles,
    backgroundColor: '#f0f0f0',
    borderRadius: variant === 'circular' ? '50%' : 'var(--radius-sm, 4px)',
    position: 'relative',
    overflow: 'hidden',
  };

  const shimmerStyles = {
    position: 'absolute',
    top: 0,
    left: 0,
    width: '100%',
    height: '100%',
    background: 'linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.5), transparent)',
    animation: 'shimmer 1.5s infinite',
  };

  if (variant === 'text' && lines > 1) {
    return (
      <div
        className={`skeleton ${className}`.trim()}
        style={skeletonStyles}
        data-od-id="skeleton"
        {...props}
      >
        {Array.from({ length: lines }).map((_, i) => (
          <div
            key={i}
            style={{
              ...skeletonStyles,
              height: baseHeight,
              width: i === lines - 1 ? '75%' : '100%',
              marginBottom: i < lines - 1 ? 'var(--space-xs, 4px)' : 0,
              position: 'relative',
            }}
          >
            <div style={shimmerStyles} />
          </div>
        ))}
      </div>
    );
  }

  return (
    <div
      className={`skeleton ${className}`.trim()}
      style={{
        ...skeletonStyles,
        width,
        height: height || baseHeight,
      }}
      data-od-id="skeleton"
      {...props}
    >
      <div style={shimmerStyles} />
    </div>
  );
};

Skeleton.propTypes = {
  variant: PropTypes.oneOf(['text', 'circular', 'rectangular']),
  width: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  height: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  lines: PropTypes.number,
  className: PropTypes.string,
};

/**
 * Etat hors ligne
 * @param {Object} props - Props du composant
 * @param {function} props.onRetry - Handler de réessai
 * @param {string} props.className - Classes CSS additionnelles
 */
const OfflineState = ({ 
  onRetry,
  className = '',
  ...props 
}) => {
  return (
    <window.Card
      variant="standard"
      padding="lg"
      className={`offline-state ${className}`.trim()}
      data-od-id="offline-state"
      {...props}
    >
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 'var(--space-md, 12px)',
        }}
      >
        <div
          style={{
            flexShrink: 0,
            width: '40px',
            height: '40px',
            backgroundColor: 'var(--error-bg, #fff2f0)',
            borderRadius: '50%',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
          data-od-id="offline-icon"
        >
          <svg 
            width="24" 
            height="24" 
            viewBox="0 0 24 24" 
            fill="none" 
            stroke="var(--error, #ff4d4f)" 
            strokeWidth="2" 
            strokeLinecap="round" 
            strokeLinejoin="round"
          >
            <path d="M12 20h9" />
            <path d="M21.5 17.5 19 16c-1.88-1.35-2.66-3.84-1.96-6.04M16 12.73a4 4 0 1 1 0-7.46M12 2C9.243 2 7 4.243 7 7c0 1.44.535 2.781 1.424 3.792M22 12c0 2.21-1.79 4-4 4s-4-1.79-4-4" />
            <line x1="2" y1="22" x2="22" y2="2" />
          </svg>
        </div>
        
        <div
          style={{
            flex: 1,
          }}
        >
          <h4
            style={{
              ...textStyles,
              fontSize: 'var(--text-hmd, 20px)',
              fontWeight: 600,
              color: 'var(--error, #ff4d4f)',
              margin: 0,
            }}
            data-od-id="offline-title"
          >
            Hors ligne
          </h4>
          <p
            style={{
              ...textStyles,
              fontSize: 'var(--text-body-sm, 14px)',
              color: 'var(--muted, #93cba8)',
              margin: 0,
            }}
            data-od-id="offline-description"
          >
            Les modifications seront synchronisees a la reconnexion
          </p>
        </div>
        
        {onRetry && (
          <window.Button
            variant="primary"
            size="sm"
            onClick={onRetry}
            data-od-id="offline-retry"
          >
            Reessayer
          </window.Button>
        )}
      </div>
    </Card>
  );
};

OfflineState.propTypes = {
  onRetry: PropTypes.func,
  className: PropTypes.string,
};

/**
 * Etat de chargement
 * @param {Object} props - Props du composant
 * @param {string} props.message - Message à afficher
 * @param {string} props.className - Classes CSS additionnelles
 */
const LoadingState = ({ 
  message = 'Chargement...',
  className = '',
  ...props 
}) => {
  return (
    <window.Card
      variant="minimal"
      padding="xxl"
      className={`loading-state ${className}`.trim()}
      data-od-id="loading-state"
      {...props}
    >
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          textAlign: 'center',
          gap: 'var(--space-md, 12px)',
        }}
      >
        <div
          className="spinner"
          style={{
            width: '40px',
            height: '40px',
            border: '3px solid var(--border, #e5edf5)',
            borderTopColor: 'var(--accent, #1c1e54)',
            borderRadius: '50%',
            animation: 'spin 0.8s linear infinite',
          }}
          data-od-id="loading-spinner"
        />
        
        {message && (
          <p
            style={{
              ...textStyles,
              fontSize: 'var(--text-body-sm, 14px)',
              color: 'var(--muted, #93cba8)',
              margin: 0,
            }}
            data-od-id="loading-message"
          >
            {message}
          </p>
        )}
      </div>
    </Card>
  );
};

LoadingState.propTypes = {
  message: PropTypes.string,
  className: PropTypes.string,
};

// ============================================================================
// EXPORTS
// ============================================================================

// ==========================================================================
// EXPORT TO WINDOW
// Pour utilisation avec Babel Standalone
// ==========================================================================
Object.assign(window, {
  Alert,
  Toast,
  EmptyState,
  Skeleton,
  OfflineState,
  LoadingState,
});

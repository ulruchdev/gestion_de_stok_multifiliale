/**
 * StockMaster CM - Atomic Components
 * Design System Inspired by Stripe
 * 
 * Composants atomiques de base (typographie, layout)
 * Ce fichier contient les composants utilitaires qui ne sont pas dans les fichiers spécialisés
 * 
 * Compatible avec Babel Standalone pour utilisation autonome
 * 
 * Usage with Babel Standalone:
 * <script type="text/babel" src="components/Atomic.jsx"></script>
 * Then use: window.H1, window.H2, window.Text, etc.
 */

// ============================================================================
// REACT REFERENCE
// ============================================================================

// Reference to window.React for Babel Standalone compatibility
const React = window.React;

// ============================================================================
// UTILITY STYLES
// ============================================================================

const baseStyles = {
  boxSizing: 'border-box',
  margin: 0,
  padding: 0,
};

const textStyles = {
  fontFamily: 'var(--font-body, system-ui, -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif)',
  fontSize: 'var(--text-body, 16px)',
  fontWeight: 'var(--text-body-weight, 400)',
  lineHeight: 'var(--text-body-lineheight, 1.5)',
  color: 'var(--fg, #262626)',
};

const monoStyles = {
  fontFamily: 'var(--font-mono, ui-monospace, SFMono-Regular, Menlo, monospace)',
  fontSize: 'var(--text-mono, 12px)',
  fontWeight: 'var(--text-mono-weight, 400)',
  lineHeight: 'var(--text-mono-lineheight, 1.5)',
  color: 'var(--fg, #262626)',
};

// ============================================================================
// TYPOGRAPHY COMPONENTS
// ============================================================================

/**
 * Titre de niveau 1
 */
const H1 = ({ children, className = '', ...props }) => {
  return (
    <h1
      className={`h1 ${className}`.trim()}
      style={{
        ...baseStyles,
        fontFamily: 'var(--font-display, SFMono, system-ui, -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif)',
        fontSize: 'var(--font-size-h1, clamp(28px, 6vw, 40px))',
        fontWeight: '700',
        lineHeight: '1.3',
        color: 'var(--fg, #262626)',
      }}
      data-od-id="heading-h1"
      {...props}
    >
      {children}
    </h1>
  );
};

/**
 * Titre de niveau 2
 */
const H2 = ({ children, className = '', ...props }) => {
  return (
    <h2
      className={`h2 ${className}`.trim()}
      style={{
        ...baseStyles,
        fontFamily: 'var(--font-display, SFMono, system-ui, -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif)',
        fontSize: 'var(--font-size-h2, clamp(24px, 5vw, 32px))',
        fontWeight: '600',
        lineHeight: '1.3',
        color: 'var(--fg, #262626)',
      }}
      data-od-id="heading-h2"
      {...props}
    >
      {children}
    </h2>
  );
};

/**
 * Titre de niveau 3
 */
const H3 = ({ children, className = '', ...props }) => {
  return (
    <h3
      className={`h3 ${className}`.trim()}
      style={{
        ...baseStyles,
        fontFamily: 'var(--font-display, SFMono, system-ui, -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif)',
        fontSize: 'var(--font-size-h3, clamp(20px, 4vw, 28px))',
        fontWeight: '600',
        lineHeight: '1.3',
        color: 'var(--fg, #262626)',
      }}
      data-od-id="heading-h3"
      {...props}
    >
      {children}
    </h3>
  );
};

/**
 * Texte standard avec options de style
 */
const Text = ({ children, className = '', size = 'body', muted = false, mono = false, ...props }) => {
  const sizeMap = {
    xs: 'var(--font-size-xs, 12px)',
    sm: 'var(--font-size-sm, 14px)',
    body: 'var(--font-size-base, 16px)',
    lg: 'var(--font-size-lg, 18px)',
    xl: 'var(--font-size-xl, 20px)',
  };

  const style = {
    ...baseStyles,
    fontFamily: mono ? 'var(--font-mono, ui-monospace, SFMono-Regular, Menlo, monospace)' : 'var(--font-body, system-ui, -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif)',
    fontSize: sizeMap[size] || sizeMap.body,
    fontWeight: 'var(--text-body-weight, 400)',
    lineHeight: 'var(--text-body-lineheight, 1.5)',
    color: muted ? 'var(--muted, #93cba8)' : 'var(--fg, #262626)',
  };

  return (
    <p
      className={`text text-${size} ${muted ? 'text-muted' : ''} ${mono ? 'font-mono' : ''} ${className}`.trim()}
      style={style}
      data-od-id="text"
      {...props}
    >
      {children}
    </p>
  );
};

/**
 * Block de code ou texte mono
 */
const Code = ({ children, className = '', ...props }) => {
  return (
    <code
      className={`code ${className}`.trim()}
      style={{
        ...baseStyles,
        ...monoStyles,
        backgroundColor: 'var(--surface, #f5faf7)',
        padding: 'var(--space-xxs, 2px) var(--space-xs, 4px)',
        borderRadius: 'var(--radius-sm, 3px)',
        border: '1px solid var(--border, #e5edf5)',
      }}
      data-od-id="code"
      {...props}
    >
      {children}
    </code>
  );
};

// ============================================================================
// LAYOUT COMPONENTS
// ============================================================================

/**
 * Diviseur horizontal
 */
const Divider = ({ className = '', ...props }) => {
  return (
    <hr
      className={`divider ${className}`.trim()}
      style={{
        ...baseStyles,
        border: 'none',
        borderTop: '1px solid var(--border, #e5edf5)',
        margin: 'var(--space-md, 12px) 0',
      }}
      data-od-id="divider"
      {...props}
    />
  );
};

/**
 * Espaceur flexible
 */
const Spacer = ({ size = 'md', className = '', ...props }) => {
  const sizeMap = {
    xs: 'var(--space-xs, 12px)',
    sm: 'var(--space-sm, 16px)',
    md: 'var(--space-md, 24px)',
    lg: 'var(--space-lg, 32px)',
    xl: 'var(--space-xl, 48px)',
  };

  return (
    <div
      className={`spacer spacer-${size} ${className}`.trim()}
      style={{
        ...baseStyles,
        height: sizeMap[size] || sizeMap.md,
      }}
      data-od-id="spacer"
      {...props}
    />
  );
};

/**
 * Container responsive
 */
const Container = ({ children, className = '', fluid = false, ...props }) => {
  return (
    <div
      className={`container ${fluid ? 'container-fluid' : ''} ${className}`.trim()}
      style={{
        ...baseStyles,
        width: '100%',
        paddingLeft: 'var(--space-lg, 16px)',
        paddingRight: 'var(--space-lg, 16px)',
        marginLeft: 'auto',
        marginRight: 'auto',
        maxWidth: fluid ? 'none' : '1200px',
      }}
      data-od-id="container"
      {...props}
    >
      {children}
    </div>
  );
};

// ============================================================================
// HOOKS
// ============================================================================

/**
 * Hook pour detecter le breakpoint actuel
 */
const useBreakpoint = () => {
  const [breakpoint, setBreakpoint] = React.useState('md');

  React.useEffect(() => {
    const checkBreakpoint = () => {
      const width = window.innerWidth;
      if (width >= 1280) setBreakpoint('2xl');
      else if (width >= 1024) setBreakpoint('xl');
      else if (width >= 768) setBreakpoint('lg');
      else if (width >= 480) setBreakpoint('md');
      else if (width >= 360) setBreakpoint('sm');
      else setBreakpoint('xs');
    };

    checkBreakpoint();
    window.addEventListener('resize', checkBreakpoint);
    return () => window.removeEventListener('resize', checkBreakpoint);
  }, []);

  return breakpoint;
};

/**
 * Hook pour gérer les états de chargement
 */
const useLoading = (initialState = false) => {
  const [isLoading, setIsLoading] = React.useState(initialState);

  const startLoading = () => setIsLoading(true);
  const stopLoading = () => setIsLoading(false);

  return { isLoading, startLoading, stopLoading };
};

/**
 * Injecte les keyframes CSS nécessaires
 */
const injectKeyframes = () => {
  const style = document.createElement('style');
  style.textContent = `
    @keyframes spin {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }
    @keyframes pulse {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.5; }
    }
    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }
    @keyframes slideInRight {
      from { transform: translateX(100%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
    @keyframes slideOutRight {
      from { transform: translateX(0); opacity: 1; }
      to { transform: translateX(100%); opacity: 0; }
    }
    @keyframes scaleIn {
      from { opacity: 0; transform: scale(0.95); }
      to { opacity: 1; transform: scale(1); }
    }
  `;
  document.head.appendChild(style);
};

// ============================================================================
// EXPORTS
// ============================================================================

// Exporter vers window pour usage avec Babel Standalone
Object.assign(window, {
  // Typography
  H1, H2, H3, Text, Code,
  // Layout
  Divider, Spacer, Container,
  // Hooks
  useBreakpoint, useLoading, injectKeyframes,
});

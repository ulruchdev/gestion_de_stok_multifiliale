/**
 * StockMaster CM - Card Components
 * Design System Inspired by Stripe
 * 
 * Composants de cartes atomiques du Design System
 * Compatible avec Babel Standalone pour utilisation autonome
 * 
 * Usage with Babel Standalone:
 * <script type="text/babel" src="components/Cards.jsx"></script>
 * Then use: window.Card, window.CardHeader, window.CardBody, window.CardFooter
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

// ============================================================================
// CARDS
// ============================================================================

/**
 * Carte standard
 * @param {Object} props - Props du composant
 * @param {node} props.children - Contenu de la carte
 * @param {string} props.className - Classes CSS additionnelles
 * @param {string} props.variant - Variante (standard, featured, minimal)
 * @param {string} props.padding - Padding (none, xs, sm, md, lg, xxl)
 * @param {function} props.onClick - Handler de clic (pour cartes cliquables)
 * @param {boolean} props.hoverable - Effet hover sur la carte
 */
const Card = React.forwardRef(({
  children,
  className = '',
  variant = 'standard',
  padding = 'md',
  onClick,
  hoverable = false,
  ...props
}, ref) => {
  const cardStyles = {
    ...baseStyles,
    backgroundColor: variant === 'featured' ? 'var(--accent, #1c1e54)' : 'var(--surface, #f5faf7)',
    border: variant === 'featured' 
      ? '1px solid var(--accent, #1c1e54)' 
      : '1px solid var(--border, #e5edf5)',
    borderRadius: 'var(--radius-md, 8px)',
    color: variant === 'featured' ? '#ffffff' : 'var(--fg, #262626)',
    boxShadow: variant === 'featured' 
      ? 'var(--shadow-lg, rgba(50,50,93,0.25) 0px 30px 45px -30px, rgba(0,0,0,0.1) 0px 18px 36px -18px)'
      : 'var(--shadow-sm, rgba(50,50,93,0.06) 0px 3px 6px)',
    transition: 'box-shadow var(--duration-normal, 200ms) ease-out, transform var(--duration-normal, 200ms) ease-out',
    cursor: onClick && !hoverable ? 'pointer' : 'default',
    ...(hoverable && onClick && {
      ':hover': {
        boxShadow: 'var(--shadow-md, rgba(50,50,93,0.08) 0px 15px 35px)',
        transform: 'translateY(-2px)',
      },
    }),
  };

  const paddingMap = {
    none: '0',
    xs: 'var(--space-xs, 4px)',
    sm: 'var(--space-sm, 8px)',
    md: 'var(--space-lg, 16px)',
    lg: 'var(--space-xl, 24px)',
    xxl: 'var(--space-xxl, 32px)',
  };

  return (
    <div
      ref={ref}
      onClick={onClick}
      className={`card card-${variant} ${className}`.trim()}
      style={{
        ...cardStyles,
        padding: paddingMap[padding],
      }}
      data-od-id="card"
      {...props}
    >
      {children}
    </div>
  );
});

Card.displayName = 'Card';

Card.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
  variant: PropTypes.oneOf(['standard', 'featured', 'minimal']),
  padding: PropTypes.oneOf(['none', 'xs', 'sm', 'md', 'lg', 'xxl']),
  onClick: PropTypes.func,
  hoverable: PropTypes.bool,
};

/**
 * Header de carte
 * @param {Object} props - Props du composant
 * @param {node} props.children - Contenu du header
 * @param {string} props.className - Classes CSS additionnelles
 */
const CardHeader = ({ children, className = '', ...props }) => {
  return (
    <div
      className={`card-header ${className}`.trim()}
      style={{
        ...baseStyles,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        marginBottom: 'var(--space-md, 12px)',
        paddingBottom: 'var(--space-md, 12px)',
        borderBottom: '1px solid var(--border, #e5edf5)',
      }}
      data-od-id="card-header"
      {...props}
    >
      {children}
    </div>
  );
};

CardHeader.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
};

/**
 * Contenu de carte
 * @param {Object} props - Props du composant
 * @param {node} props.children - Contenu du body
 * @param {string} props.className - Classes CSS additionnelles
 */
const CardBody = ({ children, className = '', ...props }) => {
  return (
    <div
      className={`card-body ${className}`.trim()}
      style={{
        ...baseStyles,
      }}
      data-od-id="card-body"
      {...props}
    >
      {children}
    </div>
  );
};

CardBody.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
};

/**
 * Footer de carte
 * @param {Object} props - Props du composant
 * @param {node} props.children - Contenu du footer
 * @param {string} props.className - Classes CSS additionnelles
 */
const CardFooter = ({ children, className = '', ...props }) => {
  return (
    <div
      className={`card-footer ${className}`.trim()}
      style={{
        ...baseStyles,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'flex-end',
        gap: 'var(--space-sm, 8px)',
        marginTop: 'var(--space-md, 12px)',
        paddingTop: 'var(--space-md, 12px)',
        borderTop: '1px solid var(--border, #e5edf5)',
      }}
      data-od-id="card-footer"
      {...props}
    >
      {children}
    </div>
  );
};

CardFooter.propTypes = {
  children: PropTypes.node.isRequired,
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
  Card,
  CardHeader,
  CardBody,
  CardFooter,
});

/**
 * StockMaster CM - Buttons Components
 * Design System Inspired by Stripe
 * 
 * Boutons atomiques du Design System
 * 
 * Usage with Babel Standalone:
 * <script type="text/babel" src="components/Buttons.jsx"></script>
 * Then use: window.Button, window.IconButton, window.ButtonGroup
 */

// ============================================================================
// REACT REFERENCES
// ============================================================================

// Reference to window.React for Babel Standalone compatibility
const React = window.React;

// Mock PropTypes for Babel Standalone compatibility
const PropTypes = window.PropTypes || {};
if (typeof PropTypes.elementType === 'undefined') PropTypes.elementType = () => {};
if (typeof PropTypes.node === 'undefined') PropTypes.node = () => {};
if (typeof PropTypes.func === 'undefined') PropTypes.func = () => {};
if (typeof PropTypes.bool === 'undefined') PropTypes.bool = () => {};
if (typeof PropTypes.string === 'undefined') PropTypes.string = () => {};
if (typeof PropTypes.number === 'undefined') PropTypes.number = () => {};
if (typeof PropTypes.oneOf === 'undefined') PropTypes.oneOf = () => {};

// ============================================================================
// UTILITY STYLES
// ============================================================================

/**
 * Applique les styles de base avec les tokens CSS
 * Tous les composants heritent de cette classe
 */
const baseStyles = {
  boxSizing: 'border-box',
  margin: 0,
  padding: 0,
};

// ============================================================================
// BUTTONS
// ============================================================================

/**
 * Bouton principal
 * @param {Object} props - Props du composant
 * @param {string} props.children - Contenu du bouton
 * @param {function} props.onClick - Handler de clic
 * @param {string} props.type - Type du bouton (button, submit, reset)
 * @param {boolean} props.disabled - Etat desactive
 * @param {string} props.className - Classes CSS additionnelles
 * @param {string} props.size - Taille (sm, md, lg)
 * @param {string} props.variant - Variante (primary, secondary, ghost, danger, icon)
 */
const Button = React.forwardRef(({
  children,
  onClick,
  type = 'button',
  disabled = false,
  className = '',
  size = 'md',
  variant = 'primary',
  ...props
}, ref) => {
  const baseButtonStyle = {
    ...baseStyles,
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 'var(--space-xs, 4px)',
    border: 'none',
    cursor: disabled ? 'not-allowed' : 'pointer',
    transition: 'all var(--duration-normal, 200ms) ease-out',
    whiteSpace: 'nowrap',
    opacity: disabled ? 0.5 : 1,
    pointerEvents: disabled ? 'none' : 'auto',
    fontFamily: 'var(--font-body, system-ui, -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif)',
    fontWeight: 'var(--text-body-weight, 400)',
    lineHeight: 1.0,
    ...getButtonStyles(variant, size),
  };

  return (
    <button
      ref={ref}
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={`btn btn-${variant} btn-${size} ${className}`.trim()}
      style={baseButtonStyle}
      data-od-id="button"
      {...props}
    >
      {children}
    </button>
  );
});

Button.displayName = 'Button';

/**
 * Retourne les styles en fonction de la variante et de la taille
 */
function getButtonStyles(variant, size) {
  const sizes = {
    sm: {
      padding: 'var(--space-xs, 4px) var(--space-md, 12px)',
      fontSize: 'var(--text-body-sm, 14px)',
      minHeight: '32px',
    },
    md: {
      padding: 'var(--space-sm, 8px) var(--space-lg, 16px)',
      fontSize: 'var(--text-body, 16px)',
      minHeight: '40px',
    },
    lg: {
      padding: 'var(--space-md, 12px) var(--space-xl, 24px)',
      fontSize: 'var(--text-body-lg, 18px)',
      minHeight: '48px',
    },
  };

  const variants = {
    primary: {
      backgroundColor: 'var(--accent, #1c1e54)',
      color: '#ffffff',
      borderRadius: 'var(--radius-sm, 4px)',
      ':hover': {
        backgroundColor: 'color-mix(in srgb, var(--accent, #1c1e54) 90%, black)',
        transform: 'translateY(-1px)',
      },
      ':active': {
        transform: 'scale(0.98)',
      },
    },
    secondary: {
      backgroundColor: 'var(--surface, #f5faf7)',
      color: 'var(--accent, #1c1e54)',
      border: '1px solid var(--border, #e5edf5)',
      borderRadius: 'var(--radius-sm, 4px)',
      ':hover': {
        backgroundColor: 'color-mix(in srgb, var(--surface, #f5faf7) 95%, var(--accent, #1c1e54))',
        borderColor: 'var(--accent, #1c1e54)',
      },
    },
    ghost: {
      backgroundColor: 'transparent',
      color: 'var(--accent, #1c1e54)',
      borderRadius: 'var(--radius-sm, 4px)',
      ':hover': {
        backgroundColor: 'rgba(28, 30, 84, 0.1)',
      },
    },
    danger: {
      backgroundColor: 'var(--error, #ff4d4f)',
      color: '#ffffff',
      borderRadius: 'var(--radius-sm, 4px)',
      ':hover': {
        backgroundColor: 'color-mix(in srgb, var(--error, #ff4d4f) 90%, black)',
        transform: 'translateY(-1px)',
      },
      ':active': {
        transform: 'scale(0.98)',
      },
    },
    icon: {
      backgroundColor: 'transparent',
      color: 'var(--accent, #1c1e54)',
      borderRadius: 'var(--radius-sm, 4px)',
      padding: 'var(--space-sm, 8px)',
      minHeight: '40px',
      minWidth: '40px',
      ':hover': {
        backgroundColor: 'rgba(28, 30, 84, 0.1)',
      },
    },
  };

  return {
    ...sizes[size],
    ...variants[variant],
  };
}

/**
 * Bouton avec icône (Lucide React)
 * @param {Object} props - Props du composant
 * @param {elementType} props.icon - Composant icône
 * @param {function} props.onClick - Handler de clic
 * @param {boolean} props.disabled - Etat desactive
 * @param {string} props.className - Classes CSS additionnelles
 * @param {string} props.size - Taille (sm, md, lg)
 * @param {string} props.variant - Variante (primary, secondary, ghost, danger)
 * @param {string} props.label - Libellé pour accessibilité
 */
const IconButton = React.forwardRef(({
  icon: Icon,
  onClick,
  disabled = false,
  className = '',
  size = 'md',
  variant = 'ghost',
  label,
  ...props
}, ref) => {
  return (
    <Button
      ref={ref}
      onClick={onClick}
      disabled={disabled}
      className={`icon-btn ${className}`.trim()}
      size={size}
      variant={variant}
      aria-label={label}
      data-od-id="icon-button"
      {...props}
    >
      {Icon && <Icon size={size === 'sm' ? 16 : size === 'lg' ? 24 : 20} />}
      {label && <span>{label}</span>}
    </Button>
  );
});

IconButton.displayName = 'IconButton';

IconButton.propTypes = {
  icon: PropTypes.elementType,
  onClick: PropTypes.func,
  disabled: PropTypes.bool,
  className: PropTypes.string,
  size: PropTypes.oneOf(['sm', 'md', 'lg']),
  variant: PropTypes.oneOf(['primary', 'secondary', 'ghost', 'danger']),
  label: PropTypes.string,
};

/**
 * Groupe de boutons
 * @param {Object} props - Props du composant
 * @param {node} props.children - Enfants (boutons)
 * @param {string} props.className - Classes CSS additionnelles
 */
const ButtonGroup = ({ children, className = '', ...props }) => {
  return (
    <div
      className={`btn-group ${className}`.trim()}
      style={{
        ...baseStyles,
        display: 'inline-flex',
        gap: 'var(--space-xs, 4px)',
        flexWrap: 'wrap',
      }}
      data-od-id="button-group"
      {...props}
    >
      {React.Children.map(children, (child, index) => (
        <React.Fragment key={index}>{child}</React.Fragment>
      ))}
    </div>
  );
};

ButtonGroup.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
};

// ============================================================================
// EXPORTS
// ============================================================================

// Exporter vers window pour usage avec Babel Standalone
Object.assign(window, {
  Button,
  IconButton,
  ButtonGroup,
  getButtonStyles,
});

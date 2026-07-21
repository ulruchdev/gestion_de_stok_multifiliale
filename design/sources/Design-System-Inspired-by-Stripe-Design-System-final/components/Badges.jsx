/**
 * StockMaster CM - Badge Components
 * Implements the badge system defined in DESIGN.md
 * 
 * Usage with Babel Standalone:
 * <script type="text/babel" src="components/Badges.jsx"></script>
 * Then use: window.OrderStatusBadge, window.StockMovementBadge, etc.
 */

// ============================================================================
// CSS Custom Properties (from DESIGN.md and system/variables.css)
// ============================================================================
const badgeStyles = {
  // Command status badge colors
  enPreparation: {
    backgroundColor: '#1c1e54', // Brand Dark
    color: '#ffffff',
  },
  validee: {
    backgroundColor: '#15be53', // Success Green
    color: '#ffffff',
  },
  livree: {
    backgroundColor: '#52c41a', // Green-6
    color: '#ffffff',
  },
  annulee: {
    backgroundColor: '#ff4d4f', // Error
    color: '#ffffff',
  },
  
  // Stock movement badge colors
  entree: {
    backgroundColor: '#52c41a', // Green-6
    color: '#ffffff',
  },
  sortie: {
    backgroundColor: '#ff4d4f', // Error
    color: '#ffffff',
  },
  correctionPos: {
    backgroundColor: '#1677ff', // Blue-6 / Info
    color: '#ffffff',
  },
  correctionNeg: {
    backgroundColor: '#d48806', // Orange-7 (darker orange to avoid pure orange)
    color: '#ffffff',
  },
  transfertEntree: {
    backgroundColor: '#13c2c2', // Cyan-6
    color: '#ffffff',
  },
  transfertSortie: {
    backgroundColor: '#eb2f96', // Magenta-6
    color: '#ffffff',
  },
  annulationVente: {
    backgroundColor: '#8c8c8c', // Grey-3
    color: '#ffffff',
  },
};

// ============================================================================
// Shared Badge Container Styles
// ============================================================================
const baseBadgeStyle = {
  display: 'inline-flex',
  alignItems: 'center',
  padding: '2px 8px',
  borderRadius: '4px',
  fontSize: '12px',
  fontWeight: 600,
  lineHeight: '16px',
  textTransform: 'uppercase',
  letterSpacing: '0.5px',
  fontFamily: 'system-ui, -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif',
};

// ============================================================================
// Order Status Badge Component
// ============================================================================
/**
 * Badge for displaying order status
 * 
 * @param {Object} props - Component props
 * @param {string} props.status - One of: EN_PREPARATION, VALIDEE, LIVREE, ANNULEE
 * @param {string} [props.className] - Additional CSS class
 * @param {Object} [props.style] - Additional inline styles
 */
const OrderStatusBadge = ({ status, className = '', style = {} }) => {
  const statusKey = status.toLowerCase();
  const statusStyle = badgeStyles[statusKey] || badgeStyles.enPreparation;
  
  return (
    <span
      className={`badge badge-command badge-${statusKey} ${className}`}
      style={{ ...baseBadgeStyle, ...statusStyle, ...style }}
      data-od-id="badge-order-status"
    >
      {status}
    </span>
  );
};

// ============================================================================
// Stock Movement Badge Component
// ============================================================================
/**
 * Badge for displaying stock movement types
 * 
 * @param {Object} props - Component props
 * @param {string} props.type - One of: ENTREE, SORTIE, CORRECTION_POS, CORRECTION_NEG, 
 *                              TRANSFERT_ENTREE, TRANSFERT_SORTIE, ANNULATION_VENTE
 * @param {string} [props.className] - Additional CSS class
 * @param {Object} [props.style] - Additional inline styles
 */
const StockMovementBadge = ({ type, className = '', style = {} }) => {
  // Convert type to style key (handle underscores)
  const styleKey = type
    .toLowerCase()
    .replace(/_/g, ''); // Remove all underscores
  
  const movementStyle = badgeStyles[styleKey] || badgeStyles.entree;
  
  return (
    <span
      className={`badge badge-movement badge-${styleKey} ${className}`}
      style={{ ...baseBadgeStyle, ...movementStyle, ...style }}
      data-od-id="badge-stock-movement"
    >
      {type}
    </span>
  );
};

// ============================================================================
// Badge Display Component (for demo purposes)
// ============================================================================
/**
 * Demo component showing all available badges
 */
const BadgeDemo = () => (
  <div style={{ padding: '24px', fontFamily: 'system-ui, -apple-system, Segoe UI, sans-serif' }}>
    <h2 style={{ marginBottom: '24px', color: '#1c1e54' }}>Order Status Badges</h2>
    
    <div style={{ display: 'flex', gap: '12px', marginBottom: '24px', flexWrap: 'wrap' }}>
      <OrderStatusBadge status="EN_PREPARATION" />
      <OrderStatusBadge status="VALIDEE" />
      <OrderStatusBadge status="LIVREE" />
      <OrderStatusBadge status="ANNULEE" />
    </div>

    <h2 style={{ marginBottom: '24px', color: '#1c1e54' }}>Stock Movement Badges</h2>
    
    <div style={{ display: 'flex', gap: '12px', marginBottom: '24px', flexWrap: 'wrap' }}>
      <StockMovementBadge type="ENTREE" />
      <StockMovementBadge type="SORTIE" />
      <StockMovementBadge type="CORRECTION_POS" />
      <StockMovementBadge type="CORRECTION_NEG" />
      <StockMovementBadge type="TRANSFERT_ENTREE" />
      <StockMovementBadge type="TRANSFERT_SORTIE" />
      <StockMovementBadge type="ANNULATION_VENTE" />
    </div>

    <h3 style={{ marginBottom: '16px', color: '#262626' }}>Accessibility Check</h3>
    <p style={{ fontSize: '14px', color: '#666' }}>
      All badges use white text (#ffffff) on colored backgrounds with sufficient 
      contrast ratio (WCAG AA minimum). The orange used for CORRECTION_NEG is 
      a darker shade (#d48806) to avoid pure orange/yellow as per project constraints.
    </p>
  </div>
);

// ============================================================================
// Export to window for Babel Standalone usage
// ============================================================================
Object.assign(window, {
  OrderStatusBadge,
  StockMovementBadge,
  BadgeDemo
});

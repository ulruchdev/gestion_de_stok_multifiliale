/**
 * StockMaster CM - Table Components
 * Design System Inspired by Stripe
 * 
 * Composants de tableau responsive qui se transforment en cartes sur mobile
 * Respect la regle: NO tables below 768px
 * 
 * Compatible avec Babel Standalone pour utilisation autonome
 * 
 * Usage with Babel Standalone:
 * <script type="text/babel" src="components/Tables.jsx"></script>
 * Then use: window.Table, window.MobileTable, etc.
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
// UTILITY FUNCTIONS
// ============================================================================

/**
 * Detecte si on est sur mobile (< 768px)
 */
const useIsMobile = () => {
  const [isMobile, setIsMobile] = React.useState(false);

  React.useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 768);
    };

    checkMobile();
    window.addEventListener('resize', checkMobile);
    return () => window.removeEventListener('resize', checkMobile);
  }, []);

  return isMobile;
};

/**
 * Formate un montant en XAF (sans decimales)
 */
const formatXAF = (amount) => {
  if (amount === null || amount === undefined) return '';
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'XAF',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(amount);
};

/**
 * Formate une date
 */
const formatDate = (date) => {
  if (!date) return '';
  return new Intl.DateTimeFormat('fr-FR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  }).format(new Date(date));
};

/**
 * Formate une date avec heure
 */
const formatDateTime = (date) => {
  if (!date) return '';
  return new Intl.DateTimeFormat('fr-FR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(date));
};

// ============================================================================
// TABLE COMPONENTS (Desktop Only)
// ============================================================================

/**
 * Tableau standard (Desktop > 768px)
 * Se transforme automatiquement en liste de cartes sur mobile
 */
const Table = ({
  columns = [],
  data = [],
  keyField = 'id',
  mobileCardRender,
  className = '',
  variant = 'standard',
  ...props
}) => {
  const isMobile = useIsMobile();

  // Si mobile, afficher la version cartes
  if (isMobile) {
    return (
      <MobileTable
        columns={columns}
        data={data}
        keyField={keyField}
        renderCard={mobileCardRender}
        className={className}
        {...props}
      />
    );
  }

  // Version desktop (tableau)
  return (
    <div
      className={`table-container table-${variant} ${className}`.trim()}
      style={{
        boxSizing: 'border-box',
        width: '100%',
        overflowX: 'auto',
      }}
      data-od-id="table-container"
      {...props}
    >
      <table
        className="table"
        style={{
          width: '100%',
          borderCollapse: 'collapse',
          backgroundColor: 'var(--surface, #f5faf7)',
          border: '1px solid var(--border, #e5edf5)',
          borderRadius: 'var(--radius-sm, 4px)',
          fontFamily: 'var(--font-body, system-ui, -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif)',
        }}
        data-od-id="table"
      >
        <TableHeader columns={columns} />
        <TableBody columns={columns} data={data} keyField={keyField} />
      </table>
    </div>
  );
};

Table.propTypes = {
  columns: PropTypes.arrayOf(
    PropTypes.shape({
      key: PropTypes.string.isRequired,
      header: PropTypes.string.isRequired,
      sortable: PropTypes.bool,
      numeric: PropTypes.bool,
      render: PropTypes.func,
    })
  ).isRequired,
  data: PropTypes.array.isRequired,
  keyField: PropTypes.string,
  mobileCardRender: PropTypes.func,
  className: PropTypes.string,
  variant: PropTypes.oneOf(['standard', 'compact', 'bordered']),
};

/**
 * Header du tableau
 */
const TableHeader = ({ columns = [] }) => {
  return (
    <thead data-od-id="table-header">
      <tr
        style={{
          backgroundColor: 'var(--accent, #1c1e54)',
          color: '#ffffff',
        }}
      >
        {columns.map((column) => (
          <th
            key={column.key}
            style={{
              padding: 'var(--space-sm, 8px) var(--space-md, 12px)',
              textAlign: column.numeric ? 'right' : 'left',
              fontSize: 'var(--text-body-sm, 14px)',
              fontWeight: 600,
              borderBottom: '1px solid rgba(255, 255, 255, 0.2)',
              whiteSpace: 'nowrap',
            }}
            data-od-id={`table-header-${column.key}`}
          >
            {column.header}
            {column.sortable && (
              <span style={{ marginLeft: '4px', opacity: 0.7 }}>↕</span>
            )}
          </th>
        ))}
      </tr>
    </thead>
  );
};

TableHeader.propTypes = {
  columns: PropTypes.arrayOf(
    PropTypes.shape({
      key: PropTypes.string.isRequired,
      header: PropTypes.string.isRequired,
      sortable: PropTypes.bool,
      numeric: PropTypes.bool,
    })
  ),
};

/**
 * Corps du tableau
 */
const TableBody = ({ columns = [], data = [], keyField = 'id' }) => {
  return (
    <tbody data-od-id="table-body">
      {data.length === 0 ? (
        <tr>
          <td
            colSpan={columns.length}
            style={{
              padding: 'var(--space-xxl, 32px)',
              textAlign: 'center',
              color: 'var(--muted, #93cba8)',
              fontSize: 'var(--text-body-sm, 14px)',
            }}
            data-od-id="table-empty"
          >
            Aucune donnee disponible
          </td>
        </tr>
      ) : (
        data.map((row, rowIndex) => (
          <tr
            key={row[keyField] || rowIndex}
            style={{
              borderBottom: '1px solid var(--border, #e5edf5)',
              ':last-child': {
                borderBottom: 'none',
              },
              ':hover': {
                backgroundColor: 'color-mix(in srgb, var(--surface, #f5faf7) 95%, var(--accent, #1c1e54))',
              },
            }}
            data-od-id={`table-row-${rowIndex}`}
          >
            {columns.map((column) => {
              const value = column.render ? column.render(row) : row[column.key];
              return (
                <td
                  key={column.key}
                  style={{
                    padding: 'var(--space-sm, 8px) var(--space-md, 12px)',
                    textAlign: column.numeric ? 'right' : 'left',
                    fontSize: 'var(--text-body-sm, 14px)',
                    color: 'var(--fg, #262626)',
                    whiteSpace: 'nowrap',
                    fontFamily: column.numeric 
                      ? 'var(--font-mono, ui-monospace, SFMono-Regular, Menlo, monospace)'
                      : 'var(--font-body, system-ui, -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif)',
                  }}
                  data-od-id={`table-cell-${column.key}-${rowIndex}`}
                >
                  {value}
                </td>
              );
            })}
          </tr>
        ))
      )}
    </tbody>
  );
};

TableBody.propTypes = {
  columns: PropTypes.arrayOf(
    PropTypes.shape({
      key: PropTypes.string.isRequired,
      numeric: PropTypes.bool,
      render: PropTypes.func,
    })
  ),
  data: PropTypes.array,
  keyField: PropTypes.string,
};

// ============================================================================
// MOBILE TABLE (Card List)
// ============================================================================

/**
 * Liste de cartes pour mobile (remplace le tableau)
 */
const MobileTable = ({
  columns = [],
  data = [],
  keyField = 'id',
  renderCard,
  className = '',
  ...props
}) => {
  // Utiliser le render personalise si fourni
  if (renderCard) {
    return (
      <div
        className={`mobile-table ${className}`.trim()}
        style={{
          boxSizing: 'border-box',
          display: 'flex',
          flexDirection: 'column',
          gap: 'var(--space-sm, 8px)',
        }}
        data-od-id="mobile-table"
        {...props}
      >
        {data.map((row, index) => (
          <React.Fragment key={row[keyField] || index}>
            {renderCard(row, index)}
          </React.Fragment>
        ))}
      </div>
    );
  }

  // Render par defaut: carte avec grille label/valeur
  return (
    <div
      className={`mobile-table ${className}`.trim()}
      style={{
        boxSizing: 'border-box',
        display: 'flex',
        flexDirection: 'column',
        gap: 'var(--space-sm, 8px)',
      }}
      data-od-id="mobile-table"
      {...props}
    >
      {data.length === 0 ? (
        <div
          style={{
            padding: 'var(--space-lg, 16px)',
            textAlign: 'center',
            color: 'var(--muted, #93cba8)',
            fontSize: 'var(--text-body-sm, 14px)',
            backgroundColor: 'var(--surface, #f5faf7)',
            border: '1px solid var(--border, #e5edf5)',
            borderRadius: 'var(--radius-md, 8px)',
          }}
          data-od-id="mobile-table-empty"
        >
          Aucune donnee disponible
        </div>
      ) : (
        data.map((row, rowIndex) => (
          <MobileTableCard
            key={row[keyField] || rowIndex}
            row={row}
            columns={columns}
            rowIndex={rowIndex}
          />
        ))
      )}
    </div>
  );
};

MobileTable.propTypes = {
  columns: PropTypes.arrayOf(
    PropTypes.shape({
      key: PropTypes.string.isRequired,
      header: PropTypes.string.isRequired,
      numeric: PropTypes.bool,
      render: PropTypes.func,
    })
  ).isRequired,
  data: PropTypes.array.isRequired,
  keyField: PropTypes.string,
  renderCard: PropTypes.func,
  className: PropTypes.string,
};

/**
 * Carte individuelle pour la version mobile
 */
const MobileTableCard = ({ row, columns = [], rowIndex }) => {
  return (
    <div
      style={{
        backgroundColor: 'var(--surface, #f5faf7)',
        border: '1px solid var(--border, #e5edf5)',
        borderRadius: 'var(--radius-md, 8px)',
        padding: 'var(--space-md, 12px)',
        boxShadow: 'var(--shadow-sm, rgba(50,50,93,0.06) 0px 3px 6px)',
      }}
      data-od-id={`mobile-card-${rowIndex}`}
    >
      {columns.map((column, colIndex) => {
        const value = column.render ? column.render(row) : row[column.key];
        const isLast = colIndex === columns.length - 1;

        return (
          <div
            key={column.key}
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              padding: isLast ? '0' : 'var(--space-xxs, 2px) 0',
              borderBottom: isLast ? 'none' : '1px solid var(--border, #e5edf5)',
              paddingBottom: isLast ? '0' : 'var(--space-xs, 4px)',
              marginBottom: isLast ? '0' : 'var(--space-xs, 4px)',
            }}
            data-od-id={`mobile-card-item-${column.key}-${rowIndex}`}
          >
            <span
              style={{
                fontSize: 'var(--text-body-sm, 14px)',
                color: 'var(--muted, #93cba8)',
                fontWeight: 500,
              }}
              data-od-id={`mobile-card-label-${column.key}-${rowIndex}`}
            >
              {column.header}:
            </span>
            <span
              style={{
                fontSize: 'var(--text-body-sm, 14px)',
                color: 'var(--fg, #262626)',
                fontWeight: 600,
                fontFamily: column.numeric 
                  ? 'var(--font-mono, ui-monospace, SFMono-Regular, Menlo, monospace)'
                  : 'var(--font-body, system-ui, -apple-system, Segoe UI, Helvetica Neue, Arial, sans-serif)',
              }}
              data-od-id={`mobile-card-value-${column.key}-${rowIndex}`}
            >
              {value}
            </span>
          </div>
        );
      })}
    </div>
  );
};

MobileTableCard.propTypes = {
  row: PropTypes.object.isRequired,
  columns: PropTypes.arrayOf(
    PropTypes.shape({
      key: PropTypes.string.isRequired,
      header: PropTypes.string.isRequired,
      numeric: PropTypes.bool,
      render: PropTypes.func,
    })
  ),
  rowIndex: PropTypes.number,
};

// ============================================================================
// SPECIFIC TABLES FOR STOCKMASTER CM
// ============================================================================

/**
 * Tableau des articles
 */
const ArticlesTable = ({
  articles = [],
  onEdit,
  onDelete,
  onView,
  className = '',
  ...props
}) => {
  const columns = [
    {
      key: 'code_article',
      header: 'Code',
      numeric: false,
      render: (row) => <code style={monoStyle}>{row.code_article}</code>,
    },
    {
      key: 'designation',
      header: 'Designation',
      numeric: false,
    },
    {
      key: 'prix_vente_ht',
      header: 'Prix HT',
      numeric: true,
      render: (row) => formatXAF(row.prix_vente_ht),
    },
    {
      key: 'stock_actuel',
      header: 'Stock',
      numeric: true,
      render: (row) => row.stock_actuel,
    },
    {
      key: 'seuil_alerte',
      header: 'Seuil Alerte',
      numeric: true,
      render: (row) => row.seuil_alerte,
    },
    {
      key: 'categorie',
      header: 'Categorie',
      numeric: false,
      render: (row) => row.categorie?.designation || '—',
    },
    {
      key: 'actions',
      header: 'Actions',
      numeric: false,
      render: (row) => (
        <div style={{ display: 'flex', gap: '4px', justifyContent: 'flex-end' }}>
          {onView && (
            <button
              onClick={() => onView(row)}
              style={iconButtonStyle}
              aria-label="Voir"
              data-od-id={`article-view-${row.id}`}
            >
              👁️
            </button>
          )}
          {onEdit && (
            <button
              onClick={() => onEdit(row)}
              style={iconButtonStyle}
              aria-label="Modifier"
              data-od-id={`article-edit-${row.id}`}
            >
              ✏️
            </button>
          )}
          {onDelete && (
            <button
              onClick={() => onDelete(row)}
              style={{ ...iconButtonStyle, color: 'var(--error, #ff4d4f)' }}
              aria-label="Supprimer"
              data-od-id={`article-delete-${row.id}`}
            >
              🗑️
            </button>
          )}
        </div>
      ),
    },
  ];

  const mobileCardStyle = {
    backgroundColor: 'var(--surface, #f5faf7)',
    border: '1px solid var(--border, #e5edf5)',
    borderRadius: 'var(--radius-md, 8px)',
    padding: 'var(--space-md, 12px)',
  };

  return (
    <Table
      columns={columns}
      data={articles}
      keyField="id"
      className={className}
      mobileCardRender={(row) => (
        <div style={mobileCardStyle} data-od-id={`mobile-article-card-${row.id}`}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-sm, 8px)' }}>
            <span style={{ fontWeight: 600, fontSize: 'var(--text-body, 16px)' }}>
              {row.designation}
            </span>
            <code style={monoStyle}>{row.code_article}</code>
          </div>
          <div style={{ display: 'flex', gap: 'var(--space-md, 12px)' }}>
            <div>
              <div style={{ color: 'var(--muted, #93cba8)', fontSize: 'var(--text-body-sm, 14px)' }}>Prix HT</div>
              <div style={{ fontWeight: 600 }}>{formatXAF(row.prix_vente_ht)}</div>
            </div>
            <div>
              <div style={{ color: 'var(--muted, #93cba8)', fontSize: 'var(--text-body-sm, 14px)' }}>Stock</div>
              <div style={{ fontWeight: 600 }}>{row.stock_actuel}</div>
            </div>
            <div>
              <div style={{ color: 'var(--muted, #93cba8)', fontSize: 'var(--text-body-sm, 14px)' }}>Seuil</div>
              <div style={{ fontWeight: 600 }}>{row.seuil_alerte}</div>
            </div>
          </div>
          {onView && (
            <div style={{ marginTop: 'var(--space-sm, 8px)' }}>
              <button onClick={() => onView(row)} style={{ ...buttonStyle, width: '100%' }}>
                Voir les details
              </button>
            </div>
          )}
        </div>
      )}
      {...props}
    />
  );
};

ArticlesTable.propTypes = {
  articles: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.number.isRequired,
      code_article: PropTypes.string.isRequired,
      designation: PropTypes.string.isRequired,
      prix_vente_ht: PropTypes.number.isRequired,
      stock_actuel: PropTypes.number.isRequired,
      seuil_alerte: PropTypes.number.isRequired,
      categorie: PropTypes.shape({
        designation: PropTypes.string,
      }),
    })
  ),
  onEdit: PropTypes.func,
  onDelete: PropTypes.func,
  onView: PropTypes.func,
  className: PropTypes.string,
};

/**
 * Tableau des commandes
 */
const CommandesTable = ({
  commandes = [],
  onView,
  onEdit,
  className = '',
  ...props
}) => {
  const columns = [
    {
      key: 'code',
      header: 'Reference',
      numeric: false,
      render: (row) => <code style={monoStyle}>{row.code}</code>,
    },
    {
      key: 'date_commande',
      header: 'Date',
      numeric: false,
      render: (row) => formatDate(row.date_commande),
    },
    {
      key: 'fournisseur',
      header: 'Fournisseur',
      numeric: false,
      render: (row) => row.fournisseur?.raison_sociale || '—',
    },
    {
      key: 'total_ht',
      header: 'Total HT',
      numeric: true,
      render: (row) => formatXAF(row.total_ht),
    },
    {
      key: 'etat_commande',
      header: 'Etat',
      numeric: false,
      render: (row) => <span>{getEtatLabel(row.etat_commande)}</span>,
    },
    {
      key: 'actions',
      header: 'Actions',
      numeric: false,
      render: (row) => (
        <div style={{ display: 'flex', gap: '4px', justifyContent: 'flex-end' }}>
          {onView && (
            <button
              onClick={() => onView(row)}
              style={iconButtonStyle}
              aria-label="Voir"
            >
              👁️
            </button>
          )}
          {onEdit && row.etat_commande === 'EN_PREPARATION' && (
            <button
              onClick={() => onEdit(row)}
              style={iconButtonStyle}
              aria-label="Modifier"
            >
              ✏️
            </button>
          )}
        </div>
      ),
    },
  ];

  return (
    <Table
      columns={columns}
      data={commandes}
      keyField="id"
      className={className}
      mobileCardRender={(row) => (
        <div style={mobileCardStyle} data-od-id={`mobile-commande-card-${row.id}`}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-sm, 8px)' }}>
            <span style={{ fontWeight: 600, fontSize: 'var(--text-body, 16px)' }}>
              Commande {row.code}
            </span>
            <span style={{ fontSize: 'var(--text-body-sm, 14px)' }}>
              {formatDate(row.date_commande)}
            </span>
          </div>
          <div style={{ display: 'flex', gap: 'var(--space-md, 12px)' }}>
            <div>
              <div style={{ color: 'var(--muted, #93cba8)', fontSize: 'var(--text-body-sm, 14px)' }}>Fournisseur</div>
              <div style={{ fontWeight: 600 }}>{row.fournisseur?.raison_sociale || '—'}</div>
            </div>
            <div>
              <div style={{ color: 'var(--muted, #93cba8)', fontSize: 'var(--text-body-sm, 14px)' }}>Total HT</div>
              <div style={{ fontWeight: 600 }}>{formatXAF(row.total_ht)}</div>
            </div>
            <div>
              <div style={{ color: 'var(--muted, #93cba8)', fontSize: 'var(--text-body-sm, 14px)' }}>Etat</div>
              <div style={{ fontWeight: 600 }}>{getEtatLabel(row.etat_commande)}</div>
            </div>
          </div>
        </div>
      )}
      {...props}
    />
  );
};

CommandesTable.propTypes = {
  commandes: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.number.isRequired,
      code: PropTypes.string.isRequired,
      date_commande: PropTypes.string.isRequired,
      fournisseur: PropTypes.shape({
        raison_sociale: PropTypes.string,
      }),
      total_ht: PropTypes.number.isRequired,
      etat_commande: PropTypes.string.isRequired,
    })
  ),
  onView: PropTypes.func,
  onEdit: PropTypes.func,
  className: PropTypes.string,
};

/**
 * Tableau des mouvements de stock
 */
const MouvementsStockTable = ({
  mouvements = [],
  className = '',
  ...props
}) => {
  const columns = [
    {
      key: 'date_mouvement',
      header: 'Date',
      numeric: false,
      render: (row) => formatDateTime(row.date_mouvement),
    },
    {
      key: 'article',
      header: 'Article',
      numeric: false,
      render: (row) => row.article?.designation || '—',
    },
    {
      key: 'type_mouvement',
      header: 'Type',
      numeric: false,
      render: (row) => getTypeMouvementLabel(row.type_mouvement),
    },
    {
      key: 'quantite',
      header: 'Quantite',
      numeric: true,
      render: (row) => row.quantite,
    },
    {
      key: 'utilisateur',
      header: 'Utilisateur',
      numeric: false,
      render: (row) => row.utilisateur?.nom || '—',
    },
    {
      key: 'motif',
      header: 'Motif',
      numeric: false,
    },
  ];

  return (
    <Table
      columns={columns}
      data={mouvements}
      keyField="id"
      className={className}
      mobileCardRender={(row) => (
        <div style={mobileCardStyle} data-od-id={`mobile-mouvement-card-${row.id}`}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-sm, 8px)' }}>
            <span style={{ fontWeight: 600, fontSize: 'var(--text-body, 16px)' }}>
              {row.article?.designation || '—'}
            </span>
            <span style={{ fontSize: 'var(--text-body-sm, 14px)', color: 'var(--muted, #93cba8)' }}>
              {formatDateTime(row.date_mouvement)}
            </span>
          </div>
          <div style={{ display: 'flex', gap: 'var(--space-md, 12px)' }}>
            <div>
              <div style={{ color: 'var(--muted, #93cba8)', fontSize: 'var(--text-body-sm, 14px)' }}>Type</div>
              <div style={{ fontWeight: 600 }}>{getTypeMouvementLabel(row.type_mouvement)}</div>
            </div>
            <div>
              <div style={{ color: 'var(--muted, #93cba8)', fontSize: 'var(--text-body-sm, 14px)' }}>Quantite</div>
              <div style={{ fontWeight: 600 }}>{row.quantite}</div>
            </div>
            <div>
              <div style={{ color: 'var(--muted, #93cba8)', fontSize: 'var(--text-body-sm, 14px)' }}>Utilisateur</div>
              <div style={{ fontWeight: 600 }}>{row.utilisateur?.nom || '—'}</div>
            </div>
          </div>
          {row.motif && (
            <div style={{ marginTop: 'var(--space-sm, 8px)' }}>
              <div style={{ color: 'var(--muted, #93cba8)', fontSize: 'var(--text-body-sm, 14px)' }}>Motif</div>
              <div style={{ fontSize: 'var(--text-body-sm, 14px)' }}>{row.motif}</div>
            </div>
          )}
        </div>
      )}
      {...props}
    />
  );
};

MouvementsStockTable.propTypes = {
  mouvements: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.number.isRequired,
      date_mouvement: PropTypes.string.isRequired,
      article: PropTypes.shape({
        designation: PropTypes.string,
      }),
      type_mouvement: PropTypes.string.isRequired,
      quantite: PropTypes.number.isRequired,
      utilisateur: PropTypes.shape({
        nom: PropTypes.string,
      }),
      motif: PropTypes.string,
    })
  ),
  className: PropTypes.string,
};

// ============================================================================
// HELPER FUNCTIONS & STYLES
// ============================================================================

/**
 * Retourne le label pour un etat de commande
 */
function getEtatLabel(etat) {
  const labels = {
    EN_PREPARATION: 'En preparation',
    VALIDEE: 'Validee',
    LIVREE: 'Livree',
    ANNULEE: 'Annulee',
  };
  return labels[etat] || etat;
}

/**
 * Retourne le label pour un type de mouvement
 */
function getTypeMouvementLabel(type) {
  const labels = {
    ENTREE: 'Entree',
    SORTIE: 'Sortie',
    CORRECTION_POS: 'Correction +',
    CORRECTION_NEG: 'Correction -',
    TRANSFERT_ENTREE: 'Transfert (entree)',
    TRANSFERT_SORTIE: 'Transfert (sortie)',
    ANNULATION_VENTE: 'Annulation vente',
  };
  return labels[type] || type;
}

/**
 * Style pour le code technique
 */
const monoStyle = {
  fontFamily: 'var(--font-mono, ui-monospace, SFMono-Regular, Menlo, monospace)',
  fontSize: 'var(--text-mono, 12px)',
  backgroundColor: 'var(--surface, #f5faf7)',
  padding: 'var(--space-xxs, 2px) var(--space-xs, 4px)',
  borderRadius: 'var(--radius-xs, 3px)',
  color: 'var(--fg, #262626)',
};

/**
 * Style pour les boutons d'icône dans les tableaux
 */
const iconButtonStyle = {
  background: 'none',
  border: 'none',
  padding: 'var(--space-xxs, 2px)',
  cursor: 'pointer',
  fontSize: '16px',
  color: 'var(--accent, #1c1e54)',
  borderRadius: 'var(--radius-xs, 3px)',
  transition: 'all var(--duration-fast, 100ms) ease-out',
  ':hover': {
    backgroundColor: 'rgba(28, 30, 84, 0.1)',
  },
};

/**
 * Style pour les boutons dans les cartes mobile
 */
const buttonStyle = {
  backgroundColor: 'var(--accent, #1c1e54)',
  color: '#ffffff',
  border: 'none',
  padding: 'var(--space-sm, 8px) var(--space-md, 12px)',
  borderRadius: 'var(--radius-sm, 4px)',
  fontSize: 'var(--text-body-sm, 14px)',
  fontWeight: 500,
  cursor: 'pointer',
  transition: 'all var(--duration-fast, 100ms) ease-out',
  ':hover': {
    backgroundColor: 'color-mix(in srgb, var(--accent, #1c1e54) 90%, black)',
  },
};

/**
 * Style pour les cartes mobile
 */
const mobileCardStyle = {
  backgroundColor: 'var(--surface, #f5faf7)',
  border: '1px solid var(--border, #e5edf5)',
  borderRadius: 'var(--radius-md, 8px)',
  padding: 'var(--space-md, 12px)',
  boxShadow: 'var(--shadow-sm, rgba(50,50,93,0.06) 0px 3px 6px)',
};

// ============================================================================
// EXPORTS
// ============================================================================

// Exporter vers window pour usage avec Babel Standalone
Object.assign(window, {
  Table,
  MobileTable,
  TableHeader,
  TableBody,
  MobileTableCard,
  ArticlesTable,
  CommandesTable,
  MouvementsStockTable,
  formatXAF,
  formatDate,
  formatDateTime,
  useIsMobile,
});

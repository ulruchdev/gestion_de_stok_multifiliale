/**
 * StockMaster CM - Input Components
 * Design System Inspired by Stripe
 * 
 * Composants de saisie atomiques du Design System
 * Compatible avec Babel Standalone pour utilisation autonome
 * 
 * Usage with Babel Standalone:
 * <script type="text/babel" src="components/Inputs.jsx"></script>
 * Then use: window.TextInput, window.AmountInput, window.SelectInput, window.Textarea
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
// INPUTS & FORMS
// ============================================================================

/**
 * Champ de texte standard
 * @param {Object} props - Props du composant
 * @param {string} props.type - Type de l'input (text, email, tel, etc.)
 * @param {string|number} props.value - Valeur de l'input
 * @param {function} props.onChange - Handler de changement
 * @param {string} props.placeholder - Placeholder
 * @param {boolean} props.disabled - Etat désactivé
 * @param {boolean} props.error - Etat d'erreur
 * @param {string} props.label - Libellé
 * @param {string} props.helperText - Texte d'aide
 * @param {string} props.errorMessage - Message d'erreur
 * @param {string} props.className - Classes CSS additionnelles
 * @param {string} props.inputMode - Mode de saisie (numeric, text, etc.)
 * @param {string} props.pattern - Pattern de validation
 * @param {boolean} props.required - Champ obligatoire
 */
const TextInput = React.forwardRef(({
  type = 'text',
  value,
  onChange,
  placeholder,
  disabled = false,
  error = false,
  label,
  helperText,
  errorMessage,
  className = '',
  inputMode,
  pattern,
  required = false,
  ...props
}, ref) => {
  const inputId = React.useId();
  
  return (
    <div
      className={`input-group ${className}`.trim()}
      style={{
        ...baseStyles,
        display: 'flex',
        flexDirection: 'column',
        gap: 'var(--space-xxs, 2px)',
      }}
      data-od-id="text-input-group"
    >
      {label && (
        <label
          htmlFor={inputId}
          style={{
            ...textStyles,
            fontSize: 'var(--text-body-sm, 14px)',
            fontWeight: 600,
            color: 'var(--fg, #262626)',
          }}
          data-od-id="input-label"
        >
          {label}
          {required && <span style={{ color: 'var(--error, #ff4d4f)' }}>*</span>}
        </label>
      )}
      
      <input
        ref={ref}
        id={inputId}
        type={type}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        disabled={disabled}
        inputMode={inputMode}
        pattern={pattern}
        required={required}
        className={`input ${error ? 'input-error' : ''} ${disabled ? 'input-disabled' : ''}`}
        style={{
          ...baseStyles,
          ...textStyles,
          width: '100%',
          padding: 'var(--space-sm, 8px) var(--space-md, 12px)',
          backgroundColor: 'var(--surface, #f5faf7)',
          border: error 
            ? '1px solid var(--error, #ff4d4f)' 
            : '1px solid var(--border, #e5edf5)',
          borderRadius: 'var(--radius-sm, 4px)',
          color: 'var(--fg, #262626)',
          fontSize: 'var(--text-body, 16px)',
          lineHeight: 1.5,
          transition: 'border-color var(--duration-fast, 100ms) ease-out, box-shadow var(--duration-fast, 100ms) ease-out',
          opacity: disabled ? 0.6 : 1,
          cursor: disabled ? 'not-allowed' : 'text',
          ':focus': {
            outline: 'none',
            borderColor: 'var(--accent, #1c1e54)',
            boxShadow: '0 0 0 3px rgba(28, 30, 84, 0.1)',
          },
          ':disabled': {
            cursor: 'not-allowed',
          },
        }}
        data-od-id="text-input"
        aria-invalid={error}
        aria-describedby={error ? `${inputId}-error` : helperText ? `${inputId}-helper` : undefined}
        {...props}
      />
      
      {helperText && !error && (
        <span
          id={`${inputId}-helper`}
          style={{
            ...textStyles,
            fontSize: 'var(--text-caption, 13px)',
            color: 'var(--muted, #93cba8)',
          }}
          data-od-id="input-helper"
        >
          {helperText}
        </span>
      )}
      
      {error && errorMessage && (
        <span
          id={`${inputId}-error`}
          style={{
            ...textStyles,
            fontSize: 'var(--text-caption, 13px)',
            color: 'var(--error, #ff4d4f)',
          }}
          data-od-id="input-error"
          role="alert"
        >
          {errorMessage}
        </span>
      )}
    </div>
  );
});

TextInput.displayName = 'TextInput';

TextInput.propTypes = {
  type: PropTypes.string,
  value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  onChange: PropTypes.func,
  placeholder: PropTypes.string,
  disabled: PropTypes.bool,
  error: PropTypes.bool,
  label: PropTypes.string,
  helperText: PropTypes.string,
  errorMessage: PropTypes.string,
  className: PropTypes.string,
  inputMode: PropTypes.string,
  pattern: PropTypes.string,
  required: PropTypes.bool,
};

/**
 * Champ de texte pour les montants (XAF - entiers uniquement)
 * Respecte la règle: "Whole numbers only for XAF"
 */
const AmountInput = React.forwardRef((props, ref) => {
  return (
    <TextInput
      ref={ref}
      type="number"
      inputMode="numeric"
      pattern="[0-9]*"
      step="1"
      min="0"
      placeholder="Montant en XAF"
      {...props}
    />
  );
});

AmountInput.displayName = 'AmountInput';

AmountInput.propTypes = {
  ...TextInput.propTypes,
};

/**
 * Select input
 * @param {Object} props - Props du composant
 * @param {string} props.value - Valeur sélectionnée
 * @param {function} props.onChange - Handler de changement
 * @param {Array} props.options - Liste des options
 * @param {boolean} props.disabled - Etat désactivé
 * @param {boolean} props.error - Etat d'erreur
 * @param {string} props.label - Libellé
 * @param {string} props.helperText - Texte d'aide
 * @param {string} props.errorMessage - Message d'erreur
 * @param {string} props.className - Classes CSS additionnelles
 * @param {boolean} props.required - Champ obligatoire
 * @param {string} props.placeholder - Placeholder
 */
const SelectInput = React.forwardRef(({
  value,
  onChange,
  options = [],
  disabled = false,
  error = false,
  label,
  helperText,
  errorMessage,
  className = '',
  required = false,
  placeholder,
  ...props
}, ref) => {
  const inputId = React.useId();
  
  return (
    <div
      className={`input-group ${className}`.trim()}
      style={{
        ...baseStyles,
        display: 'flex',
        flexDirection: 'column',
        gap: 'var(--space-xxs, 2px)',
      }}
      data-od-id="select-input-group"
    >
      {label && (
        <label
          htmlFor={inputId}
          style={{
            ...textStyles,
            fontSize: 'var(--text-body-sm, 14px)',
            fontWeight: 600,
            color: 'var(--fg, #262626)',
          }}
          data-od-id="select-label"
        >
          {label}
          {required && <span style={{ color: 'var(--error, #ff4d4f)' }}>*</span>}
        </label>
      )}
      
      <select
        ref={ref}
        id={inputId}
        value={value}
        onChange={onChange}
        disabled={disabled}
        className={`input select ${error ? 'input-error' : ''} ${disabled ? 'input-disabled' : ''}`}
        style={{
          ...baseStyles,
          ...textStyles,
          width: '100%',
          padding: 'var(--space-sm, 8px) var(--space-md, 12px)',
          backgroundColor: 'var(--surface, #f5faf7)',
          border: error 
            ? '1px solid var(--error, #ff4d4f)' 
            : '1px solid var(--border, #e5edf5)',
          borderRadius: 'var(--radius-sm, 4px)',
          color: 'var(--fg, #262626)',
          fontSize: 'var(--text-body, 16px)',
          lineHeight: 1.5,
          cursor: disabled ? 'not-allowed' : 'pointer',
          opacity: disabled ? 0.6 : 1,
          appearance: 'none',
          backgroundImage: 'url("data:image/svg+xml,%3Csvg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"%231c1e54\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"%3E%3Cpolyline points=\"6 9 12 15 18 9\"%3E%3C/polyline%3E%3C/svg%3E")',
          backgroundRepeat: 'no-repeat',
          backgroundPosition: 'right 12px center',
          backgroundSize: '16px',
          paddingRight: '40px',
        }}
        data-od-id="select-input"
        aria-invalid={error}
        aria-describedby={error ? `${inputId}-error` : helperText ? `${inputId}-helper` : undefined}
        {...props}
      >
        {placeholder && (
          <option value="" disabled hidden>
            {placeholder}
          </option>
        )}
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      
      {helperText && !error && (
        <span
          id={`${inputId}-helper`}
          style={{
            ...textStyles,
            fontSize: 'var(--text-caption, 13px)',
            color: 'var(--muted, #93cba8)',
          }}
          data-od-id="select-helper"
        >
          {helperText}
        </span>
      )}
      
      {error && errorMessage && (
        <span
          id={`${inputId}-error`}
          style={{
            ...textStyles,
            fontSize: 'var(--text-caption, 13px)',
            color: 'var(--error, #ff4d4f)',
          }}
          data-od-id="select-error"
          role="alert"
        >
          {errorMessage}
        </span>
      )}
    </div>
  );
});

SelectInput.displayName = 'SelectInput';

SelectInput.propTypes = {
  value: PropTypes.string,
  onChange: PropTypes.func,
  options: PropTypes.arrayOf(PropTypes.shape({
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
  })),
  disabled: PropTypes.bool,
  error: PropTypes.bool,
  label: PropTypes.string,
  helperText: PropTypes.string,
  errorMessage: PropTypes.string,
  className: PropTypes.string,
  required: PropTypes.bool,
  placeholder: PropTypes.string,
};

/**
 * Textarea
 * @param {Object} props - Props du composant
 * @param {string} props.value - Valeur
 * @param {function} props.onChange - Handler de changement
 * @param {string} props.placeholder - Placeholder
 * @param {boolean} props.disabled - Etat désactivé
 * @param {boolean} props.error - Etat d'erreur
 * @param {string} props.label - Libellé
 * @param {string} props.helperText - Texte d'aide
 * @param {string} props.errorMessage - Message d'erreur
 * @param {string} props.className - Classes CSS additionnelles
 * @param {number} props.rows - Nombre de lignes
 * @param {boolean} props.required - Champ obligatoire
 */
const Textarea = React.forwardRef({{
  value,
  onChange,
  placeholder,
  disabled = false,
  error = false,
  label,
  helperText,
  errorMessage,
  className = '',
  rows = 4,
  required = false,
  ...props
}, ref) => {
  const inputId = React.useId();
  
  return (
    <div
      className={`input-group ${className}`.trim()}
      style={{
        ...baseStyles,
        display: 'flex',
        flexDirection: 'column',
        gap: 'var(--space-xxs, 2px)',
      }}
      data-od-id="textarea-group"
    >
      {label && (
        <label
          htmlFor={inputId}
          style={{
            ...textStyles,
            fontSize: 'var(--text-body-sm, 14px)',
            fontWeight: 600,
            color: 'var(--fg, #262626)',
          }}
          data-od-id="textarea-label"
        >
          {label}
          {required && <span style={{ color: 'var(--error, #ff4d4f)' }}>*</span>}
        </label>
      )}
      
      <textarea
        ref={ref}
        id={inputId}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        disabled={disabled}
        rows={rows}
        className={`input textarea ${error ? 'input-error' : ''} ${disabled ? 'input-disabled' : ''}`}
        style={{
          ...baseStyles,
          ...textStyles,
          width: '100%',
          padding: 'var(--space-sm, 8px) var(--space-md, 12px)',
          backgroundColor: 'var(--surface, #f5faf7)',
          border: error 
            ? '1px solid var(--error, #ff4d4f)' 
            : '1px solid var(--border, #e5edf5)',
          borderRadius: 'var(--radius-sm, 4px)',
          color: 'var(--fg, #262626)',
          fontSize: 'var(--text-body, 16px)',
          lineHeight: 1.5,
          resize: 'vertical',
          minHeight: '100px',
          transition: 'border-color var(--duration-fast, 100ms) ease-out, box-shadow var(--duration-fast, 100ms) ease-out',
          opacity: disabled ? 0.6 : 1,
          cursor: disabled ? 'not-allowed' : 'text',
        }}
        data-od-id="textarea"
        aria-invalid={error}
        aria-describedby={error ? `${inputId}-error` : helperText ? `${inputId}-helper` : undefined}
        {...props}
      />
      
      {helperText && !error && (
        <span
          id={`${inputId}-helper`}
          style={{
            ...textStyles,
            fontSize: 'var(--text-caption, 13px)',
            color: 'var(--muted, #93cba8)',
          }}
          data-od-id="textarea-helper"
        >
          {helperText}
        </span>
      )}
      
      {error && errorMessage && (
        <span
          id={`${inputId}-error`}
          style={{
            ...textStyles,
            fontSize: 'var(--text-caption, 13px)',
            color: 'var(--error, #ff4d4f)',
          }}
          data-od-id="textarea-error"
          role="alert"
        >
          {errorMessage}
        </span>
      )}
    </div>
  );
});

Textarea.displayName = 'Textarea';

Textarea.propTypes = {
  value: PropTypes.string,
  onChange: PropTypes.func,
  placeholder: PropTypes.string,
  disabled: PropTypes.bool,
  error: PropTypes.bool,
  label: PropTypes.string,
  helperText: PropTypes.string,
  errorMessage: PropTypes.string,
  className: PropTypes.string,
  rows: PropTypes.number,
  required: PropTypes.bool,
};

// ============================================================================
// EXPORTS
// ============================================================================

// ============================================================================
// EXPORTS
// ============================================================================

// Exporter vers window pour usage avec Babel Standalone
Object.assign(window, {
  TextInput,
  AmountInput,
  SelectInput,
  Textarea,
});

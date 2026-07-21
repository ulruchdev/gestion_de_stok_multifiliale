/**
 * StockMaster CM - Components Index
 * Design System Inspired by Stripe
 * 
 * Export centralise de tous les composants du Design System
 * Compatible avec Babel Standalone pour utilisation autonome
 */

// ============================================================================
// LOAD ALL COMPONENTS
// Charge tous les fichiers JSX et exporte dans window pour usage avec Babel
// ============================================================================

// Charger les icônes
try {
  // Essayer de charger Icons.jsx
  const iconsScript = document.createElement('script');
  iconsScript.src = 'components/Icons.jsx';
  iconsScript.type = 'text/babel';
  document.head.appendChild(iconsScript);
} catch (e) {
  // Les icônes seront chargées manuellement dans les HTML
}

// Charger les badges
try {
  const badgesScript = document.createElement('script');
  badgesScript.src = 'components/Badges.jsx';
  badgesScript.type = 'text/babel';
  document.head.appendChild(badgesScript);
} catch (e) {
  // Les badges seront chargés manuellement
}

// Charger les boutons
try {
  const buttonsScript = document.createElement('script');
  buttonsScript.src = 'components/Buttons.jsx';
  buttonsScript.type = 'text/babel';
  document.head.appendChild(buttonsScript);
} catch (e) {
  // Les boutons seront chargés manuellement
}

// Charger les inputs
try {
  const inputsScript = document.createElement('script');
  inputsScript.src = 'components/Inputs.jsx';
  inputsScript.type = 'text/babel';
  document.head.appendChild(inputsScript);
} catch (e) {
  // Les inputs seront chargés manuellement
}

// Charger les cartes
try {
  const cardsScript = document.createElement('script');
  cardsScript.src = 'components/Cards.jsx';
  cardsScript.type = 'text/babel';
  document.head.appendChild(cardsScript);
} catch (e) {
  // Les cartes seront chargées manuellement
}

// Charger les alertes
try {
  const alertsScript = document.createElement('script');
  alertsScript.src = 'components/Alerts.jsx';
  alertsScript.type = 'text/babel';
  document.head.appendChild(alertsScript);
} catch (e) {
  // Les alertes seront chargées manuellement
}

// Charger les tables
try {
  const tablesScript = document.createElement('script');
  tablesScript.src = 'components/Tables.jsx';
  tablesScript.type = 'text/babel';
  document.head.appendChild(tablesScript);
} catch (e) {
  // Les tables seront chargées manuellement
}

// Charger Atomic.jsx
try {
  const atomicScript = document.createElement('script');
  atomicScript.src = 'components/Atomic.jsx';
  atomicScript.type = 'text/babel';
  document.head.appendChild(atomicScript);
} catch (e) {
  // Atomic sera chargé manuellement
}

// ============================================================================
// EXPORT DEFAULT
// ============================================================================

/**
 * Export par defaut de tous les composants
 * Usage avec Babel: 
 * <script type="text/babel" src="components/index.jsx"></script>
 * 
 * Usage avec modules:
 * import { Button, TextInput, Card, Alert, OrderStatusBadge, StockMasterIcons } from './components';
 */

// Exporter un objet vide - les composants sont disponibles dans window
// après chargement des scripts individuels
const StockMasterComponents = {};

// Si on est dans un contexte module, essayer d'importer
if (typeof import !== 'undefined') {
  // Ce code ne s'exécutera que dans un environnement qui supporte les modules
  // Pour Node.js ou bundlers
  try {
    Object.assign(StockMasterComponents, {
      // Badges
      get OrderStatusBadge() { return import('./Badges.jsx').then(m => m.OrderStatusBadge); },
      get StockMovementBadge() { return import('./Badges.jsx').then(m => m.StockMovementBadge); },
      get BadgeDemo() { return import('./Badges.jsx').then(m => m.BadgeDemo); },
      
      // Buttons
      get Button() { return import('./Buttons.jsx').then(m => m.Button); },
      get IconButton() { return import('./Buttons.jsx').then(m => m.IconButton); },
      get ButtonGroup() { return import('./Buttons.jsx').then(m => m.ButtonGroup); },
      
      // Inputs
      get TextInput() { return import('./Inputs.jsx').then(m => m.TextInput); },
      get AmountInput() { return import('./Inputs.jsx').then(m => m.AmountInput); },
      get SelectInput() { return import('./Inputs.jsx').then(m => m.SelectInput); },
      get Textarea() { return import('./Inputs.jsx').then(m => m.Textarea); },
      
      // Cards
      get Card() { return import('./Cards.jsx').then(m => m.Card); },
      get CardHeader() { return import('./Cards.jsx').then(m => m.CardHeader); },
      get CardBody() { return import('./Cards.jsx').then(m => m.CardBody); },
      get CardFooter() { return import('./Cards.jsx').then(m => m.CardFooter); },
      
      // Alerts
      get Alert() { return import('./Alerts.jsx').then(m => m.Alert); },
      get Toast() { return import('./Alerts.jsx').then(m => m.Toast); },
      get EmptyState() { return import('./Alerts.jsx').then(m => m.EmptyState); },
      get Skeleton() { return import('./Alerts.jsx').then(m => m.Skeleton); },
      get OfflineState() { return import('./Alerts.jsx').then(m => m.OfflineState); },
      get LoadingState() { return import('./Alerts.jsx').then(m => m.LoadingState); },
      
      // Tables
      get Table() { return import('./Tables.jsx').then(m => m.Table); },
      get MobileTable() { return import('./Tables.jsx').then(m => m.MobileTable); },
      get ArticlesTable() { return import('./Tables.jsx').then(m => m.ArticlesTable); },
      get CommandesTable() { return import('./Tables.jsx').then(m => m.CommandesTable); },
      get MouvementsStockTable() { return import('./Tables.jsx').then(m => m.MouvementsStockTable); },
      get formatXAF() { return import('./Tables.jsx').then(m => m.formatXAF); },
      get formatDate() { return import('./Tables.jsx').then(m => m.formatDate); },
      get formatDateTime() { return import('./Tables.jsx').then(m => m.formatDateTime); },
      
      // Atomic
      get H1() { return import('./Atomic.jsx').then(m => m.H1); },
      get H2() { return import('./Atomic.jsx').then(m => m.H2); },
      get H3() { return import('./Atomic.jsx').then(m => m.H3); },
      get Text() { return import('./Atomic.jsx').then(m => m.Text); },
      get Code() { return import('./Atomic.jsx').then(m => m.Code); },
      get Divider() { return import('./Atomic.jsx').then(m => m.Divider); },
      get Spacer() { return import('./Atomic.jsx').then(m => m.Spacer); },
      get Container() { return import('./Atomic.jsx').then(m => m.Container); },
      get useBreakpoint() { return import('./Atomic.jsx').then(m => m.useBreakpoint); },
      get useLoading() { return import('./Atomic.jsx').then(m => m.useLoading); },
      get injectKeyframes() { return import('./Atomic.jsx').then(m => m.injectKeyframes); },
      
      // Icons
      get StockMasterIcons() { return import('./Icons.jsx').then(m => m.StockMasterIcons); }
    });
  } catch (e) {
    // Ignorer les erreurs de chargement
  }
}

// Exporter dans window pour utilisation avec Babel
Object.assign(window, {
  StockMasterComponents,
  // Références directes qui seront remplies par les scripts individuels
  OrderStatusBadge: window.OrderStatusBadge || (() => null),
  StockMovementBadge: window.StockMovementBadge || (() => null),
  BadgeDemo: window.BadgeDemo || (() => null),
  Button: window.Button || (() => null),
  IconButton: window.IconButton || (() => null),
  ButtonGroup: window.ButtonGroup || (() => null),
  TextInput: window.TextInput || (() => null),
  AmountInput: window.AmountInput || (() => null),
  SelectInput: window.SelectInput || (() => null),
  Textarea: window.Textarea || (() => null),
  Card: window.Card || (() => null),
  CardHeader: window.CardHeader || (() => null),
  CardBody: window.CardBody || (() => null),
  CardFooter: window.CardFooter || (() => null),
  Alert: window.Alert || (() => null),
  Toast: window.Toast || (() => null),
  EmptyState: window.EmptyState || (() => null),
  Skeleton: window.Skeleton || (() => null),
  OfflineState: window.OfflineState || (() => null),
  LoadingState: window.LoadingState || (() => null),
  Table: window.Table || (() => null),
  MobileTable: window.MobileTable || (() => null),
  ArticlesTable: window.ArticlesTable || (() => null),
  CommandesTable: window.CommandesTable || (() => null),
  MouvementsStockTable: window.MouvementsStockTable || (() => null),
  formatXAF: window.formatXAF || ((amount) => `${amount || 0} XAF`),
  formatDate: window.formatDate || ((date) => date ? new Date(date).toLocaleDateString('fr-FR') : '-'),
  formatDateTime: window.formatDateTime || ((date) => date ? new Date(date).toLocaleString('fr-FR') : '-'),
  H1: window.H1 || (() => null),
  H2: window.H2 || (() => null),
  H3: window.H3 || (() => null),
  Text: window.Text || (() => null),
  Code: window.Code || (() => null),
  Divider: window.Divider || (() => null),
  Spacer: window.Spacer || (() => null),
  Container: window.Container || (() => null),
  useBreakpoint: window.useBreakpoint || (() => 'md'),
  useLoading: window.useLoading || (() => ({ isLoading: false, startLoading: () => {}, stopLoading: () => {} })),
  injectKeyframes: window.injectKeyframes || (() => {}),
  StockMasterIcons: window.StockMasterIcons || {}
});

// Note: Dans un contexte Babel Standalone, StockMasterComponents est disponible via window.StockMasterComponents
// Pour usage module, ce fichier peut être utilisé comme entry point mais nécessitera un bundler

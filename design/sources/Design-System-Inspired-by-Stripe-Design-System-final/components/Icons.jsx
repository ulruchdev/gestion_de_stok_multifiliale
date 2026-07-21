/**
 * StockMaster CM - Icon System
 * Systeme d'iconnes base sur Lucide React
 * 
 * US-D006: Set d'iconnes (lucide-react)
 * 
 * Documentation: https://lucide.dev/
 * 
 * Usage avec Babel Standalone:
 * 1. Charger Lucide React depuis CDN:
 *    <script src="https://unpkg.com/lucide@latest/dist/umd/lucide.js"></script>
 * 
 * 2. Charger ce fichier:
 *    <script type="text/babel" src="components/Icons.jsx"></script>
 * 
 * 3. Utiliser les iconnes:
 *    <StockMasterIcons.Package size={24} color="var(--accent)" />
 *    OU
 *    <PackageIcon size={24} />
 */

// ============================================================================
// STOCKMASTER CM ICON SYSTEM
// Intégration avec Lucide React via CDN
// ============================================================================

// Vérifier que Lucide est chargé
if (typeof window.Lucide === 'undefined') {
  console.warn('Lucide React non chargé. Chargez d\'abord: https://unpkg.com/lucide@latest/dist/umd/lucide.js');
  
  // Créer un wrapper vide pour éviter les erreurs
  window.Lucide = {};
}

// ============================================================================
// ICÔNES PRINCIPALES STOCKMASTER CM
// Organisées par domaine fonctionnel
// Chaque icône est un wrapper autour de l'icône Lucide correspondante
// ============================================================================

// --- ICÔNES DE STOCK ---
const StockIcon = (props) => {
  const LucideIcon = window.Lucide.Package || (() => null);
  return <LucideIcon {...props} />;
};

const PackageIcon = (props) => {
  const LucideIcon = window.Lucide.Package || (() => null);
  return <LucideIcon {...props} />;
};

const PackageOpenIcon = (props) => {
  const LucideIcon = window.Lucide.PackageOpen || (() => null);
  return <LucideIcon {...props} />;
};

const BoxesIcon = (props) => {
  const LucideIcon = window.Lucide.Boxes || (() => null);
  return <LucideIcon {...props} />;
};

const ArchiveIcon = (props) => {
  const LucideIcon = window.Lucide.Archive || (() => null);
  return <LucideIcon {...props} />;
};

// --- ICÔNES D'ALERTE ---
const AlertTriangleIcon = (props) => {
  const LucideIcon = window.Lucide.AlertTriangle || (() => null);
  return <LucideIcon {...props} />;
};

const AlertCircleIcon = (props) => {
  const LucideIcon = window.Lucide.AlertCircle || (() => null);
  return <LucideIcon {...props} />;
};

const BellIcon = (props) => {
  const LucideIcon = window.Lucide.Bell || (() => null);
  return <LucideIcon {...props} />;
};

const BellOffIcon = (props) => {
  const LucideIcon = window.Lucide.BellOff || (() => null);
  return <LucideIcon {...props} />;
};

// --- ICÔNES DE TRANSFERT ---
const TruckIcon = (props) => {
  const LucideIcon = window.Lucide.Truck || (() => null);
  return <LucideIcon {...props} />;
};

const ArrowLeftRightIcon = (props) => {
  const LucideIcon = window.Lucide.ArrowLeftRight || (() => null);
  return <LucideIcon {...props} />;
};

const MoveIcon = (props) => {
  const LucideIcon = window.Lucide.Move || (() => null);
  return <LucideIcon {...props} />;
};

const ExchangeIcon = (props) => {
  const LucideIcon = window.Lucide.Exchange || (() => null);
  return <LucideIcon {...props} />;
};

// --- ICÔNES DE FACTURE ---
const FileInvoiceIcon = (props) => {
  const LucideIcon = window.Lucide.FileInvoice || (() => null);
  return <LucideIcon {...props} />;
};

const FileTextIcon = (props) => {
  const LucideIcon = window.Lucide.FileText || (() => null);
  return <LucideIcon {...props} />;
};

const ReceiptIcon = (props) => {
  const LucideIcon = window.Lucide.Receipt || (() => null);
  return <LucideIcon {...props} />;
};

const PrinterIcon = (props) => {
  const LucideIcon = window.Lucide.Printer || (() => null);
  return <LucideIcon {...props} />;
};

// --- ICÔNES D'UTILISATEUR ---
const UserIcon = (props) => {
  const LucideIcon = window.Lucide.User || (() => null);
  return <LucideIcon {...props} />;
};

const UsersIcon = (props) => {
  const LucideIcon = window.Lucide.Users || (() => null);
  return <LucideIcon {...props} />;
};

const UserPlusIcon = (props) => {
  const LucideIcon = window.Lucide.UserPlus || (() => null);
  return <LucideIcon {...props} />;
};

const UserCogIcon = (props) => {
  const LucideIcon = window.Lucide.UserCog || (() => null);
  return <LucideIcon {...props} />;
};

const ShieldIcon = (props) => {
  const LucideIcon = window.Lucide.Shield || (() => null);
  return <LucideIcon {...props} />;
};

// --- ICÔNES DE COMMANDE ---
const ShoppingCartIcon = (props) => {
  const LucideIcon = window.Lucide.ShoppingCart || (() => null);
  return <LucideIcon {...props} />;
};

const ShoppingBasketIcon = (props) => {
  const LucideIcon = window.Lucide.ShoppingBasket || (() => null);
  return <LucideIcon {...props} />;
};

const ClipboardListIcon = (props) => {
  const LucideIcon = window.Lucide.ClipboardList || (() => null);
  return <LucideIcon {...props} />;
};

const ListIcon = (props) => {
  const LucideIcon = window.Lucide.List || (() => null);
  return <LucideIcon {...props} />;
};

// --- ICÔNES DE CAISSE ---
const CalculatorIcon = (props) => {
  const LucideIcon = window.Lucide.Calculator || (() => null);
  return <LucideIcon {...props} />;
};

const CreditCardIcon = (props) => {
  const LucideIcon = window.Lucide.CreditCard || (() => null);
  return <LucideIcon {...props} />;
};

const BanknoteIcon = (props) => {
  const LucideIcon = window.Lucide.Banknote || (() => null);
  return <LucideIcon {...props} />;
};

const CoinsIcon = (props) => {
  const LucideIcon = window.Lucide.Coins || (() => null);
  return <LucideIcon {...props} />;
};

// --- ICÔNES DE NAVIGATION ---
const HomeIcon = (props) => {
  const LucideIcon = window.Lucide.Home || (() => null);
  return <LucideIcon {...props} />;
};

const SettingsIcon = (props) => {
  const LucideIcon = window.Lucide.Settings || (() => null);
  return <LucideIcon {...props} />;
};

const MenuIcon = (props) => {
  const LucideIcon = window.Lucide.Menu || (() => null);
  return <LucideIcon {...props} />;
};

const XIcon = (props) => {
  const LucideIcon = window.Lucide.X || (() => null);
  return <LucideIcon {...props} />;
};

const ChevronRightIcon = (props) => {
  const LucideIcon = window.Lucide.ChevronRight || (() => null);
  return <LucideIcon {...props} />;
};

const ChevronLeftIcon = (props) => {
  const LucideIcon = window.Lucide.ChevronLeft || (() => null);
  return <LucideIcon {...props} />;
};

const ChevronUpIcon = (props) => {
  const LucideIcon = window.Lucide.ChevronUp || (() => null);
  return <LucideIcon {...props} />;
};

const ChevronDownIcon = (props) => {
  const LucideIcon = window.Lucide.ChevronDown || (() => null);
  return <LucideIcon {...props} />;
};

// --- ICÔNES D'ACTION ---
const SearchIcon = (props) => {
  const LucideIcon = window.Lucide.Search || (() => null);
  return <LucideIcon {...props} />;
};

const FilterIcon = (props) => {
  const LucideIcon = window.Lucide.Filter || (() => null);
  return <LucideIcon {...props} />;
};

const SortAscIcon = (props) => {
  const LucideIcon = window.Lucide.SortAsc || (() => null);
  return <LucideIcon {...props} />;
};

const SortDescIcon = (props) => {
  const LucideIcon = window.Lucide.SortDesc || (() => null);
  return <LucideIcon {...props} />;
};

const PlusIcon = (props) => {
  const LucideIcon = window.Lucide.Plus || (() => null);
  return <LucideIcon {...props} />;
};

const MinusIcon = (props) => {
  const LucideIcon = window.Lucide.Minus || (() => null);
  return <LucideIcon {...props} />;
};

const EditIcon = (props) => {
  const LucideIcon = window.Lucide.Edit || (() => null);
  return <LucideIcon {...props} />;
};

const TrashIcon = (props) => {
  const LucideIcon = window.Lucide.Trash2 || window.Lucide.Trash || (() => null);
  return <LucideIcon {...props} />;
};

const EyeIcon = (props) => {
  const LucideIcon = window.Lucide.Eye || (() => null);
  return <LucideIcon {...props} />;
};

const EyeOffIcon = (props) => {
  const LucideIcon = window.Lucide.EyeOff || (() => null);
  return <LucideIcon {...props} />;
};

const CheckIcon = (props) => {
  const LucideIcon = window.Lucide.Check || (() => null);
  return <LucideIcon {...props} />;
};

const XCircleIcon = (props) => {
  const LucideIcon = window.Lucide.XCircle || (() => null);
  return <LucideIcon {...props} />;
};

const LoaderIcon = (props) => {
  const LucideIcon = window.Lucide.Loader2 || window.Lucide.Loader || (() => null);
  return <LucideIcon {...props} />;
};

// --- ICÔNES D'ÉTAT ---
const CheckCircleIcon = (props) => {
  const LucideIcon = window.Lucide.CheckCircle || (() => null);
  return <LucideIcon {...props} />;
};

const AlertOctagonIcon = (props) => {
  const LucideIcon = window.Lucide.AlertOctagon || (() => null);
  return <LucideIcon {...props} />;
};

const InfoIcon = (props) => {
  const LucideIcon = window.Lucide.Info || (() => null);
  return <LucideIcon {...props} />;
};

// ============================================================================
// MAP DES ICÔNES PAR CATÉGORIE
// ============================================================================

const StockMasterIcons = {
  // Stock
  Stock: StockIcon,
  Package: PackageIcon,
  PackageOpen: PackageOpenIcon,
  Boxes: BoxesIcon,
  Archive: ArchiveIcon,
  
  // Alertes
  AlertTriangle: AlertTriangleIcon,
  AlertCircle: AlertCircleIcon,
  Bell: BellIcon,
  BellOff: BellOffIcon,
  
  // Transferts
  Truck: TruckIcon,
  ArrowLeftRight: ArrowLeftRightIcon,
  Move: MoveIcon,
  Exchange: ExchangeIcon,
  
  // Factures
  FileInvoice: FileInvoiceIcon,
  FileText: FileTextIcon,
  Receipt: ReceiptIcon,
  Printer: PrinterIcon,
  
  // Utilisateurs
  User: UserIcon,
  Users: UsersIcon,
  UserPlus: UserPlusIcon,
  UserCog: UserCogIcon,
  Shield: ShieldIcon,
  
  // Commandes
  ShoppingCart: ShoppingCartIcon,
  ShoppingBasket: ShoppingBasketIcon,
  ClipboardList: ClipboardListIcon,
  List: ListIcon,
  
  // Caisse
  Calculator: CalculatorIcon,
  CreditCard: CreditCardIcon,
  Banknote: BanknoteIcon,
  Coins: CoinsIcon,
  
  // Navigation
  Home: HomeIcon,
  Settings: SettingsIcon,
  Menu: MenuIcon,
  X: XIcon,
  ChevronRight: ChevronRightIcon,
  ChevronLeft: ChevronLeftIcon,
  ChevronUp: ChevronUpIcon,
  ChevronDown: ChevronDownIcon,
  
  // Actions
  Search: SearchIcon,
  Filter: FilterIcon,
  SortAsc: SortAscIcon,
  SortDesc: SortDescIcon,
  Plus: PlusIcon,
  Minus: MinusIcon,
  Edit: EditIcon,
  Trash: TrashIcon,
  Eye: EyeIcon,
  EyeOff: EyeOffIcon,
  Check: CheckIcon,
  XCircle: XCircleIcon,
  Loader: LoaderIcon,
  
  // États
  CheckCircle: CheckCircleIcon,
  AlertOctagon: AlertOctagonIcon,
  Info: InfoIcon
};

// ============================================================================
// EXPORT GLOBAL
// ============================================================================

// Assigner toutes les icônes à window pour utilisation globale
Object.assign(window, {
  StockMasterIcons,
  // Export individuel de chaque icône pour un accès direct
  StockIcon, PackageIcon, PackageOpenIcon, BoxesIcon, ArchiveIcon,
  AlertTriangleIcon, AlertCircleIcon, BellIcon, BellOffIcon,
  TruckIcon, ArrowLeftRightIcon, MoveIcon, ExchangeIcon,
  FileInvoiceIcon, FileTextIcon, ReceiptIcon, PrinterIcon,
  UserIcon, UsersIcon, UserPlusIcon, UserCogIcon, ShieldIcon,
  ShoppingCartIcon, ShoppingBasketIcon, ClipboardListIcon, ListIcon,
  CalculatorIcon, CreditCardIcon, BanknoteIcon, CoinsIcon,
  HomeIcon, SettingsIcon, MenuIcon, XIcon, ChevronRightIcon, ChevronLeftIcon, ChevronUpIcon, ChevronDownIcon,
  SearchIcon, FilterIcon, SortAscIcon, SortDescIcon,
  PlusIcon, MinusIcon, EditIcon, TrashIcon, EyeIcon, EyeOffIcon, CheckIcon, XCircleIcon, LoaderIcon,
  CheckCircleIcon, AlertOctagonIcon, InfoIcon
});

// ============================================================================
// UTILITAIRES
// ============================================================================

/**
 * Récupère une icône par son nom
 * @param {string} iconName - Nom de l'icône (ex: 'Package', 'User')
 * @param {Object} props - Props à passer à l'icône
 * @returns {JSX.Element} L'icône demandée
 */
const getIcon = (iconName, props = {}) => {
  const IconComponent = StockMasterIcons[iconName];
  return IconComponent ? <IconComponent {...props} /> : null;
};

// Ajouter getIcon à window
window.getIcon = getIcon;

/**
 * Crée une icône avec des props par défaut
 * @param {string} iconName - Nom de l'icône
 * @param {Object} defaultProps - Props par défaut
 * @returns {Function} Composant icône avec props par défaut
 */
const withDefaultProps = (iconName, defaultProps = {}) => {
  return (props = {}) => getIcon(iconName, { ...defaultProps, ...props });
};

// Ajouter withDefaultProps à window
window.withDefaultProps = withDefaultProps;

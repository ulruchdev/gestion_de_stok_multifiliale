import { Link, useNavigate } from 'react-router-dom';
import { Building2, Store, ArrowRight, Check, Star, Monitor, Shield, Package } from 'lucide-react';
import { cn } from '@/shared/lib/utils';
import { Logo } from '@/shared/ui/Logo';

interface InscriptionOption {
  id: string;
  title: string;
  description: string;
  path: string;
  icon: React.ComponentType<{ className?: string }>;
  color: string;
  bgLight: string;
  features: string[];
  badge?: string;
  popular?: boolean;
}

const options: InscriptionOption[] = [
  {
    id: 'unique',
    title: 'Entreprise unique',
    description:
      'Pour les commerces, pharmacies ou distributeurs avec un seul point de vente.',
    path: '/inscription/entreprise-unique',
    icon: Store,
    color: 'text-[var(--brand-primary)]',
    bgLight: 'bg-[var(--brand-primary-bg)]',
    badge: 'Idéal pour démarrer',
    features: [
      'Gestion de stock complète',
      'Ventes directes (caisse)',
      'Commandes fournisseurs',
      "Jusqu'à 3 utilisateurs",
      'Tableau de bord filiale',
    ],
  },
  {
    id: 'groupe',
    title: 'Groupe multi-sites',
    description:
      'Pour les groupes de distribution, chaînes de magasins ou réseaux de pharmacies.',
    path: '/inscription/groupe',
    icon: Building2,
    color: 'text-purple-600',
    bgLight: 'bg-purple-50',
    badge: 'Recommandé',
    popular: true,
    features: [
      'Tout ce que propose Entreprise',
      'Vue consolidée multi-filiales',
      'Transferts de stock inter-filiales',
      'Rapports consolidés Groupe',
      'Utilisateurs illimités',
    ],
  },
];

function ChoiceCard({ option, index }: { option: InscriptionOption; index: number }) {
  const navigate = useNavigate();
  const Icon = option.icon;

  return (
    <div
      className={cn(
        'group relative flex flex-col rounded-2xl border-2 transition-all duration-300 animate-fade-in-up hover:-translate-y-1',
        option.popular
          ? 'border-purple-300 bg-white shadow-lg hover:shadow-xl hover:border-purple-400'
          : 'border-[var(--brand-hairline)] bg-white shadow-sm hover:shadow-lg hover:border-[var(--brand-primary-border)]',
      )}
      style={{ animationDelay: `${index * 100}ms` }}
    >
      {/* Badge "Recommandé" */}
      {option.popular && (
        <div className="absolute -top-3 left-1/2 -translate-x-1/2 z-10">
          <span className="inline-flex items-center gap-1.5 px-4 py-1 rounded-full bg-gradient-to-r from-purple-600 to-indigo-600 text-white text-xs font-semibold shadow-lg">
            <Star className="h-3 w-3 fill-white" aria-hidden="true" />
            Recommandé
          </span>
        </div>
      )}

      <div className="p-6 sm:p-8 flex flex-col h-full">
        {/* Icon + Title row */}
        <div className="flex items-start gap-4 mb-4">
          <div
            className={cn(
              'shrink-0 w-12 h-12 rounded-xl flex items-center justify-center ring-1 ring-inset',
              option.popular
                ? 'bg-purple-50 ring-purple-200'
                : option.bgLight + ' ring-[var(--brand-primary-border)]',
            )}
          >
            <Icon className={cn('h-6 w-6', option.color)} aria-hidden="true" />
          </div>
          <div className="min-w-0 flex-1">
            <div className="flex items-center gap-2 mb-0.5">
              <h2 className="text-lg font-semibold font-display text-[var(--brand-ink)]">
                {option.title}
              </h2>
              {option.badge && !option.popular && (
                <span className="shrink-0 text-[10px] font-medium text-[var(--brand-primary)] bg-[var(--brand-primary-bg)] px-2 py-0.5 rounded-full border border-[var(--brand-primary-border)] ring-1 ring-inset ring-[var(--brand-primary-border)]">
                  {option.badge}
                </span>
              )}
            </div>
            <p className="text-sm text-[var(--brand-ink-muted)] leading-relaxed">
              {option.description}
            </p>
          </div>
        </div>

        {/* Features list */}
        <ul className="space-y-2.5 my-5">
          {option.features.map((feature) => (
            <li key={feature} className="flex items-start gap-3 text-sm">
              <span
                className={cn(
                  'shrink-0 mt-0.5 w-5 h-5 rounded-full flex items-center justify-center',
                  option.popular ? 'bg-purple-50' : option.bgLight,
                )}
              >
                <Check className={cn('h-3 w-3', option.color)} aria-hidden="true" />
              </span>
              <span className="text-[var(--brand-ink)]">{feature}</span>
            </li>
          ))}
        </ul>

        {/* Spacer */}
        <div className="flex-1" />

        {/* CTA */}
        <button
          onClick={() => navigate(option.path)}
          className={cn(
            'group/btn relative w-full inline-flex items-center justify-center gap-2 px-5 py-3 rounded-xl text-sm font-semibold transition-all duration-200',
            'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2',
            option.popular
              ? 'bg-gradient-to-r from-purple-600 to-indigo-600 text-white shadow-md hover:shadow-lg hover:from-purple-500 hover:to-indigo-500 focus-visible:ring-purple-500'
              : 'bg-[var(--brand-primary)] text-white shadow-sm hover:shadow-md hover:bg-[var(--brand-primary-hover)] focus-visible:ring-[var(--brand-primary)]',
          )}
        >
          {option.popular ? 'Créer mon groupe' : "Créer mon espace"}
          <ArrowRight className="h-4 w-4 transition-transform duration-200 group-hover/btn:translate-x-1" aria-hidden="true" />
        </button>
      </div>
    </div>
  );
}

export function InscriptionChoixPage() {
  return (
    <div className="min-h-[calc(100vh-10rem)] flex items-center justify-center py-8">
      <div className="w-full max-w-4xl mx-auto px-4">
        {/* Logo centré */}
        <div className="text-center mb-8">
          <Logo variant="auth" size="lg" />
        </div>

        {/* Hero section */}
        <div className="text-center mb-10 sm:mb-12 space-y-4">
          <h1 className="text-3xl sm:text-4xl font-bold font-display tracking-tight text-[var(--brand-ink)]">
            Créez votre espace
          </h1>
          <p className="text-base sm:text-lg text-[var(--brand-ink-muted)] max-w-lg mx-auto leading-relaxed">
            Choisissez la formule qui correspond à votre organisation.
            Pas de carte bancaire, pas d'engagement.
          </p>
        </div>

        {/* Grille de choix */}
        <div className="grid gap-6 sm:grid-cols-2">
          {options.map((option, i) => (
            <ChoiceCard key={option.id} option={option} index={i} />
          ))}
        </div>

        {/* Trust row */}
        <div className="mt-10 flex flex-wrap items-center justify-center gap-x-6 gap-y-2 text-sm text-[var(--brand-ink-muted)]">
          <span className="inline-flex items-center gap-1.5">
            <Shield className="h-3.5 w-3.5 text-[var(--stock-success)]" aria-hidden="true" />
            Données sécurisées
          </span>
          <span className="inline-flex items-center gap-1.5">
            <Monitor className="h-3.5 w-3.5 text-[var(--stock-success)]" aria-hidden="true" />
            Interface intuitive
          </span>
          <span className="inline-flex items-center gap-1.5">
            <Package className="h-3.5 w-3.5 text-[var(--stock-success)]" aria-hidden="true" />
            Gratuit pour démarrer
          </span>
        </div>

        {/* Lien connexion */}
        <div className="mt-8 text-center">
          <p className="text-sm text-[var(--brand-ink-muted)]">
            Déjà un compte ?{' '}
            <Link
              to="/login"
              className="font-semibold text-[var(--brand-primary)] hover:text-[var(--brand-primary-hover)] transition-colors"
            >
              Se connecter
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

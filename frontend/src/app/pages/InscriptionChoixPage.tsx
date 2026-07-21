import { Link, useNavigate } from 'react-router-dom';
import { Building2, Store, ArrowRight, ShieldCheck, Globe, Users } from 'lucide-react';
import { Button } from '@/shared/ui/Button';
import { Card, CardContent } from '@/shared/ui/Card';

interface InscriptionOption {
  title: string;
  description: string;
  path: string;
  icon: React.ComponentType<{ className?: string }>;
  features: string[];
  badge?: string;
}

const options: InscriptionOption[] = [
  {
    title: 'Entreprise unique',
    description:
      'Pour les commerces, pharmacies ou distributeurs avec un seul point de vente. Gérez votre stock, vos ventes et vos clients en toute simplicité.',
    path: '/inscription/entreprise-unique',
    icon: Store,
    features: [
      'Gestion de stock complète',
      'Ventes directes (caisse)',
      'Commandes clients et fournisseurs',
      'Tableau de bord filiale',
      "Jusqu'à 3 utilisateurs",
    ],
    badge: 'Idéal pour démarrer',
  },
  {
    title: 'Groupe multi-sites',
    description:
      "Pour les groupes de distribution, chaînes de magasins ou réseaux de pharmacies avec plusieurs filiales. Vue consolidée et transferts inter-filiales.",
    path: '/inscription/groupe',
    icon: Building2,
    features: [
      'Toutes les fonctionnalités Entreprise',
      'Vue consolidée multi-filiales',
      'Transferts de stock inter-filiales',
      'Rôles et permissions avancés',
      'Rapports consolidés Groupe',
      'Utilisateurs illimités',
    ],
    badge: 'Recommandé',
  },
];

export function InscriptionChoixPage() {
  const navigate = useNavigate();

  return (
    <div className="space-y-6 animate-fade-in-up">
      {/* En-tête */}
      <div className="text-center space-y-3">
        <div className="inline-flex items-center justify-center w-12 h-12 rounded-full bg-[var(--brand-primary-bg)] mb-1">
          <Users className="h-6 w-6 text-[var(--brand-primary)]" />
        </div>
        <h1 className="text-2xl font-semibold font-display tracking-tight text-[var(--brand-ink)]">
          Créer mon espace
        </h1>
        <p className="text-sm text-[var(--brand-ink-muted)] max-w-sm mx-auto">
          Choisissez le type de compte qui correspond à votre organisation
        </p>
      </div>

      {/* Cartes de choix */}
      <div className="grid gap-4 sm:grid-cols-2">
        {options.map((option) => {
          const Icon = option.icon;
          return (
            <Link
              key={option.path}
              to={option.path}
              className="group block"
            >
              <Card
                variant="interactive"
                className="h-full transition-all duration-[var(--brand-duration-mid)] group-hover:shadow-lg group-hover:-translate-y-0.5"
              >
                <CardContent padding="lg" className="h-full flex flex-col">
                  {/* Badge */}
                  {option.badge && (
                    <span className="inline-flex items-center gap-1 text-xs font-medium text-[var(--brand-primary)] bg-[var(--brand-primary-bg)] px-2.5 py-1 rounded-full w-fit mb-4">
                      <ShieldCheck className="h-3 w-3" />
                      {option.badge}
                    </span>
                  )}

                  {/* Icon + Title */}
                  <div className="flex items-start gap-3 mb-3">
                    <div className="shrink-0 w-10 h-10 rounded-lg bg-[var(--brand-primary-bg)] flex items-center justify-center">
                      <Icon className="h-5 w-5 text-[var(--brand-primary)]" />
                    </div>
                    <div>
                      <h2 className="text-lg font-semibold font-display text-[var(--brand-ink)]">
                        {option.title}
                      </h2>
                      <p className="text-sm text-[var(--brand-ink-muted)] mt-0.5">
                        {option.description}
                      </p>
                    </div>
                  </div>

                  {/* Features */}
                  <ul className="space-y-2 mt-auto pt-4 border-t border-[var(--brand-hairline)]">
                    {option.features.map((feature) => (
                      <li
                        key={feature}
                        className="flex items-center gap-2 text-sm text-[var(--brand-ink-muted)]"
                      >
                        <Globe className="h-3.5 w-3.5 text-[var(--stock-success)] shrink-0" />
                        {feature}
                      </li>
                    ))}
                  </ul>

                  {/* CTA */}
                  <div className="mt-4 pt-3">
                    <Button
                      variant="brand"
                      className="w-full group/btn"
                      onClick={(e) => {
                        e.preventDefault();
                        navigate(option.path);
                      }}
                    >
                      Choisir {option.title.toLowerCase()}
                      <ArrowRight className="h-4 w-4 transition-transform group-hover/btn:translate-x-0.5" />
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </Link>
          );
        })}
      </div>

      {/* Lien retour */}
      <p className="text-center text-sm text-[var(--brand-ink-muted)]">
        Déjà un compte ?{' '}
        <Link
          to="/login"
          className="font-semibold text-[var(--brand-primary)] hover:text-[var(--brand-primary-hover)] transition-colors"
        >
          Se connecter
        </Link>
      </p>
    </div>
  );
}

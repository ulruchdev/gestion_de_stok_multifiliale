import { useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, CheckCircle2, BarChart3, Globe, Shield, Building2, Store, Warehouse, TrendingUp, Users, Clock, Smartphone } from 'lucide-react';
import { Button } from '@/shared/ui/Button';
import { Logo } from '@/shared/ui/Logo';

const stats = [
  { value: '500+', label: 'Commerces équipés', icon: Store },
  { value: '15', label: 'Filiales max par groupe', icon: Building2 },
  { value: '99.9%', label: 'Disponibilité', icon: Shield },
  { value: 'XAF', label: 'Devise locale supportée', icon: TrendingUp },
];

const features = [
  {
    title: 'Gestion de stock en temps réel',
    description: 'Suivez vos entrées et sorties de stock automatiquement. Alertes de rupture imminente pour ne jamais manquer de produits.',
    icon: Warehouse,
    gradient: 'from-blue-500 to-indigo-500',
  },
  {
    title: 'Multi-filiales consolidé',
    description: 'Vue groupe consolidée de toutes vos filiales. Transferts de stock inter-filiales en un clic.',
    icon: Globe,
    gradient: 'from-indigo-500 to-purple-500',
  },
  {
    title: 'Caisse & Ventes directes',
    description: 'Interface caisse optimisée pour le point de vente. Recherche rapide d\'articles, calcul automatique du TTC.',
    icon: BarChart3,
    gradient: 'from-emerald-500 to-teal-500',
  },
  {
    title: 'Commandes fournisseurs',
    description: 'Gérez vos approvisionnements de A à Z. Création multi-lignes, validation, réception partielle.',
    icon: TrendingUp,
    gradient: 'from-orange-500 to-red-500',
  },
  {
    title: 'Mode hors-ligne',
    description: 'Conçu pour les réalités camerounaises. Continuez à travailler même sans connexion Internet.',
    icon: Smartphone,
    gradient: 'from-cyan-500 to-blue-500',
  },
  {
    title: 'Rapports & Statistiques',
    description: 'Tableaux de bord par filiale ou consolidés groupe. Top articles, clients fidèles, comparaison inter-filiales.',
    icon: Users,
    gradient: 'from-pink-500 to-rose-500',
  },
];

const testimonials = [
  {
    quote: "StockMaster a révolutionné la gestion de mes 3 magasins. Je vois tout depuis mon téléphone.",
    author: 'Jean Kamga',
    role: 'Gérant — Boutiques du Centre',
    avatar: 'JK',
  },
  {
    quote: "Les alertes de stock m'évitent les ruptures pendant la période des fêtes. Indispensable !",
    author: 'Paul Nkwi',
    role: 'Directeur — Pharma Express',
    avatar: 'PN',
  },
  {
    quote: "Le mode hors-ligne nous sauve pendant les coupures. On ne perd jamais une vente.",
    author: 'Marie Tchinda',
    role: 'Caissière — Supermarché T',
    avatar: 'MT',
  },
  {
    quote: "La consolidation groupe m'a permis de réduire mes stocks de 25% en 3 mois.",
    author: 'Samuel Fotso',
    role: 'CEO — DistriPlus Cameroun',
    avatar: 'SF',
  },
];

function AccueilHeader() {
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <header className="fixed top-0 left-0 right-0 z-50 bg-white/80 backdrop-blur-lg border-b border-[var(--brand-hairline)]">
      <div className="container mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <Logo variant="landing" size="md" />

          {/* Desktop Nav */}
          <nav className="hidden md:flex items-center gap-8">
            <a href="#features" className="text-sm text-[var(--brand-ink-muted)] hover:text-[var(--brand-ink)] transition-colors">
              Fonctionnalités
            </a>
            <a href="#stats" className="text-sm text-[var(--brand-ink-muted)] hover:text-[var(--brand-ink)] transition-colors">
              Chiffres
            </a>
            <a href="#testimonials" className="text-sm text-[var(--brand-ink-muted)] hover:text-[var(--brand-ink)] transition-colors">
              Témoignages
            </a>
          </nav>

          {/* Actions */}
          <div className="hidden md:flex items-center gap-3">
            <Link to="/login">
              <Button variant="ghost" size="sm">
                Connexion
              </Button>
            </Link>
            <Link to="/inscription">
              <Button variant="brand" size="sm">
                Créer mon espace
              </Button>
            </Link>
          </div>

          {/* Mobile menu button */}
          <button
            onClick={() => setMenuOpen(!menuOpen)}
            className="md:hidden rounded-lg p-2 hover:bg-accent transition-colors"
            aria-label="Menu"
            aria-expanded={menuOpen}
          >
            <div className="space-y-1.5">
              <span className={`block w-5 h-0.5 bg-[var(--brand-ink)] transition-transform ${menuOpen ? 'rotate-45 translate-y-1' : ''}`} />
              <span className={`block w-5 h-0.5 bg-[var(--brand-ink)] transition-opacity ${menuOpen ? 'opacity-0' : ''}`} />
              <span className={`block w-5 h-0.5 bg-[var(--brand-ink)] transition-transform ${menuOpen ? '-rotate-45 -translate-y-1' : ''}`} />
            </div>
          </button>
        </div>

        {/* Mobile menu */}
        {menuOpen && (
          <nav className="md:hidden border-t border-[var(--brand-hairline)] py-4 space-y-3 animate-fade-in" role="navigation">
            <a href="#features" onClick={() => setMenuOpen(false)} className="block text-sm text-[var(--brand-ink-muted)] hover:text-[var(--brand-ink)] py-2">
              Fonctionnalités
            </a>
            <a href="#stats" onClick={() => setMenuOpen(false)} className="block text-sm text-[var(--brand-ink-muted)] hover:text-[var(--brand-ink)] py-2">
              Chiffres
            </a>
            <a href="#testimonials" onClick={() => setMenuOpen(false)} className="block text-sm text-[var(--brand-ink-muted)] hover:text-[var(--brand-ink)] py-2">
              Témoignages
            </a>
            <div className="flex gap-3 pt-2">
              <Link to="/login" className="flex-1" onClick={() => setMenuOpen(false)}>
                <Button variant="outline" className="w-full">
                  Connexion
                </Button>
              </Link>
              <Link to="/inscription" className="flex-1" onClick={() => setMenuOpen(false)}>
                <Button variant="brand" className="w-full">
                  S'inscrire
                </Button>
              </Link>
            </div>
          </nav>
        )}
      </div>
    </header>
  );
}

function HeroSection() {
  return (
    <section className="relative pt-32 pb-20 md:pt-40 md:pb-28 overflow-hidden">
      {/* Background gradient */}
      <div className="absolute inset-0 bg-gradient-to-b from-[var(--brand-primary-bg)] via-transparent to-transparent opacity-50" />
      <div className="absolute top-1/4 -left-32 w-96 h-96 bg-[var(--brand-primary)]/5 rounded-full blur-3xl" />
      <div className="absolute top-1/3 -right-32 w-96 h-96 bg-purple-500/5 rounded-full blur-3xl" />

      <div className="container mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 relative">
        <div className="max-w-3xl mx-auto text-center space-y-8">
          {/* Badge */}
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[var(--brand-primary-bg)] border border-[var(--brand-primary-border)]">
            <span className="text-xs font-medium text-[var(--brand-primary)]">
              🇨🇲 Conçu pour les PME camerounaises
            </span>
          </div>

          {/* Headline */}
          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold font-display tracking-tight text-[var(--brand-ink)] leading-[1.1]">
            Gérez votre stock
            <br />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-[var(--brand-primary)] to-purple-600">
              sans prise de tête
            </span>
          </h1>

          {/* Subtitle */}
          <p className="text-lg sm:text-xl text-[var(--brand-ink-muted)] max-w-2xl mx-auto leading-relaxed">
            La solution de gestion de stock multi-filiales pensée pour les commerces, 
            pharmacies et distributeurs camerounais. <strong>Simple, rapide, hors-ligne.</strong>
          </p>

          {/* CTA */}
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link to="/inscription" className="w-full sm:w-auto">
              <Button variant="brand" size="xl" className="w-full sm:w-auto text-base gap-2 shadow-lg hover:shadow-xl">
                Créer mon espace gratuit
                <ArrowRight className="h-5 w-5" />
              </Button>
            </Link>
            <Link to="/login" className="w-full sm:w-auto">
              <Button variant="outline" size="xl" className="w-full sm:w-auto text-base">
                J'ai déjà un compte
              </Button>
            </Link>
          </div>

          {/* Trust badges */}
          <div className="flex flex-wrap items-center justify-center gap-x-8 gap-y-2 pt-4">
            {[
              { icon: CheckCircle2, text: 'Sans engagement' },
              { icon: Clock, text: 'Activation en 2 minutes' },
              { icon: Shield, text: 'Données sécurisées' },
            ].map((item) => {
              const Icon = item.icon;
              return (
                <div key={item.text} className="flex items-center gap-1.5 text-sm text-[var(--brand-ink-muted)]">
                  <Icon className="h-4 w-4 text-[var(--stock-success)]" />
                  {item.text}
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </section>
  );
}

function FeaturesSection() {
  return (
    <section id="features" className="py-20 md:py-28 bg-[var(--brand-canvas-soft)]">
      <div className="container mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="max-w-2xl mx-auto text-center mb-16 space-y-4">
          <h2 className="text-3xl sm:text-4xl font-bold font-display tracking-tight text-[var(--brand-ink)]">
            Tout ce qu'il vous faut pour gérer votre stock
          </h2>
          <p className="text-lg text-[var(--brand-ink-muted)]">
            Des fonctionnalités complètes, conçues pour les besoins réels des commerces camerounais.
          </p>
        </div>

        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {features.map((feature) => {
            const Icon = feature.icon;
            return (
              <div
                key={feature.title}
                className="group relative bg-white rounded-xl p-6 border border-[var(--brand-hairline)] shadow-sm hover:shadow-md transition-all duration-[var(--brand-duration-mid)] hover:-translate-y-0.5"
              >
                <div className={`w-10 h-10 rounded-lg bg-gradient-to-br ${feature.gradient} flex items-center justify-center mb-4 shadow-sm`}>
                  <Icon className="h-5 w-5 text-white" />
                </div>
                <h3 className="text-base font-semibold font-display text-[var(--brand-ink)] mb-2">
                  {feature.title}
                </h3>
                <p className="text-sm text-[var(--brand-ink-muted)] leading-relaxed">
                  {feature.description}
                </p>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}

function StatsSection() {
  return (
    <section id="stats" className="py-20 md:py-28 relative overflow-hidden">
      <div className="absolute inset-0 bg-gradient-to-br from-[var(--brand-primary)] to-indigo-700" />
      <div className="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIHZpZXdCb3g9IjAgMCA2MCA2MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48ZyBmaWxsPSJub25lIiBmaWxsLXJ1bGU9ImV2ZW5vZGQiPjxnIGZpbGw9IiNmZmYiIGZpbGwtb3BhY2l0eT0iMC4wNSI+PGNpcmNsZSBjeD0iMzAiIGN5PSIzMCIgcj0iMSIvPjwvZz48L2c+PC9zdmc+')] opacity-30" />

      <div className="container mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 relative">
        <div className="max-w-2xl mx-auto text-center mb-16 space-y-4">
          <h2 className="text-3xl sm:text-4xl font-bold font-display tracking-tight text-white">
            StockMaster en chiffres
          </h2>
          <p className="text-lg text-white/70">
            La confiance de nos utilisateurs est notre plus belle récompense.
          </p>
        </div>

        <div className="grid grid-cols-2 lg:grid-cols-4 gap-8">
          {stats.map((stat) => {
            const Icon = stat.icon;
            return (
              <div key={stat.label} className="text-center space-y-3">
                <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-white/10 backdrop-blur-sm">
                  <Icon className="h-6 w-6 text-white" />
                </div>
                <div className="space-y-1">
                  <p className="text-3xl sm:text-4xl font-bold font-display text-white">{stat.value}</p>
                  <p className="text-sm text-white/70">{stat.label}</p>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}

function TestimonialsSection() {
  return (
    <section id="testimonials" className="py-20 md:py-28">
      <div className="container mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="max-w-2xl mx-auto text-center mb-16 space-y-4">
          <h2 className="text-3xl sm:text-4xl font-bold font-display tracking-tight text-[var(--brand-ink)]">
            Ils nous font confiance
          </h2>
          <p className="text-lg text-[var(--brand-ink-muted)]">
            Découvrez ce que nos utilisateurs disent de StockMaster.
          </p>
        </div>

        <div className="grid sm:grid-cols-2 gap-6">
          {testimonials.map((t) => (
            <div
              key={t.author}
              className="bg-white rounded-xl p-6 border border-[var(--brand-hairline)] shadow-sm"
            >
              <div className="flex items-start gap-1 mb-4">
                {[...Array(5)].map((_, i) => (
                  <svg key={i} className="w-4 h-4 text-yellow-400 fill-current" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                  </svg>
                ))}
              </div>
              <p className="text-sm text-[var(--brand-ink-muted)] leading-relaxed mb-4 italic">
                &ldquo;{t.quote}&rdquo;
              </p>
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-full bg-[var(--brand-primary-bg)] flex items-center justify-center text-xs font-semibold text-[var(--brand-primary)]">
                  {t.avatar}
                </div>
                <div>
                  <p className="text-sm font-medium text-[var(--brand-ink)]">{t.author}</p>
                  <p className="text-xs text-[var(--brand-ink-muted)]">{t.role}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function CTASection() {
  return (
    <section className="py-20 md:py-28 bg-[var(--brand-canvas-soft)]">
      <div className="container mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="max-w-3xl mx-auto text-center space-y-8">
          <h2 className="text-3xl sm:text-4xl font-bold font-display tracking-tight text-[var(--brand-ink)]">
            Prêt à moderniser votre gestion de stock ?
          </h2>
          <p className="text-lg text-[var(--brand-ink-muted)] max-w-xl mx-auto">
            Rejoignez les centaines de commerces camerounais qui font confiance à StockMaster. 
            <strong> Gratuit pour démarrer, sans carte bancaire.</strong>
          </p>
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link to="/inscription">
              <Button variant="brand" size="xl" className="text-base gap-2 shadow-lg hover:shadow-xl">
                Créer mon espace gratuit
                <ArrowRight className="h-5 w-5" />
              </Button>
            </Link>
            <Link to="/login">
              <Button variant="outline" size="xl" className="text-base">
                Se connecter
              </Button>
            </Link>
          </div>
        </div>
      </div>
    </section>
  );
}

function AccueilFooter() {
  return (
    <footer className="bg-[var(--brand-dark)] text-white/60">
      <div className="container mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-8">
          {/* Brand */}
          <div className="space-y-3">
            <div className="flex items-center gap-2">
              <div className="w-7 h-7 rounded-lg bg-white/10 flex items-center justify-center">
                <Warehouse className="h-3.5 w-3.5 text-white" />
              </div>
              <span className="text-sm font-semibold text-white">StockMaster</span>
            </div>
            <p className="text-xs leading-relaxed">
              Solution de gestion de stock multi-filiales pour les PME camerounaises.
            </p>
          </div>

          {/* Produit */}
          <div className="space-y-3">
            <p className="text-xs font-semibold text-white uppercase tracking-wider">Produit</p>
            <ul className="space-y-2">
              {['Fonctionnalités', 'Tarifs', 'API', 'Documentation'].map((item) => (
                <li key={item}>
                  <a href="#" className="text-xs hover:text-white transition-colors">{item}</a>
                </li>
              ))}
            </ul>
          </div>

          {/* Entreprise */}
          <div className="space-y-3">
            <p className="text-xs font-semibold text-white uppercase tracking-wider">Entreprise</p>
            <ul className="space-y-2">
              {['À propos', 'Blog', 'Contact', 'Presse'].map((item) => (
                <li key={item}>
                  <a href="#" className="text-xs hover:text-white transition-colors">{item}</a>
                </li>
              ))}
            </ul>
          </div>

          {/* Légal */}
          <div className="space-y-3">
            <p className="text-xs font-semibold text-white uppercase tracking-wider">Légal</p>
            <ul className="space-y-2">
              {['Confidentialité', 'CGU', 'CGV', 'Mentions légales'].map((item) => (
                <li key={item}>
                  <a href="#" className="text-xs hover:text-white transition-colors">{item}</a>
                </li>
              ))}
            </ul>
          </div>
        </div>

        <div className="mt-10 pt-8 border-t border-white/10 text-center">
          <p className="text-xs">
            &copy; {new Date().getFullYear()} StockMaster CM. Tous droits réservés.
          </p>
        </div>
      </div>
    </footer>
  );
}

export function AccueilPage() {
  return (
    <div className="min-h-screen bg-white">
      <AccueilHeader />
      <HeroSection />
      <FeaturesSection />
      <StatsSection />
      <TestimonialsSection />
      <CTASection />
      <AccueilFooter />
    </div>
  );
}

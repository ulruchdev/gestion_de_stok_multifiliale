import type { ReactNode } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { Badge, Card, EmptyState, SkeletonText } from '@/shared/ui';
import { useArticle, useCategoriesLookup } from '../hooks';
import {
  APPARENCE_STATUT_ALERTE,
  formaterMontantXaf,
  formaterPourcentage,
  formaterStock,
} from '../lib/format';
import { estIntrouvable, messageDErreurCatalogue } from '../lib/erreurs';

function Ligne({
  libelle,
  children,
}: {
  libelle: string;
  children: ReactNode;
}) {
  return (
    <div className="space-y-1">
      <dt className="text-xs uppercase tracking-wide text-[var(--brand-ink-muted)]">
        {libelle}
      </dt>
      <dd className="text-sm text-[var(--brand-ink)]">{children}</dd>
    </div>
  );
}

export function ArticleDetailPage() {
  const { id } = useParams<{ id: string }>();
  const identifiant = Number.parseInt(id ?? '', 10);
  const idValide = Number.isFinite(identifiant) && identifiant > 0;

  const { data: article, isLoading, isError, error, refetch } = useArticle(
    idValide ? identifiant : null,
  );
  const categories = useCategoriesLookup();

  const retour = (
    <Link
      to="/catalogue/articles"
      className="inline-flex items-center gap-2 text-sm font-medium text-[var(--brand-ink-muted)] transition-colors hover:text-[var(--brand-ink)]"
    >
      <ArrowLeft aria-hidden="true" className="h-4 w-4" />
      Retour aux articles
    </Link>
  );

  if (!idValide) {
    return (
      <div className="space-y-4">
        {retour}
        <EmptyState
          variant="error"
          title="Article introuvable"
          description="L’identifiant de l’article est invalide."
        />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {retour}

      {isLoading && (
        <Card className="space-y-4 p-6" aria-busy="true">
          <SkeletonText lines={1} />
          <SkeletonText lines={4} />
        </Card>
      )}

      {isError && (
        <Card className="p-6">
          <EmptyState
            variant={estIntrouvable(error) ? 'no-results' : 'error'}
            title={
              estIntrouvable(error)
                ? 'Article introuvable'
                : 'Impossible de charger l’article'
            }
            description={
              estIntrouvable(error)
                ? 'Cet article n’existe pas ou n’appartient pas à votre groupe.'
                : messageDErreurCatalogue(error)
            }
            action={
              estIntrouvable(error)
                ? undefined
                : { label: 'Réessayer', onClick: () => void refetch(), variant: 'outline' }
            }
          />
        </Card>
      )}

      {article && (
        <>
          <header className="flex flex-wrap items-start justify-between gap-4">
            <div className="space-y-1">
              <p className="font-mono text-xs text-[var(--brand-ink-muted)]">
                {article.codeArticle}
              </p>
              <h1 className="text-2xl font-semibold text-[var(--brand-ink)]">
                {article.designation}
              </h1>
            </div>
            <div className="flex items-center gap-2">
              {article.statutAlerte ? (
                <Badge variant={APPARENCE_STATUT_ALERTE[article.statutAlerte].variant}>
                  Alerte : {APPARENCE_STATUT_ALERTE[article.statutAlerte].libelle}
                </Badge>
              ) : (
                <Badge variant="outline">Alerte indisponible</Badge>
              )}
              <Badge variant={article.actif ? 'success' : 'secondary'}>
                {article.actif ? 'Actif' : 'Inactif'}
              </Badge>
            </div>
          </header>

          <div className="grid gap-6 lg:grid-cols-3">
            <Card className="p-6 lg:col-span-2">
              <h2 className="mb-4 text-base font-semibold text-[var(--brand-ink)]">
                Prix
              </h2>
              <dl className="grid gap-4 sm:grid-cols-2">
                <Ligne libelle="Prix d’achat HT">
                  {formaterMontantXaf(article.prixAchatHt)}
                </Ligne>
                <Ligne libelle="Prix de vente HT">
                  {formaterMontantXaf(article.prixVenteHt)}
                </Ligne>
                <Ligne libelle="TVA">{formaterPourcentage(article.tauxTva)}</Ligne>
                <Ligne libelle="Prix de vente TTC">
                  <span className="font-semibold">
                    {formaterMontantXaf(article.prixVenteTtc)}
                  </span>
                </Ligne>
                <Ligne libelle="Marge brute">
                  {formaterPourcentage(article.margeBrutePct)}
                </Ligne>
                <Ligne libelle="Seuil d’alerte">
                  {formaterStock(article.seuilAlerte)}
                </Ligne>
              </dl>
            </Card>

            <Card className="space-y-4 p-6">
              <h2 className="text-base font-semibold text-[var(--brand-ink)]">
                Stock
              </h2>
              <p className="text-3xl font-semibold tabular-nums text-[var(--brand-ink)]">
                {formaterStock(article.stockActuel)}
              </p>
              <dl className="space-y-3">
                <Ligne libelle="Catégorie">
                  {categories.get(article.categorieId) ??
                    `Catégorie #${article.categorieId}`}
                </Ligne>
              </dl>
              {article.stockActuel === null && (
                <p className="text-xs text-[var(--brand-ink-muted)]">
                  Stock non disponible pour votre filiale.
                </p>
              )}
            </Card>
          </div>
        </>
      )}
    </div>
  );
}

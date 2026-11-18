import { useMemo } from 'react';
import { Link } from 'react-router-dom';
import { Badge, Card, DataTable, type Column } from '@/shared/ui';
import { useArticles, useCategoriesLookup } from '../hooks';
import { TAILLE_PAGE, usePaginationUrl } from '../usePaginationUrl';
import type { Article } from '../types';
import {
  APPARENCE_STATUT_ALERTE,
  formaterMontantXaf,
  formaterPourcentage,
  formaterStock,
} from '../lib/format';
import { messageDErreurCatalogue } from '../lib/erreurs';

export function ArticlesPage() {
  const { query, page, changerRecherche, changerPage } = usePaginationUrl();
  const { data, isLoading, isError, error, refetch } = useArticles({
    page: page - 1,
    size: TAILLE_PAGE,
    search: query,
  });
  const categories = useCategoriesLookup();

  const colonnes = useMemo<Column<Article>[]>(
    () => [
      {
        key: 'codeArticle',
        label: 'Code',
        render: (article) => (
          <Link
            to={`/catalogue/articles/${article.id}`}
            className="font-medium text-[var(--brand-primary)] hover:underline"
          >
            {article.codeArticle}
          </Link>
        ),
      },
      {
        key: 'designation',
        label: 'Désignation',
        render: (article) => article.designation,
      },
      {
        key: 'categorieId',
        label: 'Catégorie',
        render: (article) =>
          categories.get(article.categorieId) ?? `Catégorie #${article.categorieId}`,
      },
      {
        key: 'prixVenteTtc',
        label: 'Prix TTC',
        className: 'text-right tabular-nums',
        render: (article) => formaterMontantXaf(article.prixVenteTtc),
      },
      {
        key: 'tauxTva',
        label: 'TVA',
        className: 'text-right tabular-nums',
        render: (article) => formaterPourcentage(article.tauxTva),
      },
      {
        key: 'stockActuel',
        label: 'Stock',
        className: 'text-right tabular-nums',
        render: (article) => formaterStock(article.stockActuel),
      },
      {
        key: 'statutAlerte',
        label: 'Alerte',
        render: (article) => {
          if (!article.statutAlerte) return <span className="text-muted-foreground">—</span>;
          const apparence = APPARENCE_STATUT_ALERTE[article.statutAlerte];
          return <Badge variant={apparence.variant}>{apparence.libelle}</Badge>;
        },
      },
    ],
    [categories],
  );

  return (
    <div className="space-y-6">
      <header className="space-y-1">
        <h1 className="text-2xl font-semibold text-[var(--brand-ink)]">Articles</h1>
        <p className="text-sm text-[var(--brand-ink-muted)]">
          Catalogue du groupe — recherche par code article ou désignation.
        </p>
      </header>

      <Card className="p-4">
        <DataTable
          columns={colonnes}
          data={data?.content ?? []}
          keyExtractor={(article) => article.id}
          isLoading={isLoading}
          isError={isError}
          errorTitle="Impossible de charger les articles"
          errorMessage={messageDErreurCatalogue(error)}
          onRetry={() => void refetch()}
          searchable
          searchPlaceholder="Rechercher un article (code ou désignation)"
          searchValue={query}
          onSearch={changerRecherche}
          emptyTitle="Aucun article"
          emptyDescription="Le catalogue de votre groupe ne contient encore aucun article."
          pagination={
            data && data.totalPages > 0
              ? {
                  page,
                  totalPages: data.totalPages,
                  onPageChange: changerPage,
                }
              : undefined
          }
        />
      </Card>
    </div>
  );
}

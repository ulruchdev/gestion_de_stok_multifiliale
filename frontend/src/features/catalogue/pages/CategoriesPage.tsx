import { useMemo } from 'react';
import { Card, DataTable, type Column } from '@/shared/ui';
import { useCategories } from '../hooks';
import { TAILLE_PAGE, usePaginationUrl } from '../usePaginationUrl';
import type { Categorie } from '../types';
import { formaterPourcentage } from '../lib/format';
import { messageDErreurCatalogue } from '../lib/erreurs';

export function CategoriesPage() {
  const { query, page, changerRecherche, changerPage } = usePaginationUrl();
  const { data, isLoading, isError, error, refetch } = useCategories({
    page: page - 1,
    size: TAILLE_PAGE,
    designation: query,
  });

  const colonnes = useMemo<Column<Categorie>[]>(
    () => [
      { key: 'code', label: 'Code', render: (c) => c.code },
      { key: 'designation', label: 'Désignation', render: (c) => c.designation },
      {
        key: 'tauxTva',
        label: 'Taux de TVA',
        className: 'text-right tabular-nums',
        render: (c) => formaterPourcentage(c.tauxTva),
      },
    ],
    [],
  );

  return (
    <div className="space-y-6">
      <header className="space-y-1">
        <h1 className="text-2xl font-semibold text-[var(--brand-ink)]">Catégories</h1>
        <p className="text-sm text-[var(--brand-ink-muted)]">
          Référentiel des catégories du groupe et de leur taux de TVA.
        </p>
      </header>

      <Card className="p-4">
        <DataTable
          columns={colonnes}
          data={data?.content ?? []}
          keyExtractor={(categorie) => categorie.id}
          isLoading={isLoading}
          isError={isError}
          errorTitle="Impossible de charger les catégories"
          errorMessage={messageDErreurCatalogue(error)}
          onRetry={() => void refetch()}
          searchable
          searchPlaceholder="Rechercher une catégorie"
          searchValue={query}
          onSearch={changerRecherche}
          emptyTitle="Aucune catégorie"
          emptyDescription="Le catalogue de votre groupe ne contient encore aucune catégorie."
          pagination={
            data && data.totalPages > 0
              ? { page, totalPages: data.totalPages, onPageChange: changerPage }
              : undefined
          }
        />
      </Card>
    </div>
  );
}

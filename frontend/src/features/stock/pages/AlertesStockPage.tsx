import { useMemo } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Badge, Button, Card, DataTable, type Column } from '@/shared/ui';
import {
  APPARENCE_STATUT_ALERTE,
  formaterStock,
  type StatutAlerte,
} from '@/features/catalogue';
import { useStockAlertes } from '../hooks';
import {
  STATUTS_ALERTE,
  TAILLE_PAGE_ALERTES,
  estStatutAlerteFiltre,
  type StatutAlerteFiltre,
} from '../api';
import type { StockArticle } from '../types';

/** Statut ouvert par défaut : la rupture imminente est l'alerte la plus utile. */
const STATUT_PAR_DEFAUT: StatutAlerteFiltre = 'BAS';

const LIBELLES_FILTRE: Record<StatutAlerteFiltre, string> = {
  BAS: 'Sous le seuil',
  RUPTURE: 'Rupture',
  ANOMALIE: 'Anomalie',
};

const DESCRIPTIONS_VIDE: Record<StatutAlerteFiltre, string> = {
  BAS: 'Aucun article n’est passé sous son seuil d’alerte.',
  RUPTURE: 'Aucun article n’est en rupture de stock.',
  ANOMALIE: 'Aucune anomalie de stock détectée.',
};

/**
 * US-F091 — liste dédiée des alertes de stock.
 *
 * Distincte du badge ponctuel de la fiche article : ici on parcourt toutes les
 * alertes de la filiale, filtrées en SQL par statut. Le statut et la page sont
 * portés par l'URL (partageable, bouton retour), sans paramètre de tenant —
 * le périmètre vient du JWT.
 */
export function AlertesStockPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  const statutParam = searchParams.get('statut') ?? STATUT_PAR_DEFAUT;
  const statut: StatutAlerteFiltre = estStatutAlerteFiltre(statutParam)
    ? statutParam
    : STATUT_PAR_DEFAUT;

  const pageLue = Number.parseInt(searchParams.get('page') ?? '', 10);
  const page = Number.isFinite(pageLue) && pageLue > 0 ? pageLue : 1;

  const { data, isLoading, isError, error, refetch } = useStockAlertes({
    statutAlerte: statut,
    page: page - 1,
    size: TAILLE_PAGE_ALERTES,
  });

  const changerFiltre = (valeur: StatutAlerteFiltre) => {
    const params = new URLSearchParams(searchParams);
    if (valeur === STATUT_PAR_DEFAUT) params.delete('statut');
    else params.set('statut', valeur);
    // Un changement de filtre repart toujours de la première page.
    params.delete('page');
    setSearchParams(params, { replace: true });
  };

  const changerPage = (pageCible: number) => {
    const params = new URLSearchParams(searchParams);
    if (pageCible > 1) params.set('page', String(pageCible));
    else params.delete('page');
    setSearchParams(params, { replace: true });
  };

  const colonnes = useMemo<Column<StockArticle>[]>(
    () => [
      {
        key: 'codeArticle',
        label: 'Code',
        render: (ligne) => (
          <Link
            to={`/catalogue/articles/${ligne.articleId}`}
            className="font-medium text-[var(--brand-primary)] hover:underline"
          >
            {ligne.codeArticle}
          </Link>
        ),
      },
      {
        key: 'designation',
        label: 'Article',
        render: (ligne) => ligne.designation,
      },
      {
        key: 'stockActuel',
        label: 'Stock',
        className: 'text-right tabular-nums',
        render: (ligne) => formaterStock(ligne.stockActuel),
      },
      {
        key: 'seuilAlerte',
        label: 'Seuil',
        className: 'text-right tabular-nums',
        render: (ligne) => formaterStock(ligne.seuilAlerte),
      },
      {
        key: 'statutAlerte',
        label: 'Statut',
        render: (ligne) => {
          if (!ligne.statutAlerte) return <span className="text-muted-foreground">—</span>;
          const apparence = APPARENCE_STATUT_ALERTE[ligne.statutAlerte as StatutAlerte];
          return <Badge variant={apparence.variant}>{apparence.libelle}</Badge>;
        },
      },
    ],
    [],
  );

  return (
    <div className="space-y-6">
      <header className="space-y-1">
        <h1 className="text-2xl font-semibold text-[var(--brand-ink)]">
          Alertes de rupture
        </h1>
        <p className="text-sm text-[var(--brand-ink-muted)]">
          Articles à surveiller dans votre filiale. Le seuil d’alerte est défini
          article par article.
        </p>
      </header>

      <div
        role="group"
        aria-label="Filtrer par statut d’alerte"
        className="flex flex-wrap gap-2"
      >
        {STATUTS_ALERTE.map((valeur) => (
          <Button
            key={valeur}
            variant={valeur === statut ? 'default' : 'outline'}
            size="sm"
            aria-pressed={valeur === statut}
            onClick={() => changerFiltre(valeur)}
          >
            {LIBELLES_FILTRE[valeur]}
          </Button>
        ))}
      </div>

      <Card className="p-4">
        <DataTable
          columns={colonnes}
          data={data?.content ?? []}
          keyExtractor={(ligne) => ligne.articleId}
          isLoading={isLoading}
          isError={isError}
          errorTitle="Impossible de charger les alertes"
          errorMessage={
            error instanceof Error ? error.message : 'Une erreur est survenue.'
          }
          onRetry={() => void refetch()}
          emptyTitle="Aucune alerte"
          emptyDescription={DESCRIPTIONS_VIDE[statut]}
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

import { type ReactNode, useState, useMemo } from 'react';
import { cn } from '../lib/utils';
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from './Table';
import { Skeleton, SkeletonText, SkeletonTable } from './Skeleton';
import { EmptyState } from './EmptyState';
import { SearchInput } from './SearchInput';
import { ChevronUp, ChevronDown, ChevronsUpDown, ChevronLeft, ChevronRight, SearchX } from 'lucide-react';
import { Button } from './Button';

// --- Sorting ---
type SortDirection = 'asc' | 'desc';
interface SortState {
  key: string;
  direction: SortDirection;
}

interface Column<T> {
  key: string;
  label: string;
  sortable?: boolean;
  render?: (item: T) => ReactNode;
  className?: string;
}

interface DataTableProps<T> {
  columns: Column<T>[];
  data: T[];
  keyExtractor: (item: T) => string | number;
  isLoading?: boolean;
  isError?: boolean;
  errorTitle?: string;
  errorMessage?: string;
  onRetry?: () => void;
  searchable?: boolean;
  searchPlaceholder?: string;
  onSearch?: (query: string) => void;
  searchValue?: string;
  pagination?: {
    page: number;
    totalPages: number;
    onPageChange: (page: number) => void;
  };
  variant?: 'default' | 'compact' | 'striped';
  emptyTitle?: string;
  emptyDescription?: string;
  onEmptyAction?: () => void;
  emptyActionLabel?: string;
  className?: string;
}

function DataTable<T extends Record<string, unknown>>({
  columns,
  data,
  keyExtractor,
  isLoading,
  isError,
  errorTitle = 'Une erreur est survenue',
  errorMessage = 'Impossible de charger les données.',
  onRetry,
  searchable,
  searchPlaceholder,
  onSearch,
  searchValue,
  pagination,
  variant = 'default',
  emptyTitle = 'Aucune donnée',
  emptyDescription,
  onEmptyAction,
  emptyActionLabel,
  className,
}: DataTableProps<T>) {
  // Local sorting state (only if not server-side)
  const [sortState, setSortState] = useState<SortState | null>(null);

  const handleSort = (key: string) => {
    setSortState((prev) => {
      if (prev?.key === key) {
        return { key, direction: prev.direction === 'asc' ? 'desc' : 'asc' };
      }
      return { key, direction: 'asc' };
    });
  };

  const sortedData = useMemo(() => {
    if (!sortState) return data;
    return [...data].sort((a, b) => {
      const aVal = a[sortState.key];
      const bVal = b[sortState.key];
      if (aVal == null) return 1;
      if (bVal == null) return -1;
      const comparison = String(aVal).localeCompare(String(bVal), 'fr', { numeric: true });
      return sortState.direction === 'asc' ? comparison : -comparison;
    });
  }, [data, sortState]);

  // --- Loading state ---
  if (isLoading) {
    return (
      <div className={cn('space-y-4', className)}>
        {searchable && (
          <div className="w-full max-w-sm">
            <Skeleton className="h-10 rounded-lg" />
          </div>
        )}
        <SkeletonTable rows={5} columns={columns.length} />
      </div>
    );
  }

  // --- Error state ---
  if (isError) {
    return (
      <div className={cn(className)}>
        <EmptyState
          variant="error"
          title={errorTitle}
          description={errorMessage}
          action={onRetry ? { label: 'Réessayer', onClick: onRetry, variant: 'outline' } : undefined}
        />
      </div>
    );
  }

  // --- Empty state ---
  if (sortedData.length === 0) {
    return (
      <div className={cn(className)}>
        {searchable && onSearch && (
          <div className="mb-4 w-full max-w-sm">
            <SearchInput
              value={searchValue}
              onChange={onSearch}
              placeholder={searchPlaceholder || 'Rechercher...'}
            />
          </div>
        )}
        <EmptyState
          variant={searchValue ? 'no-results' : 'no-data'}
          title={searchValue ? 'Aucun résultat' : emptyTitle}
          description={searchValue ? `Aucun élément ne correspond à "${searchValue}"` : emptyDescription}
          action={onEmptyAction ? { label: emptyActionLabel || 'Ajouter', onClick: onEmptyAction } : undefined}
        />
      </div>
    );
  }

  // --- Data state ---
  return (
    <div className={cn('space-y-4', className)}>
      {/* Search */}
      {searchable && onSearch && (
        <div className="flex items-center justify-between gap-4">
          <div className="w-full max-w-sm">
            <SearchInput
              value={searchValue}
              onChange={onSearch}
              placeholder={searchPlaceholder || 'Rechercher...'}
            />
          </div>
          <div className="text-xs text-[var(--brand-ink-muted)] whitespace-nowrap">
            {sortedData.length} résultat{sortedData.length > 1 ? 's' : ''}
          </div>
        </div>
      )}

      {/* Table */}
      <Table variant={variant}>
        <TableHeader>
          <TableRow>
            {columns.map((col) => (
              <TableHead key={col.key} className={col.className}>
                {col.sortable ? (
                  <button
                    type="button"
                    onClick={() => handleSort(col.key)}
                    className="inline-flex items-center gap-1 hover:text-[var(--brand-ink)] transition-colors"
                  >
                    {col.label}
                    {sortState?.key === col.key ? (
                      sortState.direction === 'asc' ? (
                        <ChevronUp className="h-3.5 w-3.5" />
                      ) : (
                        <ChevronDown className="h-3.5 w-3.5" />
                      )
                    ) : (
                      <ChevronsUpDown className="h-3.5 w-3.5 opacity-40" />
                    )}
                  </button>
                ) : (
                  col.label
                )}
              </TableHead>
            ))}
          </TableRow>
        </TableHeader>
        <TableBody>
          {sortedData.map((item) => (
            <TableRow key={keyExtractor(item)}>
              {columns.map((col) => (
                <TableCell key={col.key} className={col.className}>
                  {col.render ? col.render(item) : String(item[col.key] ?? '')}
                </TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>

      {/* Pagination */}
      {pagination && (
        <div className="flex items-center justify-between">
          <div className="text-xs text-[var(--brand-ink-muted)]">
            Page {pagination.page} sur {pagination.totalPages}
          </div>
          <div className="flex items-center gap-1">
            <Button
              variant="outline"
              size="icon-sm"
              disabled={pagination.page <= 1}
              onClick={() => pagination.onPageChange(pagination.page - 1)}
              aria-label="Page précédente"
            >
              <ChevronLeft className="h-4 w-4" />
            </Button>
            {Array.from({ length: pagination.totalPages }, (_, i) => i + 1)
              .filter((p) => {
                // Show first, last, and pages around current
                const range = 2;
                return (
                  p === 1 ||
                  p === pagination.totalPages ||
                  Math.abs(p - pagination.page) <= range
                );
              })
              .map((p, index, arr) => (
                <span key={p} className="inline-flex items-center">
                  {index > 0 && arr[index - 1] !== p - 1 && (
                    <span className="px-1 text-xs text-[var(--brand-ink-muted)]">...</span>
                  )}
                  <Button
                    variant={p === pagination.page ? 'default' : 'ghost'}
                    size="icon-sm"
                    onClick={() => pagination.onPageChange(p)}
                    aria-label={`Page ${p}`}
                    aria-current={p === pagination.page ? 'page' : undefined}
                  >
                    {p}
                  </Button>
                </span>
              ))}
            <Button
              variant="outline"
              size="icon-sm"
              disabled={pagination.page >= pagination.totalPages}
              onClick={() => pagination.onPageChange(pagination.page + 1)}
              aria-label="Page suivante"
            >
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}

DataTable.displayName = 'DataTable';

export { DataTable };
export type { DataTableProps, Column, SortState, SortDirection };

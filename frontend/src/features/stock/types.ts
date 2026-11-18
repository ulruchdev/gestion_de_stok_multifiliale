import type { StatutAlerte } from '@/features/catalogue';

/**
 * Contrats de lecture du stock — miroir exact de `StockArticleResponse` et
 * `PageResponse` backend (US-051, `GET /api/v1/stock`).
 *
 * `stockActuel` et `seuilAlerte` arrivent en JSON number (BigDecimal backend,
 * quantités décimales) ; `statutAlerte` est dérivé en SQL côté backend et ne
 * doit jamais être recalculé ici.
 */
export interface StockArticle {
  articleId: number;
  codeArticle: string;
  designation: string;
  stockActuel: number;
  seuilAlerte: number;
  statutAlerte: StatutAlerte | null;
}

export interface StockListResponse {
  content: StockArticle[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

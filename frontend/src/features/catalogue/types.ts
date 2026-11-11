/**
 * Contrats de lecture du catalogue — miroir exact des DTO backend
 * (`ArticleListResponse`, `CategorieListResponse`), US-032/033 et US-028.
 *
 * Les montants sont des entiers XAF (Integer backend) ; les champs décimaux
 * (`tauxTva`, `margeBrutePct`, `seuilAlerte`, `stockActuel`) arrivent en
 * JSON number. `stockActuel` / `statutAlerte` sont null quand la filiale du
 * JWT n'est pas exploitable : l'écran n'invente alors aucune valeur.
 */

export type StatutAlerte = 'NORMAL' | 'BAS' | 'RUPTURE' | 'ANOMALIE';

export interface Article {
  id: number;
  codeArticle: string;
  designation: string;
  categorieId: number;
  prixAchatHt: number;
  prixVenteHt: number;
  tauxTva: number;
  prixVenteTtc: number;
  margeBrutePct: number | null;
  seuilAlerte: number;
  actif: boolean;
  groupId: number | null;
  stockActuel: number | null;
  statutAlerte: StatutAlerte | null;
}

export interface Categorie {
  id: number;
  code: string;
  designation: string;
  tauxTva: number;
  groupId: number | null;
}

/** Enveloppe paginée des articles (US-032) : contient `first` / `last`. */
export interface ArticleListResponse {
  content: Article[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

/** Enveloppe paginée des catégories (US-028) : contient `first` / `last`. */
export interface CategorieListResponse {
  content: Categorie[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

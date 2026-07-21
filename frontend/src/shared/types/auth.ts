export enum RoleUtilisateur {
  SUPER_ADMIN = 'SUPER_ADMIN',
  ADMIN_GROUPE = 'ADMIN_GROUPE',
  ADMIN_FILIALE = 'ADMIN_FILIALE',
  GESTIONNAIRE_STOCK = 'GESTIONNAIRE_STOCK',
  RESPONSABLE_ACHATS = 'RESPONSABLE_ACHATS',
  COMMERCIAL = 'COMMERCIAL',
  CAISSIER = 'CAISSIER',
}

export enum ScopeUtilisateur {
  GROUPE = 'GROUPE',
  FILIALE = 'FILIALE',
}

export interface Utilisateur {
  id: number;
  email: string;
  nom: string;
  prenom: string;
  role: RoleUtilisateur;
  scope: ScopeUtilisateur;
  entrepriseId: number;
  groupeId?: number;
  actif: boolean;
}

export interface AuthState {
  user: Utilisateur | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

export interface LoginRequest {
  email: string;
  motDePasse: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  utilisateur: Utilisateur;
}

export interface InscriptionEntrepriseUniqueRequest {
  nomEntreprise: string;
  nif: string;
  email: string;
  telephone: string;
  adminNom: string;
  adminPrenom: string;
  adminMotDePasse: string;
}

export interface InscriptionGroupeRequest {
  nomGroupe: string;
  nomEntreprise: string;
  nif: string;
  email: string;
  telephone: string;
  adminNom: string;
  adminPrenom: string;
  adminMotDePasse: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface ProblemResponse {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
  timestamp: string;
  errors?: Record<string, string[]>;
}

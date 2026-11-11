import { RoleUtilisateur } from '@/shared/types';

/**
 * Source unique de la matrice de rôles des sections de l'application.
 * Utilisée à la fois pour filtrer le menu (DashboardLayout) et pour garder
 * les routes (RequireAuth) : le menu et les guards ne peuvent plus diverger.
 */
export const TOUS_LES_ROLES: readonly RoleUtilisateur[] =
  Object.values(RoleUtilisateur);

export const ROLES_PAR_SECTION = {
  dashboard: TOUS_LES_ROLES,
  groupe: [RoleUtilisateur.ADMIN_GROUPE],
  transferts: [RoleUtilisateur.ADMIN_GROUPE],
  catalogue: [
    RoleUtilisateur.ADMIN_GROUPE,
    RoleUtilisateur.ADMIN_FILIALE,
    RoleUtilisateur.GESTIONNAIRE_STOCK,
  ],
  tiers: [
    RoleUtilisateur.ADMIN_FILIALE,
    RoleUtilisateur.COMMERCIAL,
    RoleUtilisateur.RESP_ACHATS,
  ],
  achats: [RoleUtilisateur.ADMIN_FILIALE, RoleUtilisateur.RESP_ACHATS],
  ventes: [
    RoleUtilisateur.ADMIN_FILIALE,
    RoleUtilisateur.COMMERCIAL,
    RoleUtilisateur.CAISSIER,
  ],
  stock: [RoleUtilisateur.ADMIN_FILIALE, RoleUtilisateur.GESTIONNAIRE_STOCK],
  utilisateurs: [RoleUtilisateur.ADMIN_GROUPE, RoleUtilisateur.ADMIN_FILIALE],
  profil: TOUS_LES_ROLES,
} as const satisfies Record<string, readonly RoleUtilisateur[]>;

export type Section = keyof typeof ROLES_PAR_SECTION;

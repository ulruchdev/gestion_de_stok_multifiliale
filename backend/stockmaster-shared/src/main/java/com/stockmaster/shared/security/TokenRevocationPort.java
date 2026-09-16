package com.stockmaster.shared.security;

/**
 * Port de révocation des sessions d'un utilisateur — US-025.
 *
 * <p>Quand un admin désactive un compte, les tokens JWT déjà émis de la cible
 * doivent être révoqués <b>immédiatement</b> (critère d'acceptation US-025) —
 * pas seulement à leur expiration naturelle. La mécanique (blacklist Redis par
 * utilisateur + suppression du refresh) vit dans le module auth, qui seul possède
 * l'espace de clés {@code blacklist:*} et {@code refresh:*} — le module
 * utilisateur n'y accède que par ce port (règle ArchUnit : les modules ne
 * s'importent pas entre eux, uniquement {@code shared}).</p>
 *
 * <p>Même philosophie que US-085 : l'implémentation est <b>fail-open</b> —
 * un incident Redis ne doit jamais faire échouer l'opération métier, la
 * barrière principale restant la vérification {@code actif=false} au login
 * et au refresh.</p>
 */
public interface TokenRevocationPort {

    /**
     * Révoque toutes les sessions actives de l'utilisateur :
     * les access tokens en cours sont refusés dès la prochaine requête
     * ({@code blacklist:user:{userId}}), et le refresh token persisté est
     * supprimé ({@code refresh:{userId}}) — la rotation s'arrête net.
     *
     * @param userId cible de la révocation ; {@code null} = no-op
     */
    void revoquerSessionsDe(Long userId);
}

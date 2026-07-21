package com.stockmaster.shared.exception;

import java.io.Serial;

/**
 * Exception levée lorsqu'une ressource demandée n'est pas trouvée.
 *
 * <p>Utilise le code d'erreur {@code ErrorCode.RES_ENTITY_NOT_FOUND}
 * et retourne un HTTP 404.</p>
 */
public class EntityNotFoundException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String entityName;
    private final Long entityId;

    public EntityNotFoundException(String entityName, Long entityId) {
        super(ErrorCode.RES_ENTITY_NOT_FOUND,
              "%s avec l'identifiant %d n'a pas été trouvé(e).".formatted(entityName, entityId));
        this.entityName = entityName;
        this.entityId = entityId;
    }

    public EntityNotFoundException(String entityName, String field, String value) {
        super(ErrorCode.RES_ENTITY_NOT_FOUND,
              "%s avec %s = '%s' n'a pas été trouvé(e).".formatted(entityName, field, value));
        this.entityName = entityName;
        this.entityId = null;
    }

    public String getEntityName() {
        return entityName;
    }

    public Long getEntityId() {
        return entityId;
    }
}

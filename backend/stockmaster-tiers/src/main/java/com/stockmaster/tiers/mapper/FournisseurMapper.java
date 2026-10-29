package com.stockmaster.tiers.mapper;

import com.stockmaster.tiers.domain.entity.Fournisseur;
import com.stockmaster.tiers.dto.request.CreerFournisseurRequest;
import com.stockmaster.tiers.dto.response.FournisseurResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * US-036 — mapping DTO ↔ entité {@code Fournisseur} (MapStruct 1.6.3).
 *
 * <p>Le rattachement tenant n'est jamais mappé depuis la requête : le champ
 * {@code entreprise} est ignoré à l'entrée et renseigné par le service depuis
 * le JWT (CA2). Il est lu en sortie pour peupler {@code entrepriseId}.</p>
 */
@Mapper(componentModel = "spring")
public interface FournisseurMapper {

    /**
     * Champs portés par le builder Lombok de {@code Fournisseur} uniquement :
     * {@code id}, {@code dateCreation}, {@code dateModification} et
     * {@code supprime} héritent d' {@code AbstractEntity} et ne sont donc pas
     * des propriétés cibles ici (builder Lombok = champs déclarés).
     */
    @Mapping(target = "entreprise", ignore = true)
    Fournisseur toEntity(CreerFournisseurRequest request);

    @Mapping(source = "entreprise.id", target = "entrepriseId")
    FournisseurResponse toResponse(Fournisseur fournisseur);
}

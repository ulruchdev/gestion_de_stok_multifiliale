package com.stockmaster.tiers.mapper;

import com.stockmaster.tiers.domain.entity.Client;
import com.stockmaster.tiers.dto.request.CreerClientRequest;
import com.stockmaster.tiers.dto.response.ClientResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * US-040 — mapping DTO ↔ entité {@code Client} (MapStruct, même pattern que
 * {@code FournisseurMapper}).
 *
 * <p>Le rattachement tenant n'est jamais mappé depuis la requête : le champ
 * {@code entreprise} est ignoré à l'entrée et renseigné par le service depuis
 * le JWT. Il est lu en sortie pour peupler {@code entrepriseId}.</p>
 */
@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "entreprise", ignore = true)
    Client toEntity(CreerClientRequest request);

    @Mapping(source = "entreprise.id", target = "entrepriseId")
    ClientResponse toResponse(Client client);
}

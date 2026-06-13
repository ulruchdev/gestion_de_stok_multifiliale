package com.stockmaster.auth.mapper;

import com.stockmaster.auth.domain.entity.Entreprise;
import com.stockmaster.auth.dto.request.InscriptionEntrepriseUniqueRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "groupe", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "typeEntreprise", constant = "MERE")
    @Mapping(target = "nom", source = "nomBoutique")
    @Mapping(target = "adresseVille", source = "ville")
    @Mapping(target = "adresseQuartier", source = "quartier")
    @Mapping(target = "adressePays", constant = "Cameroun")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "codeFiliale", ignore = true)
    @Mapping(target = "nif", ignore = true)
    @Mapping(target = "telephone", ignore = true)
    @Mapping(target = "adresseRue", ignore = true)
    @Mapping(target = "adresseRegion", ignore = true)
    @Mapping(target = "logo", ignore = true)
    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "supprime", ignore = true)
    Entreprise toEntreprise(InscriptionEntrepriseUniqueRequest request);
}

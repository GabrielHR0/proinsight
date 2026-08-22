package com.prosup.proinsight.infrastructure.persistence.repository;

import com.prosup.proinsight.infrastructure.persistence.document.AcademiaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcademiaRepository extends MongoRepository<AcademiaDocument, String> {

    List<AcademiaDocument> findByOwnerId(String ownerId);

    List<AcademiaDocument> findByOwnerIdIn(List<String> ownerIds);

}

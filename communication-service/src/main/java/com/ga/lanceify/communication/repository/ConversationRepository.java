package com.ga.lanceify.communication.repository;

import com.ga.lanceify.communication.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    List<Conversation> findByParticipantIdsContainingOrderByLastMessageAtDesc(String userId);

    @Query("{ 'participantIds': { $all: [?0, ?1] }, 'projectId': ?2 }")
    Optional<Conversation> findByParticipantIdsContainingAndParticipantIdsContainingAndProjectId(
            String participant1,
            String participant2,
            String projectId
    );

    @Query("{ 'participantIds': { $all: [?0, ?1] }, 'projectId': null }")
    Optional<Conversation> findByParticipantIdsContainingAndParticipantIdsContaining(
            String participant1,
            String participant2
    );
}

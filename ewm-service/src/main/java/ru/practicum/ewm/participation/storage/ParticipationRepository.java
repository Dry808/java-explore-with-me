package ru.practicum.ewm.participation.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.participation.model.Participation;
import ru.practicum.ewm.event.enums.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    List<Participation> findByEvent_IdAndRequester_Id(Long eventId, Long userId);

    List<Participation> findByIdInAndEventId(List<Long> requestIds, Long eventId);

    List<Participation> findByEventIdAndStatus(Long eventId, RequestStatus requestStatus);

    List<Participation> findByRequesterId(Long userId);

    Optional<Participation> findByIdAndRequesterId(Long requestId, Long userId);

    List<Participation> findByEvent_Id(Long eventId);
}

package com.hamster.gro_up.service;

import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.dto.request.RetrospectCreateRequest;
import com.hamster.gro_up.dto.request.RetrospectUpdateRequest;
import com.hamster.gro_up.dto.response.RetrospectListResponse;
import com.hamster.gro_up.dto.response.RetrospectResponse;
import com.hamster.gro_up.entity.Retrospect;
import com.hamster.gro_up.entity.Schedule;
import com.hamster.gro_up.entity.User;
import com.hamster.gro_up.exception.retrospect.RetrospectNotFoundException;
import com.hamster.gro_up.exception.schedule.ScheduleNotFoundException;
import com.hamster.gro_up.exception.user.UserNotFoundException;
import com.hamster.gro_up.repository.RetrospectRepository;
import com.hamster.gro_up.repository.ScheduleRepository;
import com.hamster.gro_up.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class RetrospectService {

    private final RetrospectRepository retrospectRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    public RetrospectResponse findRetrospect(AuthUser authUser, Long retrospectId) {
        Retrospect retrospect = retrospectRepository.findByIdWithSchedule(retrospectId).orElseThrow(RetrospectNotFoundException::new);

        retrospect.validateOwner(authUser.getId());

        return RetrospectResponse.from(retrospect);
    }

    public RetrospectListResponse findAllRetrospects(AuthUser authUser) {
        List<Retrospect> retrospectList = retrospectRepository.findAllByUserIdWithSchedule(authUser.getId());

        List<RetrospectResponse> responseList = retrospectList.stream().map(RetrospectResponse::from).toList();

        return RetrospectListResponse.of(responseList);
    }

    @Transactional
    public RetrospectResponse createRetrospect(AuthUser authUser, RetrospectCreateRequest request) {

        User user = userRepository.findById(authUser.getId()).orElseThrow(UserNotFoundException::new);
        Schedule schedule = scheduleRepository.findByIdWithCompany(request.getScheduleId()).orElseThrow(ScheduleNotFoundException::new);

        Retrospect retrospect = Retrospect.builder()
                .memo(request.getMemo())
                .schedule(schedule)
                .user(user)
                .build();

        Retrospect saveRetrospect = retrospectRepository.save(retrospect);

        return RetrospectResponse.from(saveRetrospect);
    }

    @Transactional
    public void updateRetrospect(AuthUser authUser, Long retrospectId,RetrospectUpdateRequest request) {
        Retrospect retrospect = retrospectRepository.findById(retrospectId).orElseThrow(RetrospectNotFoundException::new);

        retrospect.validateOwner(authUser.getId());

        retrospect.update(request.getMemo());
    }

    @Transactional
    public void deleteRetrospect(AuthUser authUser, Long retrospectId) {
        Retrospect retrospect = retrospectRepository.findById(retrospectId).orElseThrow(RetrospectNotFoundException::new);

        retrospect.validateOwner(authUser.getId());

        retrospectRepository.delete(retrospect);
    }

}

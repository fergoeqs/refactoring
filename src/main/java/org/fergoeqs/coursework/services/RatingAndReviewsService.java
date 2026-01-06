package org.fergoeqs.coursework.services;

import org.apache.coyote.BadRequestException;
import org.fergoeqs.coursework.dto.RatingAndReviewsDTO;
import org.fergoeqs.coursework.exception.BusinessException;
import org.fergoeqs.coursework.exception.ResourceNotFoundException;
import org.fergoeqs.coursework.models.AppUser;
import org.fergoeqs.coursework.models.RatingAndReviews;
import org.fergoeqs.coursework.repositories.RatingAndReviewsRepository;
import org.fergoeqs.coursework.utils.Mappers.RatingAndReviewsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RatingAndReviewsService {
    private final RatingAndReviewsRepository ratingAndReviewsRepository;
    private final UserService userService;
    private final RatingAndReviewsMapper rrMapper;
    private final AppointmentsService appointmentsService;

    public RatingAndReviewsService(RatingAndReviewsRepository ratingAndReviewsRepository, UserService userService,
                                   RatingAndReviewsMapper ratingAndReviewsMapper, AppointmentsService appointmentsService) {
        this.ratingAndReviewsRepository = ratingAndReviewsRepository;
        this.userService = userService;
        this.rrMapper = ratingAndReviewsMapper;
        this.appointmentsService = appointmentsService;
    }

    public RatingAndReviews findById(Long id) {
        return ratingAndReviewsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rating and review not found with id: " + id));
    }

    public List<RatingAndReviews> findAllByVetId(Long vetId) {
        return ratingAndReviewsRepository.findAllByVetId(vetId);
    }

    @Transactional
    public RatingAndReviews save(RatingAndReviewsDTO ratingAndReviewsDTO) throws BadRequestException {
        AppUser owner = userService.getAuthenticatedUser();
        if (!appointmentsService.existsByOwnerAndVet(owner.getId(), ratingAndReviewsDTO.vet())) {
            throw new BusinessException("You cannot leave a review because you did not have an appointment with him");
        }
        if (userService.isVet(owner)) {
            throw new BusinessException("Veterinarian cannot leave reviews");
        }
        RatingAndReviews rr = rrMapper.fromDTO(ratingAndReviewsDTO);
        rr.setVet(userService.findById(ratingAndReviewsDTO.vet())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + ratingAndReviewsDTO.vet())));
        rr.setOwner(owner);
        return ratingAndReviewsRepository.save(rr);
    }

    @Transactional
    public void deleteById(Long id) {
        ratingAndReviewsRepository.deleteById(id);
    }
}

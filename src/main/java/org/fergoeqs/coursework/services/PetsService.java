package org.fergoeqs.coursework.services;

import org.fergoeqs.coursework.dto.PetDTO;
import org.fergoeqs.coursework.exception.ResourceNotFoundException;
import org.fergoeqs.coursework.models.AppUser;
import org.fergoeqs.coursework.models.Pet;
import org.fergoeqs.coursework.models.enums.RoleType;
import org.fergoeqs.coursework.repositories.PetsRepository;
import org.fergoeqs.coursework.utils.Mappers.PetMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Transactional(readOnly = true)
@Service
public class PetsService {
    private final PetsRepository petsRepository;
    private final PetMapper petMapper;
    private final SectorsService sectorsService;
    private final StorageService storageService;

    public PetsService(PetsRepository petsRepository, PetMapper petMapper, SectorsService sectorsService,
                       StorageService storageService) {
        this.petsRepository = petsRepository;
        this.petMapper = petMapper;
        this.sectorsService = sectorsService;
        this.storageService = storageService;
    }

    public List<Pet> findAllPets() {
        return petsRepository.findAll();
    }

    public Pet findPetById(Long petId) {
        return petsRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found with id: " + petId));
    }

    public List<Pet> findPetsByOwner(Long ownerId) {
        return petsRepository.findAllByOwnerId(ownerId);
    }

    public List<Pet> findPetsByVet(Long vetId) {
        return petsRepository.findAllByActualVetId(vetId);
    }

    @Transactional
    public Pet addPet(PetDTO petDTO, AppUser owner) {
        if (owner.getAuthorities().stream().anyMatch(auth ->
                auth.getAuthority().equals("ROLE_USER"))){
            owner.getRoles().clear();
            owner.getRoles().add(RoleType.ROLE_OWNER);
        }
        Pet pet = petMapper.petDTOToPet(petDTO);
        pet.setOwner(owner);
        return petsRepository.save(pet);
    }

    @Transactional
    public Pet updatePet(Long petId, AppUser author, PetDTO petDTO) {
        Pet pet = petsRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found with id: " + petId));
        if (!(pet.getOwner()!= null && pet.getOwner().equals(author)) && !isAdmin(author) && !isVet(author) ) {
            throw new IllegalArgumentException("User is not allowed to update this pet (only for owner, vet or admin)");
        }
        petMapper.updatePetFromDTO(petDTO, pet);
        return petsRepository.save(pet);
    }

    @Transactional
    public void deletePet(Long petId, AppUser deleter) {
        Pet pet = petsRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found with id: " + petId));
        if (!(isAdmin(deleter) && !isVet(deleter))) {
            throw new IllegalArgumentException("User is not allowed to delete pets");
        }
        petsRepository.delete(pet);
    }

    @Transactional
    public void placeInSector(Long petId, Long sectorId) {
        Pet pet = petsRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found with id: " + petId));
        pet.setSector(sectorsService.findSectorById(sectorId));
        petsRepository.save(pet);
    }

    @Transactional
    public void removeFromSector(Long petId) {
        Pet pet = petsRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found with id: " + petId));
        pet.setSector(null);
        petsRepository.save(pet);
    }


    @Transactional
    public void bindPet(Long petId, AppUser vet) {
        Pet pet = petsRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found with id: " + petId));
        if (!isVet(vet)) {
            throw new IllegalArgumentException("User is not allowed to bind pets");
        }
        pet.setActualVet(vet);
        petsRepository.save(pet);
    }

    @Transactional
    public void unbindPet(Long petId) {
        Pet pet = petsRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found with id: " + petId));
        pet.setActualVet(null);
        petsRepository.save(pet);
    }

    @Transactional
    public void updatePetAvatar(Long petId, MultipartFile avatar) throws IOException {
        Pet pet = petsRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found with id: " + petId));
        String contentType = avatar.getContentType();
        if (contentType == null || (!contentType.equals("image/png") && !contentType.equals("image/jpeg"))) {
            throw new IllegalArgumentException("Invalid file type. Only PNG and JPEG are allowed.");
        }
        String objectName = "avatar/" + petId;
        storageService.uploadFile("pets", objectName, avatar.getInputStream(), contentType);
        pet.setPhotoUrl(storageService.generatePublicUrl("pets", objectName));
        petsRepository.save(pet);
    }


    private boolean isVet(AppUser user) {
        return user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_VET"));
    }

    private boolean isAdmin(AppUser user) {
        return user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

}

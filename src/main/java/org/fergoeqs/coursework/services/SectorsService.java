package org.fergoeqs.coursework.services;

import org.fergoeqs.coursework.dto.SectorDTO;
import org.fergoeqs.coursework.exception.ResourceNotFoundException;
import org.fergoeqs.coursework.models.Sector;
import org.fergoeqs.coursework.models.enums.CategoryType;
import org.fergoeqs.coursework.repositories.SectorsRepository;
import org.fergoeqs.coursework.utils.Mappers.SectorMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
public class SectorsService {
    private final SectorsRepository sectorsRepository;
    private final SectorMapper sectorMapper;

    public SectorsService(SectorsRepository sectorsRepository, SectorMapper sectorMapper) {
        this.sectorsRepository = sectorsRepository;
        this.sectorMapper = sectorMapper;
    }

    public Sector findSectorById(Long sectorId) {
        return sectorsRepository.findById(sectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sector not found with id: " + sectorId));
    }

    public List<Sector> findAllSectors() {
        return sectorsRepository.findAll();
    }

    public List<Sector> findAllSectorsByType(CategoryType category) {
        return sectorsRepository.findAllByCategory(category);
    }

    public List<Sector> findAllAvailableByType(CategoryType category) {
        return sectorsRepository.findAllByIsAvailableAndCategory(true, category);
    }

    @Transactional
    public Sector addSector(SectorDTO sectorDTO) {
        Sector sector = sectorMapper.sectorDTOToSector(sectorDTO);
        sector.setOccupancy(0);
        sector.setIsAvailable(true);
        return sectorsRepository.save(sector);
    }

    @Transactional
    public void deleteSector(Long sectorId) {
        sectorsRepository.deleteById(sectorId);
    }

}

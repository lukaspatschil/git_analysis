package com.tuwien.gitanalyser.entity.mapper;

import com.tuwien.gitanalyser.endpoints.dtos.StatsDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.StatsInternalDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StatsMapper {

    StatsDTO dtoToDTO(StatsInternalDTO statsInternalDTO);

    List<StatsDTO> dtosToDTOs(List<StatsInternalDTO> statsInternalDTOS);
}

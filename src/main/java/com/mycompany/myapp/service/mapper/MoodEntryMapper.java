package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.MoodEntry;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.service.dto.MoodEntryDTO;
import com.mycompany.myapp.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link MoodEntry} and its DTO {@link MoodEntryDTO}.
 */
@Mapper(componentModel = "spring")
public interface MoodEntryMapper extends EntityMapper<MoodEntryDTO, MoodEntry> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    MoodEntryDTO toDto(MoodEntry s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}

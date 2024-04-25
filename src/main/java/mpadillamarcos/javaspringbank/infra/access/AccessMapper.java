package mpadillamarcos.javaspringbank.infra.access;

import mpadillamarcos.javaspringbank.domain.access.AccountAccessRepository;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccessMapper extends AccountAccessRepository {
}

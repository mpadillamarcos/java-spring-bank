package mpadillamarcos.javaspringbank.infra.account;

import mpadillamarcos.javaspringbank.domain.account.AccountRepository;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper extends AccountRepository {

}

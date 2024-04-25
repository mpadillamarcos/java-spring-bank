package mpadillamarcos.javaspringbank.infra.balance;

import mpadillamarcos.javaspringbank.domain.balance.BalanceRepository;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BalanceMapper extends BalanceRepository {
}

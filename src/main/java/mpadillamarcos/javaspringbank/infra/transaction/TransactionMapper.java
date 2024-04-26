package mpadillamarcos.javaspringbank.infra.transaction;

import mpadillamarcos.javaspringbank.domain.transaction.TransactionRepository;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionMapper extends TransactionRepository {
}

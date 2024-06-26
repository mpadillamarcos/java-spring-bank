package mpadillamarcos.javaspringbank.infra;

import mpadillamarcos.javaspringbank.infra.type.AccountIdTypeHandler;
import mpadillamarcos.javaspringbank.infra.type.GroupIdTypeHandler;
import mpadillamarcos.javaspringbank.infra.type.TransactionIdTypeHandler;
import mpadillamarcos.javaspringbank.infra.type.UserIdTypeHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.test.autoconfigure.AutoConfigureMybatis;
import org.springframework.test.context.ContextConfiguration;

@AutoConfigureMybatis
@ContextConfiguration(classes = {
        UserIdTypeHandler.class,
        AccountIdTypeHandler.class,
        TransactionIdTypeHandler.class,
        GroupIdTypeHandler.class
})
@MapperScan(basePackages = "mpadillamarcos.javaspringbank.infra")
public class MapperTestBase extends DbTestBase {
}

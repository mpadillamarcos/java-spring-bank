package mpadillamarcos.javaspringbank.infra.type;

import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.Id;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Function;

@RequiredArgsConstructor
public class IdTypeHandler<T extends Id<UUID>> extends BaseTypeHandler<T> {

    private final Function<UUID, T> constructor;

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter.value());
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return constructor.apply(rs.getObject(columnName, UUID.class));
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return constructor.apply(rs.getObject(columnIndex, UUID.class));
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return constructor.apply(cs.getObject(columnIndex, UUID.class));
    }
}

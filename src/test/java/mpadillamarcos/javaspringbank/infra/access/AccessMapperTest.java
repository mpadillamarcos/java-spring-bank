package mpadillamarcos.javaspringbank.infra.access;

import mpadillamarcos.javaspringbank.infra.MapperTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static mpadillamarcos.javaspringbank.domain.Instances.dummyAccountAccess;
import static mpadillamarcos.javaspringbank.domain.access.AccessState.REVOKED;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.OPERATOR;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.VIEWER;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {AccessMapper.class})
public class AccessMapperTest extends MapperTestBase {

    @Autowired
    private AccessMapper mapper;

    @Test
    void returns_empty_when_user_has_no_access_to_the_account() {
        var access = mapper.findAccountAccess(randomAccountId(), randomUserId());

        assertThat(access).isEmpty();
    }

    @Test
    void returns_one_inserted_access() {
        var access = dummyAccountAccess().build();
        mapper.insert(access);

        var storedAccess = mapper.findAccountAccess(access.getAccountId(), access.getUserId());

        assertThat(storedAccess).hasValue(access);
    }

    @Test
    void updates_the_state_of_an_already_inserted_access() {
        var access = dummyAccountAccess().build();
        mapper.insert(access);

        mapper.update(access.revoke());

        var storedAccess = mapper.findAccountAccess(access.getAccountId(), access.getUserId());

        assertThat(storedAccess).hasValue(access.revoke());
    }

    @Test
    void updates_the_type_of_an_already_inserted_access() {
        var access = dummyAccountAccess().type(VIEWER).build();
        mapper.insert(access);

        mapper.update(access.grant(OPERATOR));

        var storedAccess = mapper.findAccountAccess(access.getAccountId(), access.getUserId());

        assertThat(storedAccess).hasValue(access.grant(OPERATOR));
    }

    @Test
    void returns_the_access_state_for_one_user_if_it_is_granted() {
        var access = dummyAccountAccess().build();
        mapper.insert(access);

        var storedAccess = mapper.findGrantedAccountAccess(access.getAccountId(), access.getUserId());

        assertThat(storedAccess).hasValue(access);
    }

    @Test
    void returns_empty_when_the_access_state_for_one_user_is_revoked() {
        var access = dummyAccountAccess().state(REVOKED).build();
        mapper.insert(access);

        var storedAccess = mapper.findGrantedAccountAccess(access.getAccountId(), access.getUserId());

        assertThat(storedAccess).isEmpty();
    }

    @Test
    void returns_a_list_of_all_granted_accesses_that_one_user_has() {
        var userId = randomUserId();
        var access1 = dummyAccountAccess().userId(userId).build();
        var access2 = dummyAccountAccess().userId(userId).type(VIEWER).build();
        var access3 = dummyAccountAccess().userId(userId).type(OPERATOR).state(REVOKED).build();
        mapper.insert(access1);
        mapper.insert(access2);
        mapper.insert(access3);

        var listAccesses = mapper.listGrantedAccountAccesses(userId);

        assertThat(listAccesses).containsExactly(access1, access2);
    }
}

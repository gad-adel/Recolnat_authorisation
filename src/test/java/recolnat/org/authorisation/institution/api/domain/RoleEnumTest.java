package recolnat.org.authorisation.institution.api.domain;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import recolnat.org.authorisation.api.domain.enums.ModeEnum;
import recolnat.org.authorisation.api.domain.enums.RoleEnum;
import recolnat.org.authorisation.common.exception.AuthorisationBusinessException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static recolnat.org.authorisation.api.domain.enums.RoleEnum.ADMIN;
import static recolnat.org.authorisation.api.domain.enums.RoleEnum.ADMIN_COLLECTION;
import static recolnat.org.authorisation.api.domain.enums.RoleEnum.ADMIN_INSTITUTION;
import static recolnat.org.authorisation.api.domain.enums.RoleEnum.DATA_ENTRY;
import static recolnat.org.authorisation.api.domain.enums.RoleEnum.USER_INFRA;
import static recolnat.org.authorisation.api.domain.enums.RoleEnum.getAssignableRole;
import static recolnat.org.authorisation.api.domain.enums.RoleEnum.isCollectionMaster;
import static recolnat.org.authorisation.api.domain.enums.RoleEnum.isDataEntry;
import static recolnat.org.authorisation.api.domain.enums.RoleEnum.isInstitutionRoleLevel;

@Slf4j
class RoleEnumTest {

    @ParameterizedTest()
    @ValueSource(strings = {"ADD", "REMOVE", "LIST"})
    void isAssignableRole_from_adminInstitution_to_collection_admin(ModeEnum mode) {
        assertThat(RoleEnum.isAssignableRole(ADMIN_INSTITUTION, ADMIN_COLLECTION, mode)).isTrue();
    }

    @Test
    void check_contains() {
        log.info("value :{}", HttpStatus.NOT_FOUND.value());
        assertThat(StringUtils.contains("HTTP 404 Not Found", String.valueOf(HttpStatus.NOT_FOUND.value()))).isTrue();

    }

    @ParameterizedTest()
    @ValueSource(strings = {"ADD", "REMOVE", "LIST"})
    void isAssignableRole_from_adminInstitution_to_data_entry(ModeEnum mode) {
        assertThat(RoleEnum.isAssignableRole(ADMIN_INSTITUTION, RoleEnum.DATA_ENTRY, mode)).isTrue();
    }

    @ParameterizedTest()
    @ValueSource(strings = {"ADD", "REMOVE", "LIST"})
    void isAssignableRole_from_adminInstitution_to_admin(ModeEnum mode) {
        assertThat(RoleEnum.isAssignableRole(ADMIN_INSTITUTION, RoleEnum.ADMIN, mode)).isFalse();
    }

    @Test
    void isAssignableRole_from_should_fail() {
        assertThrows(AuthorisationBusinessException.class, () -> RoleEnum.fromValue("ADMIN_INSTITUTION1"));
    }

    @Test
    void isAssignableRole_from_should_ok() {
        assertThat(RoleEnum.fromValue("ADMIN_INSTITUTION")).isEqualTo(ADMIN_INSTITUTION);

    }

    @ParameterizedTest()
    @ValueSource(strings = {"ADD", "REMOVE", "LIST"})
    void get_assignableRole_from_should_ok(ModeEnum mode) {
        assertThat(RoleEnum.getAssignableRole(ADMIN_INSTITUTION, mode)).usingRecursiveComparison().ignoringCollectionOrder()
                .isEqualTo(List.of(DATA_ENTRY, ADMIN_COLLECTION));
    }

    @ParameterizedTest()
    @ValueSource(strings = {"ADD", "REMOVE", "LIST"})
    void get_assignableRole_from_should_be_null(ModeEnum mode) {
        assertThat(RoleEnum.getAssignableRole(USER_INFRA, mode)).usingRecursiveComparison().ignoringCollectionOrder()
                .isEqualTo(List.of());
    }

    @ParameterizedTest()
    @ValueSource(strings = {"ADD", "REMOVE", "LIST"})
    void get_assignableRole_from_should__null(ModeEnum mode) {
        var assignableRoles = getAssignableRole(USER_INFRA, mode);
        assertThat(assignableRoles).doesNotContain(ADMIN);
        assertThat(assignableRoles).usingRecursiveComparison().ignoringCollectionOrder()
                .isEqualTo(List.of());
    }

    @Test
    void is_data_entry_should_be_ok() {
        assertThat(isDataEntry(DATA_ENTRY)).isTrue();
    }

    @Test
    void is_admin_collection_should_be_ok() {
        assertThat(isCollectionMaster(ADMIN_COLLECTION)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"DATA_ENTRY", "ADMIN_COLLECTION", "ADMIN_INSTITUTION"})
    void is_admin_inst_level_should_be_ok(String role) {
        assertThat(isInstitutionRoleLevel(RoleEnum.fromValue(role))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "USER"})
    void is_admin_not_inst_level_should_be_ok(String role) {
        assertThat(isInstitutionRoleLevel(RoleEnum.fromValue(role))).isFalse();
    }


}

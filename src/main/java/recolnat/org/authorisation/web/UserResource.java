package recolnat.org.authorisation.web;


import io.recolnat.api.RetrieveAllUsersApi;
import io.recolnat.model.UnassignedUserDTO;
import io.recolnat.model.UserDTO;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import recolnat.org.authorisation.common.exception.AuthorisationBusinessException;
import recolnat.org.authorisation.common.exception.AuthorisationTechnicalException;
import recolnat.org.authorisation.common.mapper.UserMapper;
import recolnat.org.authorisation.service.UserService;
import recolnat.org.authorisation.web.dto.AdminUserPageResponseDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UserResource implements RetrieveAllUsersApi {

    private static final String APPLICATIVE_ERROR = "Applicative Error";
    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return RetrieveAllUsersApi.super.getRequest();
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/v1/users",
            produces = {"application/json"}
    )
    public ResponseEntity<AdminUserPageResponseDTO> getAdminUsers(
            @NotNull @Min(0) @Parameter(name = "page", required = true, in = ParameterIn.QUERY) @Valid @RequestParam(value = "page") Integer page,
            @NotNull @Parameter(name = "size", required = true, in = ParameterIn.QUERY) @Valid @RequestParam(value = "size") Integer size,
            @Parameter(name = "q", description = "Nom recherché", in = ParameterIn.QUERY) @Valid @RequestParam(value = "q", required = false) String searchTerm,
            @Parameter(name = "institution_id", description = "Institution recherchée", in = ParameterIn.QUERY)
            @Valid @RequestParam(value = "institution_id", required = false) UUID institutionId) {
        final var userProfilePage = userService.findAll(page, size, searchTerm, institutionId);
        return new ResponseEntity<>(userMapper.toAdminResponseDTO(userProfilePage), OK);
    }

    @Override
    public ResponseEntity<List<UnassignedUserDTO>> getUnassignedUsers() {
        try {
            final var userProfilePage = userService.findAllUnassignedUsers();
            return new ResponseEntity<>(userProfilePage, OK);
        } catch (AuthorisationBusinessException | AuthorisationTechnicalException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthorisationBusinessException(HttpStatus.CONFLICT, APPLICATIVE_ERROR, e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<UserDTO> getUserById(UUID uid) {
        try {
            final var user = userMapper.toDto(userService.getUserByUid(uid));
            return new ResponseEntity<>(user, OK);
        } catch (AuthorisationBusinessException | AuthorisationTechnicalException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthorisationBusinessException(HttpStatus.CONFLICT, APPLICATIVE_ERROR, e.getMessage());
        }
    }

}

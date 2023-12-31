package es.com.unir.slrassistant.security.model.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChangeUserPasswordRequest {
    private String currentPassword;
    private String newPassword;
}

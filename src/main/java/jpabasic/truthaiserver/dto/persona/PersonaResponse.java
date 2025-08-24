package jpabasic.truthaiserver.dto.persona;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonaResponse{
    private String persona;

    public PersonaResponse(String persona) {
        this.persona = persona;
    }
}

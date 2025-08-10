package jpabasic.truthaiserver.dto.answer.openai;

import jpabasic.truthaiserver.dto.answer.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Choice {

    private int index;
    private Message message;

}

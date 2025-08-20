package jpabasic.truthaiserver.service.prompt;

import jpabasic.truthaiserver.domain.PromptDomain;
import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.dto.prompt.BasePromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class OptimizedTemplate extends BasePromptTemplate {


//    @Override
//    protected String domainGuidelines() {
//        return """
//            # Constraints
//                - Do not reveal internal reasoning. Instead, include a one-sentence rationale labeled "Why".
//                - There must be a specific source with "Why"
//                - If key facts may be time-sensitive, state that you would verify with recent sources before finalizing.
//                - Be clear, specific, and avoid hedging.
//                - If there is no 100% accurate sources, say "i can't find right sources. sorry", then don't give any sources.
//            """;
//    }


    @Override
    protected String userContent(Message message) {
        String text=message.getContent();
        return """
                ## Your task:
                Answer the following question, delimited by triple backticks.\s
                Provide a detailed and well-structured answer with at least 3 paragraphs. Include examples, explanations, and comparisons if relevant. 
                
                
                ## Question: ```%s```
                
                
                """.formatted(text);
    }

    @Override
    public String key() {
        return "optimized";
    }



    public String getOptimizedPrompt(PromptDomain domain, String persona, Message message) {
        StringBuilder sb=new StringBuilder();
        sb.append(systemIdentity(domain,persona)).append("\n\n")
                .append(domainGuidelines()).append("\n\n")
                .append(userContent(message));
        return sb.toString();
    }
}

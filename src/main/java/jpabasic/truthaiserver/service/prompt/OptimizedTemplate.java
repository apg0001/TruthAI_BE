package jpabasic.truthaiserver.service.prompt;

import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.dto.prompt.BasePromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class OptimizedTemplate extends BasePromptTemplate {
    @Override
    //도메인 + 페르소나
    protected String systemIdentity(String domain,String persona) {

        return String.format("You are an expert %s advisor and educator for %s.",domain,persona);

    }

    @Override
    protected String domainGuidelines() {
        return """
            # Constraints
                - Do not reveal internal reasoning. Instead, include a one-sentence rationale labeled "Why".
                - There must be a specific source with "Why"
                - If key facts may be time-sensitive, state that you would verify with recent sources before finalizing.
                - Be clear, specific, and avoid hedging.
            """;
    }


    @Override
    //유저가 하는 질문
    protected String userContent(Message message) {
        String text=message.getContent();
        return """
                ## Your task:
                Answer the following question, delimited by triple backticks.\s
                Provide a detailed and well-structured answer with at least 3 paragraphs. Include examples, explanations, and comparisons if relevant. 
                
                
                ## Question: ```%s```
                
                ## Output format (strict):
                1) Think and prepare your answer in English internally, but only ouptut the final answer in Korean. Do not include English text in the output.
                2) Why: Provide a concise reason explaining why your answer is correct or reliable, and clearly reference the sources. 
                3) Sources: List at least 2 different credible source URLs you used, in bullet point format. Eacu URL must be directly relevant to the content. 
                
            
                
                
                """.formatted(text);
    }

    @Override
    public String key() {
        return "optimized";
    }



    public String getOptimizedPrompt(String domain,String persona,Message message) {
        StringBuilder sb=new StringBuilder();
        sb.append(systemIdentity(domain,persona)).append("\n\n")
                .append(domainGuidelines()).append("\n\n")
                .append(userContent(message));
        return sb.toString();
    }
}

package jpabasic.truthaiserver.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "prompts")
@Getter
@NoArgsConstructor
public class Prompt extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_prompt", nullable = false, columnDefinition = "Text")
    private String originalPrompt;

    @Column(name = "optimized_prompt", columnDefinition = "Text")
    private String optimizedPrompt;

    @Column(columnDefinition = "Text")
    private String summary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "prompt", cascade = CascadeType.ALL)
    private List<Answer> answers = new ArrayList<>();

    // answers 리스트 getter (Lombok @Getter와 함께 작동)
    public List<Answer> getAnswers() {
        return answers;
    }

    public Prompt(String originalPrompt,List<Answer> answers,User user,String summary) {
        this.originalPrompt = originalPrompt;
        this.answers = answers;
        this.user = user;
        this.summary = summary;
        
        // 양방향 관계 설정: 각 Answer의 prompt와 user 설정
        if (answers != null) {
            for (Answer answer : answers) {
                answer.setPrompt(this);
                answer.setUser(user);
            }
        }
    }

    public Prompt(String originalPrompt,List<Answer> answers,User user) {
        this.originalPrompt = originalPrompt;
        this.answers = answers;
        this.user = user;
        
        // 양방향 관계 설정: 각 Answer의 prompt와 user 설정
        if (answers != null) {
            for (Answer answer : answers) {
                answer.setPrompt(this);
                answer.setUser(user);
            }
        }
    }

    public void assignUser(User user) {
        this.user = user;
    }

    public void savePrompt(String originalPrompt,String optimizedPrompt,String summary) {
        this.originalPrompt = originalPrompt;
        this.optimizedPrompt = optimizedPrompt;
        this.summary = summary;
    }

    public void optimize(String optimizedPrompt) {
        this.optimizedPrompt = optimizedPrompt;
    }

    public void addAnswer(Answer answer) {
        answers.add(answer);
    }

    public void assignFolder(Folder folder) {
        this.folder = folder;
    }

    // answers 리스트 설정을 위한 메서드 추가
    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

}

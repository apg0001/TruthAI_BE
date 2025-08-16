package jpabasic.truthaiserver.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "answers")
@Getter
@NoArgsConstructor
public class Answer extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LLMModel model;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "opinion", columnDefinition = "TEXT")
    private String opinion;

    @Column(name = "score")
    private Float score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id")
    private Prompt prompt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable=false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @OneToMany(mappedBy = "answer", cascade = CascadeType.ALL)
    private List<Claim> claims = new ArrayList<>();

    public void updateOpinionAndScore(String opinion, Float score) {
        this.opinion = opinion;
        this.score = score;
    }
  
    public Answer(LLMModel model,String answer){
        this.model = model;
        this.content = answer;
    }

    public Answer(String content,LLMModel model,Prompt prompt,User user){
        this.content = content;
        this.model = model;
        this.prompt = prompt;
        this.user = user;
    }
}

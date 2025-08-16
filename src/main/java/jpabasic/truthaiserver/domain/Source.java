package jpabasic.truthaiserver.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sources")
@Getter
@NoArgsConstructor
public class Source {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sourceUrl;

    private String sourceTitle;

    @Column(name = "source_summary", columnDefinition = "TEXT")
    private String sourceSummary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id")
    private Claim claim;

    //prompt or answer와 매핑
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="answer_id")
    private Answer answer;

    public Source(String sourceTitle,String sourceUrl,Answer answer) {
        this.sourceTitle = sourceTitle;
        this.sourceUrl = sourceUrl;
        this.answer = answer;
    }


}
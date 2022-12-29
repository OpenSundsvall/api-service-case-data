package se.sundsvall.casedata.integration.db.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@ToString
public class Law {

    // Rubrik
    @Column(name = "heading")
    private String heading;

    // Svensk författningssamling, (SFS)
    @Column(name = "sfs")
    private String sfs;

    // kapitel
    @Column(name = "chapter")
    private String chapter;

    // paragraf
    @Column(name = "article")
    private String article;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Law law)) return false;
        return Objects.equals(heading, law.heading) && Objects.equals(sfs, law.sfs) && Objects.equals(chapter, law.chapter) && Objects.equals(article, law.article);
    }

    @Override
    public int hashCode() {
        return Objects.hash(heading, sfs, chapter, article);
    }
}

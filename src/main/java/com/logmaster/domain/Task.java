package com.logmaster.domain;

import com.logmaster.domain.verification.Verification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

@Data
public class Task {
    private String id;
    private String name;
    private String tip;
    private String wikiLink;
    private int displayItemId;
    private Set<Tag> tags;

    private @Nullable Verification verification;

    public Set<Tag> getTags() {
        if (tags == null) {
            tags = new HashSet<>();
        }

        return tags;
    }
}

package com.collectionlogmaster.domain;

import com.collectionlogmaster.domain.verification.Verification;
import lombok.Data;

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

    public boolean isLMS() {
        return getTags().contains(Tag.LMS);
    }
}

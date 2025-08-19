package com.collectionlogmaster.domain.verification.clog;

import com.collectionlogmaster.domain.verification.Verification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CollectionLogVerification extends Verification {
    private int @NonNull [] itemIds;
    private int count;
}

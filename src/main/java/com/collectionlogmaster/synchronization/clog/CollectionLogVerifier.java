package com.collectionlogmaster.synchronization.clog;

import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.domain.verification.clog.CollectionLogVerification;
import com.collectionlogmaster.synchronization.Verifier;
import lombok.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

@Singleton
public class CollectionLogVerifier implements Verifier {
    @Inject
    private CollectionLogService collectionLogService;

    public boolean supports(@NonNull Task task) {
        return task.getVerification() instanceof CollectionLogVerification;
    }

    public boolean verify(@NonNull Task task) {
        assert task.getVerification() instanceof CollectionLogVerification;
        CollectionLogVerification verif = (CollectionLogVerification) task.getVerification();

        long totalObtained = Arrays.stream(verif.getItemIds())
                .filter(itemId -> this.collectionLogService.isItemObtained(itemId))
                .count();

        return totalObtained >= verif.getCount();
    }
}

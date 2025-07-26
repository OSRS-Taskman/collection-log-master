package com.logmaster.domain;

import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nullable;

@ToString
@Getter
public class BaseSaveData {
    protected @Nullable Integer version = null;
}

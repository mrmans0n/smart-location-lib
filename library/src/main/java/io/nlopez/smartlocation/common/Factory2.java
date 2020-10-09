package io.nlopez.smartlocation.common;

import androidx.annotation.NonNull;

public interface Factory2<T, ARG1, ARG2> {
    @NonNull
    T create(ARG1 arg1, ARG2 arg2);
}

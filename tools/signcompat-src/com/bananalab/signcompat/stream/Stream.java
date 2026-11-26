package com.bananalab.signcompat.stream;
import com.bananalab.signcompat.function.*;
public interface Stream<T>{<R> Stream<R> map(Function<? super T,? extends R> f);boolean anyMatch(Predicate<? super T> f);IntStream mapToInt(ToIntFunction<? super T> f);LongStream mapToLong(ToLongFunction<? super T> f);}

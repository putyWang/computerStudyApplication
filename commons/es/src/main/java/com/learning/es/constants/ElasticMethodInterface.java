package com.learning.es.constants;

import java.io.IOException;

public interface ElasticMethodInterface<T> {
    void run(T var1) throws InterruptedException, IOException;
}

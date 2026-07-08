package com.omar.domain.port.out;

import com.omar.domain.model.Autor;
import io.smallrye.mutiny.Uni;

public interface AutorProviderPort {

  Uni<Autor> fetchAutor(Long id);
}

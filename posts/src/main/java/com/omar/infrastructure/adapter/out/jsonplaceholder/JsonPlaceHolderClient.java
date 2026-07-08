package com.omar.infrastructure.adapter.out.jsonplaceholder;

import com.omar.infrastructure.adapter.out.jsonplaceholder.dto.JphPostDto;
import com.omar.infrastructure.adapter.out.jsonplaceholder.dto.JphUserDto;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "jsonplaceholder")
public interface JsonPlaceHolderClient {

  @GET
  @Path("/posts")
  Uni<List<JphPostDto>> getPosts();

  @GET
  @Path("/posts/{id}")
  Uni<JphPostDto> getPost(@PathParam("id") Long id);

  @GET
  @Path("/users/{id}")
  Uni<JphUserDto> getUser(@PathParam("id") Long id);
}

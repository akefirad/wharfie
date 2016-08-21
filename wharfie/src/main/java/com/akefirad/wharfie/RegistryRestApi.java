package com.akefirad.wharfie;

import com.akefirad.wharfie.payload.BaseResponse;
import com.akefirad.wharfie.payload.CatalogResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RegistryRestApi {
    @GET ("v2/")
    Call<BaseResponse> getBase ();

    @GET ("v2/_catalog")
    Call<CatalogResponse> getCatalog ();

    @GET ("v2/_catalog")
    Call<CatalogResponse> getCatalog (@Query ("n") int count);

    @GET ("v2/_catalog")
    Call<CatalogResponse> getCatalog (@Query ("n") int count, @Query ("last") String last);
}

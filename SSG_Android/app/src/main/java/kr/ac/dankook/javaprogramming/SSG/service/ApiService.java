package kr.ac.dankook.javaprogramming.SSG.service;

import java.util.List;

import kr.ac.dankook.javaprogramming.SSG.dto.MyAssetDto;
import kr.ac.dankook.javaprogramming.SSG.dto.StockHistoryResponse;
import kr.ac.dankook.javaprogramming.SSG.dto.StockListResponse;
import kr.ac.dankook.javaprogramming.SSG.dto.TradeRequest;
import kr.ac.dankook.javaprogramming.SSG.dto.UserRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @POST("/api/v1/auth/signup")
    Call<String> signUp(@Body UserRequest userRequest);

    @POST("/api/v1/auth/login")
    Call<String> login(@Body UserRequest loginRequest);

    @GET("api/v1/stocks")
    Call<List<StockListResponse>> getStockList();

    @GET("api/v1/stocks/{symbol}/history")
    Call<List<StockHistoryResponse>> getStockHistory(@Path("symbol") String symbol);

    @GET("api/v1/asset/{userId}")
    Call<MyAssetDto> getMyAsset(@Path("userId") Long userId);

    @POST("api/v1/trade/buy")
    Call<Void> buyStock(@Body TradeRequest request);

    @POST("api/v1/trade/sell")
    Call<Void> sellStock(@Body TradeRequest request);

    @POST("api/v1/stocks/reset")
    Call<String> resetSimulation();
}

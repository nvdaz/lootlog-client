package xyz.nvda.lootlog;

import com.apollographql.apollo.ApolloCall.Callback;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.CustomTypeAdapter;
import com.apollographql.apollo.api.Operation.Variables;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.ResponseField;
import com.apollographql.apollo.cache.normalized.CacheKey;
import com.apollographql.apollo.cache.normalized.CacheKeyResolver;
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy;
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory;
import com.apollographql.apollo.exception.ApolloException;
import com.google.gson.Gson;
import xyz.nvda.lootlog.api.CompleteChallengeMutation;
import xyz.nvda.lootlog.api.InitChallengeMutation;
import xyz.nvda.lootlog.api.InitChallengeMutation.Data;
import xyz.nvda.lootlog.api.InitChallengeMutation.InitChallenge;
import xyz.nvda.lootlog.api.type.CustomType;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;

public class ApolloProvider {
  private static final ConfigurationHandler config = ConfigurationHandler.getInstance();
  private static final ApolloProvider instance = new ApolloProvider();

  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
  private static final String BASE_URL = "http://192.168.86.88/graphql";
  private String accessToken;
  private final OkHttpClient httpClient =
      new OkHttpClient.Builder()
          .addInterceptor(
              new Interceptor() {
                @NotNull
                @Override
                public okhttp3.Response intercept(@NotNull Chain chain) throws IOException {
                  Request request = chain.request();
                  return chain.proceed(
                      accessToken == null
                          ? request
                          : request
                              .newBuilder()
                              .addHeader("Authorization", "Bearer " + accessToken)
                              .build());
                }
              })
          .build();
  private final ApolloClient apolloClient =
      ApolloClient.builder()
          .serverUrl(BASE_URL)
          .addCustomTypeAdapter(
              CustomType.DATE,
              new CustomTypeAdapter<Instant>() {
                @Nonnull
                @Override
                public Instant decode(@Nonnull String value) {
                  return Instant.ofEpochMilli(Long.parseLong(value));
                }

                @Nonnull
                @Override
                public String encode(@Nonnull Instant value) {
                  return String.valueOf(value.toEpochMilli());
                }
              })
          .addCustomTypeAdapter(
              CustomType.REGEXP,
              new CustomTypeAdapter<Pattern>() {
                @Nonnull
                @Override
                public Pattern decode(@Nonnull String value) {
                  List<Character> flags =
                      value
                          .substring(value.lastIndexOf('/') + 1)
                          .chars()
                          .mapToObj(e -> (char) e)
                          .collect(Collectors.toList());

                  return Pattern.compile(
                      value.substring(value.indexOf('/') + 1, value.lastIndexOf('/')),
                      flags.contains('i') ? Pattern.CASE_INSENSITIVE : 0);
                }

                @Nonnull
                @Override
                public String encode(@Nonnull Pattern value) {
                  return value.toString();
                }
              })
          .normalizedCache(
              new LruNormalizedCacheFactory(
                  EvictionPolicy.builder().maxSizeBytes(10 * 1024).build()),
              new CacheKeyResolver() {
                @Nonnull
                @Override
                public CacheKey fromFieldRecordSet(
                    @Nonnull ResponseField field, @Nonnull Map<String, Object> recordSet) {
                  return formatCacheKey((String) recordSet.get("_id"));
                }

                @Nonnull
                @Override
                public CacheKey fromFieldArguments(
                    @Nonnull ResponseField field, @Nonnull Variables variables) {
                  return formatCacheKey((String) field.resolveArgument("_id", variables));
                }

                private CacheKey formatCacheKey(String id) {
                  if (id == null || id.isEmpty()) {
                    return CacheKey.NO_KEY;
                  } else {
                    return CacheKey.from(id);
                  }
                }
              })
          .okHttpClient(httpClient)
          .build();

  private ApolloProvider() {}

  public static ApolloProvider getInstance() {
    return instance;
  }

  public ApolloClient getClient() {
    return this.apolloClient;
  }

  public boolean hasToken() {
    return this.accessToken != null;
  }

  public void refreshAccessToken(Consumer<RefreshTokenResult> consumer) {
    this.refreshAccessToken(consumer, 0);
  }

  public void refreshAccessToken(Consumer<RefreshTokenResult> consumer, int errorCount) {
    if (!config.getAccessToken().equals("")) {
      this.accessToken = config.getAccessToken();
      consumer.accept(RefreshTokenResult.SUCCESS_CONFIG);
      return;
    }

    String uuid =
        Minecraft.getMinecraft().getSession().getProfile().getId().toString().replaceAll("-", "");
    InitChallengeMutation initChallengeMutation =
        InitChallengeMutation.builder().uuid(uuid).build();

    apolloClient
        .mutate(initChallengeMutation)
        .enqueue(
            new Callback<Data>() {
              @Override
              public void onResponse(
                  @Nonnull Response<InitChallengeMutation.Data> initChallengeResponse) {
                if (initChallengeResponse.data() == null) {
                  if (errorCount < 1) refreshAccessToken(consumer, errorCount + 1);
                  else consumer.accept(RefreshTokenResult.FAILED_INIT_REQUEST);
                  return;
                }
                InitChallenge initChallengeData = initChallengeResponse.data().initChallenge();

                JoinInput joinInput =
                    new JoinInput(
                        Minecraft.getMinecraft().getSession().getToken(),
                        uuid,
                        initChallengeData.serverID());

                Request request =
                    new Request.Builder()
                        .url("https://sessionserver.mojang.com/session/minecraft/join")
                        .post(RequestBody.Companion.create(joinInput.toString(), JSON))
                        .build();

                try (okhttp3.Response joinResponse = httpClient.newCall(request).execute()) {
                  if (joinResponse.code() != 200 && joinResponse.code() != 204) {
                    if (errorCount < 1) refreshAccessToken(consumer, errorCount + 1);
                    else consumer.accept(RefreshTokenResult.FAILED_MOJANG_AUTH);
                    return;
                  }
                  CompleteChallengeMutation completeChallengeMutation =
                      CompleteChallengeMutation.builder().token(initChallengeData.token()).build();

                  apolloClient
                      .mutate(completeChallengeMutation)
                      .enqueue(
                          new Callback<CompleteChallengeMutation.Data>() {
                            @Override
                            public void onResponse(
                                @Nonnull
                                    Response<CompleteChallengeMutation.Data>
                                        completeChallengeResponse) {
                              if (completeChallengeResponse.data() == null) {
                                if (errorCount < 1) refreshAccessToken(consumer, errorCount + 1);
                                else consumer.accept(RefreshTokenResult.FAILED_COMPLETION_REQUEST);
                                return;
                              }

                              accessToken = completeChallengeResponse.data().token();

                              consumer.accept(RefreshTokenResult.SUCCESS_GENERATED);
                            }

                            @Override
                            public void onFailure(@Nonnull ApolloException ex) {
                              ex.printStackTrace();
                              if (errorCount < 1) refreshAccessToken(consumer, errorCount + 1);
                              else consumer.accept(RefreshTokenResult.FAILED_COMPLETION_REQUEST);
                            }
                          });
                } catch (IOException ex) {
                  ex.printStackTrace();
                  if (errorCount < 1) refreshAccessToken(consumer, errorCount + 1);
                  else consumer.accept(RefreshTokenResult.FAILED_MOJANG_REQUEST);
                }
              }

              @Override
              public void onFailure(@Nonnull ApolloException ex) {
                ex.printStackTrace();
                if (errorCount < 1) refreshAccessToken(consumer, errorCount + 1);
                else consumer.accept(RefreshTokenResult.FAILED_INIT_REQUEST);
              }
            });
  }

  private static class JoinInput {

    public String accessToken;
    public String selectedProfile;
    public String serverId;

    public JoinInput(String accessToken, String selectedProfile, String serverId) {
      this.accessToken = accessToken;
      this.selectedProfile = selectedProfile;
      this.serverId = serverId;
    }

    @Override
    public String toString() {
      return new Gson().toJson(this);
    }
  }
}

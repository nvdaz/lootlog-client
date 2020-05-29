package com.nvda.lootlog;

import com.apollographql.apollo.ApolloCall.Callback;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.nvda.lootlog.UpdateChecker.UpdateCheckResult.UpdateStatus;
import com.nvda.lootlog.api.SetVersionMutation;
import com.nvda.lootlog.api.SetVersionMutation.Data;
import com.nvda.lootlog.api.SetVersionMutation.SetVersion;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public class UpdateChecker {

  private static final ApolloProvider apolloProvider = ApolloProvider.getInstance();

  public static void getUpdates(Consumer<UpdateCheckResult> consumer) {
    SetVersionMutation mutation = SetVersionMutation.builder().version(LootLog.VERSION).build();
    apolloProvider
        .getClient()
        .mutate(mutation)
        .enqueue(
            new Callback<Data>() {
              @Override
              public void onResponse(@Nonnull Response<Data> response) {
                if (response.data() == null) consumer.accept(UpdateCheckResult.ERRED_CHECK_RESULT);
                SetVersion data = response.data().setVersion();
                consumer.accept(
                    new UpdateCheckResult(
                        data.isCurrent() ? UpdateStatus.CURRENT : UpdateStatus.OUTDATED,
                        data.changelog()));
              }

              @Override
              public void onFailure(@Nonnull ApolloException ex) {
                ex.printStackTrace();
                consumer.accept(UpdateCheckResult.ERRED_CHECK_RESULT);
              }
            });
  }

  public static class UpdateCheckResult {

    private static final UpdateCheckResult ERRED_CHECK_RESULT = new UpdateCheckResult(UpdateStatus.ERRED, null);
    
    public enum UpdateStatus {
      CURRENT,
      OUTDATED,
      ERRED
    }

    public final UpdateStatus updateStatus;
    public final String changelog;

    UpdateCheckResult(UpdateStatus updateStatus, String changelog) {
      this.updateStatus = updateStatus;
      this.changelog = changelog;
    }
  }
}

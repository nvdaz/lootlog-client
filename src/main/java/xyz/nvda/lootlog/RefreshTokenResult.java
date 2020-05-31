package xyz.nvda.lootlog;

public enum RefreshTokenResult {
  SUCCESS_CONFIG,
  SUCCESS_GENERATED,
  FAILED_COMPLETION_REQUEST,
  FAILED_MOJANG_REQUEST,
  FAILED_MOJANG_AUTH,
  FAILED_INIT_REQUEST;

  public String getDescription() {
    switch (this) {
      case SUCCESS_CONFIG:
        return "Loaded authorization from config!";
      case SUCCESS_GENERATED:
        return "Generated authorization token!";
      case FAILED_COMPLETION_REQUEST:
        return "Server Error? Failed to finalize login";
      case FAILED_MOJANG_REQUEST:
        return "Failed Mojang request. Try restarting your game and reloading your session.";
      case FAILED_MOJANG_AUTH:
        return "Failed Mojang authentication. Try restarting your game and reloading your session.";
      case FAILED_INIT_REQUEST:
        return "Server Error? Failed to initialize authentication challenge.";
      default:
        return "Something funky occurred.";
    }
  }

  public boolean isSuccess() {
    return this == SUCCESS_CONFIG || this == SUCCESS_GENERATED;
  }
}

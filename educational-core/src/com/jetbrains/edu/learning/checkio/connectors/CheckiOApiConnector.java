package com.jetbrains.edu.learning.checkio.connectors;

import com.jetbrains.edu.learning.checkio.api.CheckiOApiService;
import com.jetbrains.edu.learning.checkio.api.CheckiOResponse;
import com.jetbrains.edu.learning.checkio.api.exceptions.ApiException;
import com.jetbrains.edu.learning.checkio.courseFormat.CheckiOMission;
import com.jetbrains.edu.learning.checkio.exceptions.CheckiOLoginRequiredException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class CheckiOApiConnector {
  private final CheckiOApiService myCheckiOApiService;
  private final CheckiOOAuthConnector myOauthConnector;

  protected CheckiOApiConnector(@NotNull CheckiOApiService checkiOApiService, @NotNull CheckiOOAuthConnector oauthConnector) {
    myCheckiOApiService = checkiOApiService;
    myOauthConnector = oauthConnector;
  }

  @NotNull
  public CheckiOOAuthConnector getOauthConnector() {
    return myOauthConnector;
  }

  @NotNull
  public List<CheckiOMission> getMissionList() throws CheckiOLoginRequiredException, ApiException {
    final String accessToken = myOauthConnector.getAccessToken();
    final CheckiOResponse<List<CheckiOMission>> missionListResponse = myCheckiOApiService.getMissionList(accessToken);
    return missionListResponse.get();
  }
}

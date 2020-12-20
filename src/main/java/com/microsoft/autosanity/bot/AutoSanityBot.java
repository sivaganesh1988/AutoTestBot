// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.autosanity.bot;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.schema.ActionTypes;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.CardAction;
import com.microsoft.bot.schema.CardImage;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.HeroCard;
import com.microsoft.bot.schema.ResourceResponse;
import okhttp3.*;
import okio.BufferedSink;
import okio.Okio;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This class implements the functionality of the Bot.
 *
 * <p>
 * This is where application specific logic for interacting with the users would
 * be added. This class tracks the conversation state through a POJO saved in
 * {@link UserState} and demonstrates welcome messages and state.
 * </p>
 *
 * @see AutoSanityBotUserState
 */
@Component
public class AutoSanityBot extends ActivityHandler {
    // Messages sent to the user.
    private static final String WELCOMEMESSAGE =
        "Welcome to autoSanity Bot. For the current demo we have a sample API preconfigured" +
                "Send Show me a demo to read the preconfigued yaml and provide the test results";
    final String INFOMESSAGE =
        "You are seeing this message because the bot received at least one "
            + "'ConversationUpdate' event, indicating you (and possibly others) "
            + "joined the conversation. If you are using the emulator, pressing "
            + "the 'Start Over' button to trigger this event again. The specifics "
            + "of the 'ConversationUpdate' event depends on the channel. You can "
            + "read more information at: " + "https://aka.ms/about-botframework-welcome-user";

    private String LOCALEMESSAGE =
        "You can use the activity's GetLocale() method to welcome the user "
            + "using the locale received from the channel. "
            + "If you are using the Emulator, you can set this value in Settings.";

    private static final String PATTERNMESSAGE =
        "It is a good pattern to use this event to send general greeting"
            + "to user, explaining what your bot can do. In this example, the bot "
            + "handles 'hello', 'hi', 'help' and 'intro'. Try it now, type 'hi'";

    private static final String FIRST_WELCOME_ONE =
        "You are seeing this message because this was your first message ever to this bot.";

    private static final String FIRST_WELCOME_TWO =
        "It is a good practice to welcome the user and provide personal greeting. For example: Welcome %s";

    private UserState userState;

    @Autowired
    public AutoSanityBot(UserState withUserState) {
        userState = withUserState;
    }

    /**
     * Normal onTurn processing, with saving of state after each turn.
     *
     * @param turnContext The context object for this turn. Provides information
     *                    about the incoming activity, and other data needed to
     *                    process the activity.
     * @return A future task.
     */
    @Override
    public CompletableFuture<Void> onTurn(TurnContext turnContext) {
        return super.onTurn(turnContext)
            .thenCompose(saveResult -> userState.saveChanges(turnContext));
    }

    /**
     * Greet when users are added to the conversation.
     *
     * <p>Note that all channels do not send the conversation update activity.
     * If you find that this bot works in the emulator, but does not in
     * another channel the reason is most likely that the channel does not
     * send this activity.</p>
     *
     * @param membersAdded A list of all the members added to the conversation, as
     *                     described by the conversation update activity.
     * @param turnContext  The context object for this turn.
     * @return A future task.
     */
  /*  @Override
    protected CompletableFuture<Void> onMembersAdded(
        List<ChannelAccount> membersAdded,
        TurnContext turnContext
    ) {
        return membersAdded.stream()
            .filter(
                member -> !StringUtils
                    .equals(member.getId(), turnContext.getActivity().getRecipient().getId())
            )
            .map(
                channel -> turnContext
                    .sendActivities(
                        MessageFactory.text(
                            "Hi there - " + channel.getName() + ". " + WELCOMEMESSAGE
                        ),
                        MessageFactory.text(
                            LOCALEMESSAGE
                            + " Current locale is " + turnContext.getActivity().getLocale()),
                        MessageFactory.text(INFOMESSAGE),
                        MessageFactory.text(PATTERNMESSAGE)
                    )
            )
            .collect(CompletableFutures.toFutureList())
            .thenApply(resourceResponses -> null);
    }
*/
    /**
     * This will prompt for a user name, after which it will send info about the
     * conversation. After sending information, the cycle restarts.
     *
     * @param turnContext The context object for this turn.
     * @return A future task.
     */
    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        // Get state data from UserState.
        StatePropertyAccessor<AutoSanityBotUserState> stateAccessor =
            userState.createProperty("WelcomeUserState");
        CompletableFuture<AutoSanityBotUserState> stateFuture =
            stateAccessor.get(turnContext, AutoSanityBotUserState::new);

        return stateFuture.thenApply(thisUserState -> {

                String[] text = turnContext.getActivity().getText().toLowerCase().split(" ");
                switch (text[0]) {
                    case "hello":
                        return turnContext.sendActivities(MessageFactory.text(WELCOMEMESSAGE));
                    case "hi":
                        return turnContext.sendActivities(MessageFactory.text(WELCOMEMESSAGE));
                    case "help":
                        return sendIntroCard(turnContext);
                    case "yaml":
                        try {
                            return turnContext.sendActivities(MessageFactory.text(generateYaml(text[1])));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    default:
                        return turnContext.sendActivity("Default");
                }

        })
            // make the return value happy.
            .thenApply(resourceResponse -> null);
    }

    private CompletableFuture<ResourceResponse> sendIntroCard(TurnContext turnContext) {
        HeroCard card = new HeroCard() {{
            setTitle("Welcome to AutoSanityBot");
            setText(
                "Welcome to a demo of autoSanityBot! or the current demo we have a sample API preconfigured\" +\n" +
                        "                \"Send Show me a demo to read the preconfigued yaml and provide the test results\""
            );
        }};

      /*  card.setImages(Collections.singletonList(new CardImage() {
            {
                setUrl("https://aka.ms/bf-welcome-card-image");
            }
        }));

        card.setButtons(Arrays.asList(
            new CardAction() {{
                setType(ActionTypes.OPEN_URL);
                setTitle("Get an overview");
                setText("Get an overview");
                setDisplayText("Get an overview");
                setValue(
                    "https://docs.microsoft.com/en-us/azure/bot-service/?view=azure-bot-service-4.0"
                );
            }},
            new CardAction() {{
                setType(ActionTypes.OPEN_URL);
                setTitle("Ask a question");
                setText("Ask a question");
                setDisplayText("Ask a question");
                setValue("https://stackoverflow.com/questions/tagged/botframework");
            }},
            new CardAction() {{
                setType(ActionTypes.OPEN_URL);
                setTitle("Learn how to deploy");
                setText("Learn how to deploy");
                setDisplayText("Learn how to deploy");
                setValue(
                    "https://docs.microsoft.com/en-us/azure/bot-service/bot-builder-howto-deploy-azure?view=azure-bot-service-4.0"
                );
            }})
        );*/

        Activity response = MessageFactory.attachment(card.toAttachment());
        return turnContext.sendActivity(response);
    }

    public String generateYaml(String path) throws IOException {

        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        String body = "{\"openAPIUrl\": \"URI\"}";
        body = body.replace("URI",path);

        RequestBody jsonBody = RequestBody.create(
                MediaType.parse("application/json"), body);

        Request request = new Request.Builder()
                .url("http://localhost:8888/api/gen/clients/java")
                .post(jsonBody)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
       JsonElement element = gson.fromJson(response.body().string(),JsonElement.class);
        JsonObject jsonObj = element.getAsJsonObject();
        String link = jsonObj.get("link").getAsString();

        Request downloadRequest = new Request.Builder().url(link).get().build();

        Response response1 = client.newCall(downloadRequest).execute();
        File file = new File("C:\\Users\\intel\\Downloads\\yaml" + "Generated.zip");
        final BufferedSink sink = Okio.buffer(Okio.sink(file));
        sink.writeAll(response1.body().source());
        sink.close();

        return "Code is generated for the given yaml and can be accessed at " + file.getAbsolutePath();
    }
}

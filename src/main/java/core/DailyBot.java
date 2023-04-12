package core;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendVoice;
import io.github.cdimascio.dotenv.Dotenv;
import org.joda.time.DateTime;

import static core.IgnoredWords.listOfIgnoredWords;
import static core.Keypads.keypadButtonsProcessing;

public class DailyBot {
    protected static TelegramBot bot;
    private Timer timer;
    private int hour;
    private int minute;
    protected final static String START = "/start";
    protected final static String HELP = "/help";
    protected final static String INFO = "/info";
    protected final static String RESET_TIME = "/reset_time";
    protected final static String STOP_SEND = "/stop_send";
    protected final static String SET_LANG = "/set_lang";
    protected final static String LEZGI_RUS = "/lezgi_rus";
    protected final static String RUS_LEZGI = "/rus_lezgi";
    protected final static String DICT_OFF = "/dict_off";
    protected final static String RUS_INTERFACE = "/rus_interface";
    protected final static String LEZGI_INTERFACE = "/lezgi_interface";
    protected final static String SELECT_LEZGI = "/select_lezgi";
    protected final static String SELECT_RUS = "/select_rus";
    protected static Map<Long, String> dictionaryLanguage = new HashMap<>();
    protected static Map<String, String> clickButtonsLezgi = new HashMap<>();
    protected static Map<String, String> clickButtonsRus = new HashMap<>();
    protected static Map<Long, Integer> timeZoneFixation = new HashMap<>();
    protected static Map<Long, String> interfaceLanguage = new HashMap<>();

    record Time(int hour, int minute) {
    }

    protected static Map<Long, Time> timeToSendMessages = new HashMap<>();

    protected static ParsedDictionary rusLezgiDictionary = new ParsedDictionary();
    protected static ParsedDictionary lezgiRusDictionary = new ParsedDictionary();

    public DailyBot(String apiKey) throws IOException {
        bot = new TelegramBot(apiKey);
        timer = new Timer();
        rusLezgiDictionary.parse("src/main/resources/rus_lezgi_dict_hajiyev.json");
        lezgiRusDictionary.parse("src/main/resources/lezgi_rus_dict_babakhanov.json");
    }

    public void start() {
        bot.setUpdatesListener(updates -> {
            try {
                for (var update : updates) {
                    /* Обработка нажатия на buttons */
                    if (update.callbackQuery() != null) {
                        CallbackQuery callbackQuery = update.callbackQuery();
                        Message message1 = callbackQuery.message();
                        long chatId = message1.chat().id();
                        String data = callbackQuery.data();
                        InlineButtons.inlineButtonsProcessing(chatId, data, callbackQuery);
                    }
                    var message = update.message(); // Получаем сообщение
                    if (message == null) {
                        continue;
                    }
                    long chatId = message.chat().id();
                    //    DateTime now1 = DateTime.now();
                    //    System.out.println(now1.getHourOfDay() + ":" + now1.getMinuteOfHour());
                    if (update.message() != null && update.message().text() != null) {

                        /*  Обработка Keypad */
                        Keypads.keypadButtonsProcessing(chatId, update.message().text());

                        String userMessage = update.message().text().toLowerCase();
                        if (dictionaryLanguage.get(chatId) != null) {
                            userMessage = userMessage.replaceAll("1", "i");
                        }
                        /* Старт, выбор языка интерфейса */
                        if (START.equals(userMessage)) {
                            InlineKeyboardMarkup languageSelectionButtons = new InlineKeyboardMarkup(
                                    new InlineKeyboardButton[][]{
                                            {new InlineKeyboardButton("Лезги чIал").callbackData(LEZGI_INTERFACE),
                                                    new InlineKeyboardButton("Русский язык").callbackData(RUS_INTERFACE)}
                                    });
                            bot.execute(new SendMessage(chatId, "ЧIал хкягъа \uD83C\uDF10 Выберите язык")
                                    .replyMarkup(languageSelectionButtons));
                            bot.execute(new SendMessage(chatId, "\uD83D\uDCA1Примечание\n\n"
                                    + "Весь интерфейс будет на лезгинском языке, если выбрать «Лезги чIал». Это больше "
                                    + "подойдет для тех, кто лезгинский уже знает и хочет подтянуть. Будет хорошая "
                                    + "возможность погрузиться в язык полностью.\nНо сложно будет тем, кто лезгинский "
                                    + "совсем не знает. Конечно, всё на ваше усмотрение, вам виднее. Желаю успехов!"));
                        }
                        if (RUS_LEZGI.equals(dictionaryLanguage.get(chatId))
                                && (rusLezgiDictionary.map.containsKey(userMessage))) {
                            sendAnswerFromDictionary(bot, rusLezgiDictionary, chatId, userMessage);
                        } else if (LEZGI_RUS.equals(dictionaryLanguage.get(chatId))
                                && (lezgiRusDictionary.map.containsKey(userMessage))) {
                            sendAnswerFromDictionary(bot, lezgiRusDictionary, chatId, userMessage);
                        } else if (LEZGI_RUS.equals(dictionaryLanguage.get(chatId))) {
                            sendAnswerToUserWhenIncorrectInput(bot, lezgiRusDictionary, clickButtonsLezgi, chatId, userMessage, LEZGI_RUS);
                        } else if (RUS_LEZGI.equals(dictionaryLanguage.get(chatId))) {
                            sendAnswerToUserWhenIncorrectInput(bot, rusLezgiDictionary, clickButtonsRus, chatId, userMessage, RUS_LEZGI);
                        }
                        /* Фиксация часового пояса  */
                        else if (timeZoneFixation.get(chatId) == null
                                && (userMessage.matches("^0?[0-9]|1[0-9]|2[0-3]$"))) {
                            timeZoneFixation.put(chatId, Integer.parseInt(userMessage));
                            if (interfaceLanguage.get(chatId).equals(RUS_INTERFACE)) {
                                bot.execute(new SendMessage(chatId, "Часовой пояс зафиксирован.\n\n"
                                        + "А теперь введите время, во сколько вы хотите получать задания (в формате часы:минуты, "
                                        + "например: 21:10):"));
                            }else if (interfaceLanguage.get(chatId).equals(LEZGI_INTERFACE)) {
                                bot.execute(new SendMessage(chatId, "Куьн часовой пояс чир хьана. Кхьихьа "
                                        + "куьн исятда шумуд сят я. МИНУТАР КХЬИМИР, анжах сят кхьихь (месела, эгер "
                                        + "исятда куьн чIав - 14:33 ятIа, ракъура 14, эгер 09:19 - ракъура 09 ...)"));
                            }
                        }
                        /* Фиксация времени */
                        else if (userMessage.matches("^\\d{1,2}:\\d{2}$")
                                && timeToSendMessages.get(chatId) == null) {
                            String[] parts = userMessage.split(":");
                            hour = Integer.parseInt(parts[0]);
                            minute = Integer.parseInt(parts[1]);
                            DateTime now = DateTime.now();
                            if (timeZoneFixation.get(chatId) < now.getHourOfDay()) {
                                hour += Math.abs(now.getHourOfDay() - timeZoneFixation.get(chatId));
                            } else if (timeZoneFixation.get(chatId) > now.getHourOfDay()) {
                                hour -= Math.abs(now.getHourOfDay() - timeZoneFixation.get(chatId));
                            }
                            timeToSendMessages.put(chatId, new Time(hour, minute));
                            DateTime scheduledTime = now.withTime(timeToSendMessages.get(chatId).hour,
                                    timeToSendMessages.get(chatId).minute, 0, 0);
                            if (scheduledTime.isBeforeNow()) {
                                scheduledTime = scheduledTime.plusDays(1);
                            }
                            if (interfaceLanguage.get(chatId).equals(RUS_INTERFACE)) {
                                bot.execute(new SendMessage(chatId, "Отлично. Каждый день, в " + userMessage
                                        + " бот будет присылать вам задания."));
                            } else if (interfaceLanguage.get(chatId).equals(LEZGI_INTERFACE)) {
                                bot.execute(new SendMessage(chatId, "Хъсан я. Гьар юкъуз, сятдин " + userMessage
                                        + " ботди квез тапшуругъар ракъурда. Агалкьунар хьуй!"));
                            }

                            timer.scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run() {
                                    DateTime now = DateTime.now();
                                    if (now.getHourOfDay() == timeToSendMessages.get(chatId).hour
                                            && now.getMinuteOfHour() == timeToSendMessages.get(chatId).minute) {
                                        sendDailyMessage(message);
                                    }
                                }
                            }, scheduledTime.toDate(), 24 * 60 * 60 * 1000);
                        }
                    }
                }
                ;
            } catch (Exception e) {
                System.out.println(e);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void sendDailyMessage(Message message) {
        bot.execute(new SendMessage(message.chat().id(), "Сагърай лезгияр"));
    }

    public static void main(String[] args) throws IOException {
        Dotenv dotenv = null;
        dotenv = Dotenv.configure().load();
        var token = dotenv.get("TELEGRAM_API_TOKEN");
        if (token == null) {
            System.err.println("Telegram token not found");
            return;
        }
        DailyBot bot = new DailyBot(token);
        bot.start();
    }

    private static void sendAnswerFromDictionary(TelegramBot bot, ParsedDictionary dictionary,
                                                 long chatId, String userMessage) {
        List<String> translations = dictionary.map.get(userMessage);
        String msgStr = convertToHtml(translations);
        var sendMessage = new SendMessage(chatId, msgStr);
        sendMessage.parseMode(ParseMode.HTML);
        bot.execute(sendMessage);
    }

    public static void sendAnswerFromButtons(TelegramBot bot, ParsedDictionary dictionary,
                                              CallbackQuery callbackQuery, long chatId, String data) {
        List<String> translations = dictionary.map.get(data.toLowerCase());
        String msgStr = convertToHtml(translations);
        var sendMessage = new SendMessage(chatId, msgStr);
        sendMessage.parseMode(ParseMode.HTML);
        bot.execute(new SendMessage(chatId, data + "\n\n"));
        bot.execute(sendMessage);
        bot.execute(new AnswerCallbackQuery(callbackQuery.id()));
    }

    public static void sendAnswerToUserWhenIncorrectInput(TelegramBot bot, ParsedDictionary dictionary, Map<String, String> clickButtons,
                                                           long chatId, String userMessage, String language) {
        if (listOfIgnoredWords.contains(userMessage)) {
            return;
        }
        record WordSim(String word, Double sim) {
        }
        List<WordSim> temp = new ArrayList<>();
        for (String word : dictionary.map.keySet()) {
            double sim = StringSimilarity.similarity(word, userMessage.toUpperCase());
            if (sim >= 0.5) {
                temp.add(new WordSim(word.toLowerCase().replaceAll("i", "I"), sim));
            }
        }
        temp.sort(Comparator.comparing(WordSim::sim).reversed());
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        for (var wordSim : temp.subList(0, Math.min(7, temp.size()))) {
            List<InlineKeyboardButton> tempList = new ArrayList<>();
            tempList.add(new InlineKeyboardButton(wordSim.word).callbackData(wordSim.word));
            buttons.add(tempList);
            clickButtons.put(wordSim.word, language);
        }
        InlineKeyboardButton[][] inlineKeyboardButton = new InlineKeyboardButton[buttons.size()][1];
        for (int i = 0; i < inlineKeyboardButton.length; i++) {
            for (int j = 0; j < inlineKeyboardButton[i].length; j++) {
                inlineKeyboardButton[i][j] = buttons.get(i).get(j);
            }
        }
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(inlineKeyboardButton);
        if (clickButtons.size() > 0 && temp.size() > 0) {
            if (interfaceLanguage.get(chatId).equals(RUS_INTERFACE)) {
                bot.execute(new SendMessage(chatId, "В словаре ничего не нашлось\uD83E\uDD14, возможно вы имели ввиду:\n")
                        .replyMarkup(inlineKeyboard));
            } else if (interfaceLanguage.get(chatId).equals(LEZGI_INTERFACE)) {
                bot.execute(new SendMessage(chatId, "Гафарганда жагъай гаф авач\uD83E\uDD14, килиг и гафариз\n")
                        .replyMarkup(inlineKeyboard));
            }
        } else {
            if (interfaceLanguage.get(chatId).equals(RUS_INTERFACE)) {
                bot.execute(new SendMessage(chatId, "В словаре ничего не нашлось\uD83E\uDD72. Повторите запрос"));
            } else if (interfaceLanguage.get(chatId).equals(LEZGI_INTERFACE)) {
                bot.execute(new SendMessage(chatId, "Гафарганда жагъай гаф авач\uD83E\uDD72. МасакI кхьихь"));
            }
        }
    }

    private static String convertToHtml(List<String> translations) {
        String msgStr = String.join("\n\n", translations).replaceAll(">", ">>>")
                .replaceAll("<", "<i>")
                .replaceAll(">>>", "</i>")
                .replaceAll("\\{", "<b>")
                .replaceAll("}", "</b>");
        return msgStr;
    }
}
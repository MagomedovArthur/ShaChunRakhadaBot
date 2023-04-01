package core;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendVoice;
import org.joda.time.DateTime;

public class DailyBot {
    private TelegramBot bot;
    private Timer timer;
    private int hour;
    private int minute;
    private final static String START = "/start";
    private final static String HELP = "/help";
    private final static String INFO = "/info";
    private final static String RESET_TIME = "/reset_time";
    private final static String STOP_SEND = "/stop_send";
    private final static String LEZGI_RUS = "/lezgi_rus";
    private final static String RUS_LEZGI = "/rus_lezgi";
    private final static String DICT_OFF = "/dict_off";
    private Map<Long, String> chatLang = new HashMap<>();
    private Map<String, String> clickButtonsLezgi = new HashMap<>();
    private Map<String, String> clickButtonsRus = new HashMap<>();
    private Map<Long, Integer> timeZoneFixation = new HashMap<>();

    record Time(int hour, int minute) {
    }

    private Map<Long, Time> timeToSendMessages = new HashMap<>();

    private ParsedDictionary rusLezgiDictionary = new ParsedDictionary();
    private ParsedDictionary lezgiRusDictionary = new ParsedDictionary();

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

                        if ("/translation".equals(data)) {
                            // Send a response message
                            bot.execute(new SendMessage(chatId, "Я Рамиза знаю с детства (или: «знаю с маленьких лет»), мы в школе учились в одном классе."));
                            bot.execute(new AnswerCallbackQuery(callbackQuery.id()));
                            //chatLang.put(chatId, "/rus_lezgi");
                        } else if (clickButtonsLezgi.containsKey(data) && "/lezgi_rus".equals(clickButtonsLezgi.get(data))) {
                            sendAnswerFromButtons(bot, lezgiRusDictionary, callbackQuery, chatId, data);
                        } else if (clickButtonsRus.containsKey(data) && "/rus_lezgi".equals(clickButtonsRus.get(data))) {
                            sendAnswerFromButtons(bot, rusLezgiDictionary, callbackQuery, chatId, data);
                        }
                    }
                    var message = update.message(); // Получаем сообщение

                    if (message == null) {
                        continue;
                    }
                    long chatId = message.chat().id();

                    //    DateTime now1 = DateTime.now();
                    //    System.out.println(now1.getHourOfDay() + ":" + now1.getMinuteOfHour());

                    if (update.message() != null && update.message().text() != null) {
                        String userMessage = update.message().text().toLowerCase();
//                    if (timeZoneFixation.get(chatId) == null || timeToSendMessages.get(chatId) == null) {
//                        continue;
//                    }
                        if (START.equals(userMessage)) {
                            String text = "Что делает этот бот?\n\n"
                                    + "Бот будет каждый день, в назначенное Вами время⏰, присылать текстовые фразы[?] на "
                                    + "Лезгинском языке и их озвучкой. Вашей задачей будет "
                                    + "прочитать фразы, прослушать их и перевести. После перевода нажимаете на кнопку "
                                    + "«Получить перевод» и сравниваете свой результат перевода\uD83D\uDD75\uD83C\uDFFB\n\n"
                                    + "Забыли какое-то слово? Тут доступен словарь\uD83D\uDCD7. "
                                    + "Нажмите на кнопку меню слева, выберите словарь и просто в чат введите слово!\n\n";
                            bot.execute(new SendMessage(chatId, "Ас-саляму алейкум\uD83D\uDC4B\uD83C\uDFFC\n\n"
                                    + "Вун атуй, рагъ атуй!\nДобро пожаловать!"));
                            bot.execute(new SendMessage(chatId, text));
                            bot.execute(new SendMessage(chatId, "Давайте начнем. Для начала боту необходимо понять, "
                                    + "какой у вас часовой пояс. Пожалуйста, введите который у вас час.\n\n Введите только час, "
                                    + "МИНУТЫ НЕ НУЖНЫ (Например, если ваше время сейчас - 14:33, отправляете 14, 09:19 - 09 и т.д.)"));

                        }
                        /* Словари */
                        else if (LEZGI_RUS.equals(userMessage)) {
                            chatLang.put(chatId, LEZGI_RUS);
                            bot.execute(new SendMessage(chatId, "Выбран Лезгинско-Русский словарь.\n\n" +
                                    "Введите слово на лезгинском языке.\n" +
                                    "Символ «I» (палочка: кI, тI, пI...) вводите через латинкую букву «i»."));

                        } else if (RUS_LEZGI.equals(userMessage)) {
                            chatLang.put(chatId, RUS_LEZGI);
                            bot.execute(new SendMessage(chatId, "Выбран Русско-Лезгинский словарь.\n " +
                                    "\nВведите слово на русском языке."));
                        }

                        if ("/rus_lezgi".equals(chatLang.get(chatId))
                                && (rusLezgiDictionary.map.containsKey(userMessage))) {
                            sendAnswerFromDictionary(bot, rusLezgiDictionary, chatId, userMessage);
                        } else if ("/lezgi_rus".equals(chatLang.get(chatId))
                                && (lezgiRusDictionary.map.containsKey(userMessage))) {
                            sendAnswerFromDictionary(bot, lezgiRusDictionary, chatId, userMessage);
                        } else if ("/lezgi_rus".equals(chatLang.get(chatId))
                                && !("/start".equals(userMessage.toLowerCase()))
                                && !("/lezgi_rus".equals(userMessage.toLowerCase()))
                                && !("/rus_lezgi".equals(userMessage.toLowerCase()))) {
                            sendAnswerToUser(bot, lezgiRusDictionary, clickButtonsLezgi, chatId, userMessage, "/lezgi_rus");
                        } else if ("/rus_lezgi".equals(chatLang.get(chatId))
                                && !("/start".equals(userMessage.toLowerCase()))
                                && !("/lezgi_rus".equals(userMessage.toLowerCase()))
                                && !("/rus_lezgi".equals(userMessage.toLowerCase()))) {
                            sendAnswerToUser(bot, rusLezgiDictionary, clickButtonsRus, chatId, userMessage, "/rus_lezgi");
                        }

                        /* Сброс времени отправки */

                        else if (RESET_TIME.equals(userMessage)) {
                            timeZoneFixation.remove(chatId);
                            timeToSendMessages.remove(chatId);
                            bot.execute(new SendMessage(chatId, "Вы сбросили время, чтобы задать другое время отправки" +
                                    "фраз. Введите ваше время сейчас:"));
                        }

                        /* Остановка отправки фраз */
                        else if (STOP_SEND.equals(userMessage)) {
                            timeZoneFixation.remove(chatId);
                            timeToSendMessages.remove(chatId);
                            bot.execute(new SendMessage(chatId, "Бот остановлен"));
                        }

                        /* Фиксация часового пояса  */
                        else if (timeZoneFixation.get(chatId) == null
                                //        && (userMessage.matches("^[0-9]|1[0-9]|2[0-3]$"))) {
                                && (userMessage.matches("^0?[0-9]|1[0-9]|2[0-3]$"))) {
                            timeZoneFixation.put(chatId, Integer.parseInt(userMessage));
                            bot.execute(new SendMessage(chatId, "Часовой пояс зафиксирован.\n\n"
                                    + "А теперь введите время, во сколько вы хотите получать фразы (в формате часы:минуты, "
                                    + "например: 21:10, 13:47, 09:05 и т.д.)"));
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
                            bot.execute(new SendMessage(chatId, "Отлично. Каждый день, в " + userMessage
                                    + " бот будет присылать Вам фразы."));
                            bot.execute(new SendMessage(chatId, "А пока вот ваша первая фраза для ознакомления"
                                    + "(не забывайте про словарь, который включается через меню слева\uD83D\uDCD7):"));

                            /* Sending voice */
                            bot.execute(new SendMessage(chatId, "«Рамиз заз бицIи чIавалай чизвайди я, чун мектебда са классда авайди тир»"));
                            File file = new File("src/main/resources/audio_1.ogg");
                            SendVoice request = new SendVoice(chatId, file);
                            bot.execute(request);

                            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(
                                    new InlineKeyboardButton[][]{
                                            {new InlineKeyboardButton("Получить перевод").callbackData("/translation")}
                                    });
                            bot.execute(new SendMessage(chatId, "Нажмите чтобы получить перевод\uD83D\uDC47\uD83C\uDFFC")
                                    .replyMarkup(inlineKeyboard));

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
        DailyBot bot = new DailyBot("6111537155:AAH-bogcqA4bLX4bRl4q7UUohsnlSRE5YHw");
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

    private static void sendAnswerFromButtons(TelegramBot bot, ParsedDictionary dictionary,
                                              CallbackQuery callbackQuery, long chatId, String data) {
        List<String> translations = dictionary.map.get(data.toLowerCase());
        String msgStr = convertToHtml(translations);
        var sendMessage = new SendMessage(chatId, msgStr);
        sendMessage.parseMode(ParseMode.HTML);
        bot.execute(new SendMessage(chatId, data + "\n\n"));
        bot.execute(sendMessage);
        bot.execute(new AnswerCallbackQuery(callbackQuery.id()));
    }

    private static void sendAnswerToUser(TelegramBot bot, ParsedDictionary dictionary, Map<String, String> clickButtons,
                                         long chatId, String userMessage, String language) {
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
            bot.execute(new SendMessage(chatId, "Ничего не нашлось\uD83E\uDD14, возможно вы имели ввиду:\n")
                    .replyMarkup(inlineKeyboard));
        } else {
            bot.execute(new SendMessage(chatId, "Ничего не нашлось\uD83E\uDD72. Повторите запрос"));
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
package core;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendVoice;

import java.io.File;

import static core.DailyBot.*;

public class InlineButtons {

    public static void inlineButtonsProcessing(long chatId, String data, CallbackQuery callbackQuery) {

        if ("/translation".equals(data)) {
            // Send a response message
            bot.execute(new SendMessage(chatId, "Я Рамиза знаю с детства (или: «знаю с маленьких лет»), мы в школе учились в одном классе."));
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()));
            //chatLang.put(chatId, "/rus_lezgi");
        }
        /* Словари */
        else if (LEZGI_RUS.equals(data)) {
            if (interfaceLanguage.get(chatId).equals(RUS_INTERFACE)) {
                bot.execute(new SendMessage(chatId, "Выбран Лезгинско-русский словарь.\n\n"
                        + "Символ «I» (в буквах: кI, тI, пI...) вводите через единицу «1» или латинкую "
                        + "букву «i»."));
            } else if (interfaceLanguage.get(chatId).equals(LEZGI_INTERFACE)) {
                bot.execute(new SendMessage(chatId, "Лезги-урус гафарган.\n\n"
                        + "«I»-лишан галай гьарфар (кI, тI, пI...) латинрин «i» ва я «1» галаз кхьихь."));
            }
            dictionaryLanguage.put(chatId, LEZGI_RUS);
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()));
        } else if (RUS_LEZGI.equals(data)) {
            if (interfaceLanguage.get(chatId).equals(LEZGI_INTERFACE)) {
                bot.execute(new SendMessage(chatId, "Урус-лезги гафарган."));
            } else if (interfaceLanguage.get(chatId).equals(RUS_INTERFACE)) {
                bot.execute(new SendMessage(chatId, "Выбран Русско-лезгинский словарь.\n\n"));
            }
            dictionaryLanguage.put(chatId, RUS_LEZGI);
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()));
        }
        /* Сброс таймера */
        else if (RESET_TIME.equals(data)) {
            timeZoneFixation.remove(chatId);
            timeToSendMessages.remove(chatId);
            dictionaryLanguage.remove(chatId);
            if (interfaceLanguage.get(chatId).equals(RUS_INTERFACE)) {
                bot.execute(new SendMessage(chatId, "Таймер сброшен.\n\n Чтобы настроить его заново "
                        + "нужно узнать ваш часовой пояс. Введите который у вас сейчас час, без минут:"));
            } else if (interfaceLanguage.get(chatId).equals(LEZGI_INTERFACE)) {
                bot.execute(new SendMessage(chatId, "Куьне бот акъвазарна маса сят эцигдайвал. Кхьихьа, куьн " +
                        "сят гьим ятIа исятда:"));
            }
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()));
        }
        /* Остановка таймера */
        else if (STOP_SEND.equals(data)) {
            timeZoneFixation.remove(chatId);
            timeToSendMessages.remove(chatId);
            if (interfaceLanguage.get(chatId).equals(RUS_INTERFACE)) {
                bot.execute(new SendMessage(chatId, "Таймер остановлен."));
            } else if (interfaceLanguage.get(chatId).equals(LEZGI_INTERFACE)) {
                bot.execute(new SendMessage(chatId, "Бот акъвазарна"));
            }
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()));
        }
        /* Сменить язык интерфейса */
        else if (SELECT_LEZGI.equals(data)) {
            ReplyKeyboardMarkup lezgiKeypad = createLezgiKeypad();
            lezgiKeypad.resizeKeyboard(true);
            SendMessage message = new SendMessage(chatId, "Куьне лезги чIалан интерфейс хкягъна");
            message.replyMarkup(lezgiKeypad);
            bot.execute(message);
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()));
            interfaceLanguage.put(chatId, LEZGI_INTERFACE);
        } else if (SELECT_RUS.equals(data)) {
            ReplyKeyboardMarkup rusKeypad = createRusKeypad();
            rusKeypad.resizeKeyboard(true);
            SendMessage message = new SendMessage(chatId, "Вы выбрали русский язык интерфейса");
            message.replyMarkup(rusKeypad);
            bot.execute(message);
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()));
            interfaceLanguage.put(chatId, RUS_INTERFACE);
        }
        else if (clickButtonsLezgi.containsKey(data) && LEZGI_RUS.equals(clickButtonsLezgi.get(data))) {
            sendAnswerFromButtons(bot, lezgiRusDictionary, callbackQuery, chatId, data);
        } else if (clickButtonsRus.containsKey(data) && RUS_LEZGI.equals(clickButtonsRus.get(data))) {
            sendAnswerFromButtons(bot, rusLezgiDictionary, callbackQuery, chatId, data);
        } else if (RUS_INTERFACE.equals(data)) {
            bot.execute(new SendMessage(chatId, "Ас-саляму алейкум\uD83D\uDC4B\uD83C\uDFFC\n\n"
                    + "Добро пожаловать!"
                    + "\n\nКаждый день, в назначенное Вами время⏳, бот будет "
                    + "присылать задания на лезгинском языке (+ их озвучка\uD83C\uDFA4). Ваша "
                    + "задача - прочитать, прослушать, перевести. После нажимаете кнопку «Получить "
                    + "перевод» и сравниваете свой перевод\uD83D\uDD0D.\n\n"
                    + "Вот вам первый пример, для понимания:\n\n"
                    + "«Рамиз заз бицIи чIавалай чизвайди я, чун мектебда са классда авайди тир»"));
            /* Sending voice */
            File file = new File("src/main/resources/audio_1.ogg");
            SendVoice request = new SendVoice(chatId, file);
            bot.execute(request);
            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(
                    new InlineKeyboardButton[][]{
                            {new InlineKeyboardButton("Получить перевод").callbackData("/translation")}
                    });
            bot.execute(new SendMessage(chatId, "После того, как перевели, нажимаете на кнопку:\uD83D\uDC47\uD83C\uDFFC")
                    .replyMarkup(inlineKeyboard));
            ReplyKeyboardMarkup rusKeypad = createRusKeypad();
            rusKeypad.resizeKeyboard(true);
            SendMessage message2 = new SendMessage(chatId, "Здесь также доступен словарь (кнопка «Выбрать "
                    + "словарь\uD83D\uDCDA»).\n\n"
                    + "Если будут проблемы с понимаем как работает бот, нажмите на «\uD83D\uDCDDИнструкция бота».\n\n"
                    + "Начнём. Сначала боту нужно опеределить ваш часовой пояс. Введите который у вас сейчас час, без "
                    + "минут (например, если ваше время сейчас - 14:33, отправляете 14)");
            message2.replyMarkup(rusKeypad);
            bot.execute(message2);
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()));
            interfaceLanguage.put(chatId, RUS_INTERFACE);
        } else if (LEZGI_INTERFACE.equals(data)) {
            bot.execute(new SendMessage(chatId, "Ас-саляму алейкум\uD83D\uDC4B\uD83C\uDFFC\n\n"
                    + "Вун атуй, рагъ атуй!\n\n"
                    + "Гьар юкъуз, куьне эцигай чIавуз⏳, ботди квез "
                    + "лезги чIалал тапшуругъар вегьеда (ва гьабурун ван\uD83C\uDFA4). Куьне вуч авуна "
                    + "кIанзава - атанвай тапшуругъ кIелна, яб гана, таржума авун. Ахпа, таржума авурдлай кьулухъ, "
                    + "«Таржума къачун» тIенкьедал илисна, гекъига ая\uD83D\uDD0D.\n\n"
                    + "Ингье квез месела, куьн гъавурда акьадайвал:\n\n"
                    + "«Рамиз заз бицIи чIавалай чизвайди я, чун мектебда са классда авайди тир»"));
            /* Sending voice */
            File file = new File("src/main/resources/audio_1.ogg");
            SendVoice request = new SendVoice(chatId, file);
            bot.execute(request);
            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(
                    new InlineKeyboardButton[][]{
                            {new InlineKeyboardButton("Таржума къачун").callbackData("/translation")}
                    });
            bot.execute(new SendMessage(chatId, "Илиса:\uD83D\uDC47\uD83C\uDFFC")
                    .replyMarkup(inlineKeyboard));
            ReplyKeyboardMarkup lezgiKeypad = createLezgiKeypad();
            lezgiKeypad.resizeKeyboard(true);
            SendMessage message2 = new SendMessage(chatId, "Гафарган герек атанвани?\uD83D\uDCD7Гьамани ина авазва. "
                    + "«Гафарган хкягъун\uD83D\uDCDA» тIенкьедал илиса.\n\n"
                    + "Бот гьикI кIвалахзаватIа гъавурда акьуначни?"
                    + "«Инструкция\uD83D\uDCDD» тIенкьедал илиса.\n\n"
                    + "Гила чна гатIумда. Сифтедай, ботдиз чир хьана кIанзава куь часовой пояс. Кхьихьа "
                    + "куьн исятда шумуд сят я. МИНУТАР КХЬИМИР, анжах сят кхьихь (месела, эгер "
                    + "исятда куьн чIав - 14:33 ятIа, ракъура 14, эгер 09:19 - ракъура 09 ...)");
            message2.replyMarkup(lezgiKeypad);
            bot.execute(message2);
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()));
            interfaceLanguage.put(chatId, LEZGI_INTERFACE);
        }
    }

    private static ReplyKeyboardMarkup createLezgiKeypad() {
        ReplyKeyboardMarkup lezgiKeypad = new ReplyKeyboardMarkup(
                new KeyboardButton[]{
                        new KeyboardButton("\uD83D\uDD20Гьарфар"),
                        new KeyboardButton("\uD83C\uDF93Экзаменар")
                },
                new KeyboardButton[]{
                        new KeyboardButton("\uD83D\uDCDAГафарган хкягъун"),
                        new KeyboardButton("⏳ЧIав туькIуьрун")
                },
                new KeyboardButton[]{
                        new KeyboardButton("\uD83D\uDCA4Гафарган хкудун"),
                        new KeyboardButton("\uD83C\uDF10ЧIал дегишрун")
                },
                new KeyboardButton[]{
                        new KeyboardButton("\uD83D\uDCDDБотдин къайдаяр"),
                        new KeyboardButton("✍\uD83C\uDFFCКъимет гун")
                }
        );
        return lezgiKeypad;
    }
    private static ReplyKeyboardMarkup createRusKeypad() {
        ReplyKeyboardMarkup rusKeypad = new ReplyKeyboardMarkup(
                new KeyboardButton[]{
                        new KeyboardButton("\uD83D\uDD20Алфавит"),
                        new KeyboardButton("\uD83C\uDF93Экзамены")
                },
                new KeyboardButton[]{
                        new KeyboardButton("\uD83D\uDCDAВыбрать словарь"),
                        new KeyboardButton("⏳Настройка таймера")
                },
                new KeyboardButton[]{
                        new KeyboardButton("\uD83D\uDCA4Выключить словари"),
                        new KeyboardButton("\uD83C\uDF10Сменить язык")
                },
                new KeyboardButton[]{
                        new KeyboardButton("\uD83D\uDCDDИнструкция бота"),
                        new KeyboardButton("✍\uD83C\uDFFCНаписать отзыв")
                }
        );
        return rusKeypad;
    }
}
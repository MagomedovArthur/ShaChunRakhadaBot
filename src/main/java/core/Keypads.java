package core;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;

import static core.DailyBot.*;

public class Keypads {
    public static void keypadButtonsProcessing(long chatId, String userMessage) {
        switch (userMessage) {
            /* russian keypad     */
            case "\uD83D\uDD20Алфавит":
                bot.execute(new SendMessage(chatId, "Видео с алфавитом"));
                break;
            case "\uD83C\uDF93Экзамены":
                bot.execute(new SendMessage(chatId, "Эта опция пока не доступна"));
                break;
            case "\uD83D\uDCDAВыбрать словарь":
                InlineKeyboardMarkup dictionaryButtons = new InlineKeyboardMarkup(
                        new InlineKeyboardButton[][]{
                                {new InlineKeyboardButton("\uD83D\uDCD7Лезгинско-Русский")
                                        .callbackData(LEZGI_RUS)},
                                {new InlineKeyboardButton("\uD83D\uDCD5Русско-Лезгинский")
                                        .callbackData(RUS_LEZGI)}
                        });
                bot.execute(new SendMessage(chatId, "Выберите словарь\uD83D\uDCDA:")
                        .replyMarkup(dictionaryButtons));
                break;
            case "⏳Настройка таймера":
                InlineKeyboardMarkup timerSettings = new InlineKeyboardMarkup(
                        new InlineKeyboardButton[][]{
                                {new InlineKeyboardButton("\uD83D\uDD57Изменить время отправки заданий")
                                        .callbackData(RESET_TIME)},
                                {new InlineKeyboardButton("\uD83D\uDE45\uD83C\uDFFB\u200D♂Остановить отправку "
                                        + "заданий по таймеру")
                                        .callbackData(STOP_SEND)}
                        });
                bot.execute(new SendMessage(chatId, "Выберите действие⌛️:")
                        .replyMarkup(timerSettings));
                break;
            case "\uD83D\uDCA4Выключить словари":
                dictionaryLanguage.remove(chatId);
                clickButtonsLezgi.remove(chatId);
                clickButtonsRus.remove(chatId);
                bot.execute(new SendMessage(chatId, "Словари выключены"));
                break;

            case "\uD83C\uDF10Сменить язык":
                InlineKeyboardMarkup selectLanguage = new InlineKeyboardMarkup(
                        new InlineKeyboardButton[][]{
                                {new InlineKeyboardButton("Лезги чIал").callbackData(SELECT_LEZGI),
                                        new InlineKeyboardButton("Русский язык").callbackData(SELECT_RUS)}
                        });
                bot.execute(new SendMessage(chatId, "ЧIал хкягъа \uD83C\uDF10 Выберите язык")
                        .replyMarkup(selectLanguage));
                break;
            case "\uD83D\uDCDDИнструкция бота":
                bot.execute(new SendMessage(chatId, "Тут будет инстуркция использования бота, потом напишу не ругайтееесь"));
                break;
            case "✍\uD83C\uDFFCНаписать отзыв":
                bot.execute(new SendMessage(chatId, "<ссылка на мой телеграм>"));
                break;

                /*    Lezgian keypad   */
            case "\uD83D\uDD20Гьарфар":
                bot.execute(new SendMessage(chatId, "Гьарфарин видео"));
                break;
            case "\uD83C\uDF93Экзаменар":
                bot.execute(new SendMessage(chatId, "Гьеле авач"));
                break;
            case "\uD83D\uDCDAГафарган хкягъун":
                InlineKeyboardMarkup dictionary = new InlineKeyboardMarkup(
                        new InlineKeyboardButton[][]{
                                {new InlineKeyboardButton("\uD83D\uDCD7Лезги-урус")
                                        .callbackData(LEZGI_RUS)},
                                {new InlineKeyboardButton("\uD83D\uDCD5Урус-лезги")
                                        .callbackData(RUS_LEZGI)}
                        });
                bot.execute(new SendMessage(chatId, "Гафарган хкягъа\uD83D\uDCDA:")
                        .replyMarkup(dictionary));
                break;
            case "⏳ЧIав туькIуьрун":
                InlineKeyboardMarkup timer = new InlineKeyboardMarkup(
                        new InlineKeyboardButton[][]{
                                {new InlineKeyboardButton("\uD83D\uDD57Тапшуругъар къвезвай чIав дегишрун")
                                        .callbackData(RESET_TIME)},
                                {new InlineKeyboardButton("\uD83D\uDE45\uD83C\uDFFB\u200D♂Бот акъвазарна")
                                        .callbackData(STOP_SEND)}
                        });
                bot.execute(new SendMessage(chatId, "Вуч авуна герек я?⌛️:")
                        .replyMarkup(timer));
                break;
            case "\uD83D\uDCA4Гафарган хкудун":
                dictionaryLanguage.remove(chatId);
                clickButtonsLezgi.remove(chatId);
                clickButtonsRus.remove(chatId);
                bot.execute(new SendMessage(chatId, "Гафарган хкудна"));
                break;
            case "\uD83C\uDF10ЧIал дегишрун":
                InlineKeyboardMarkup selectLanguag = new InlineKeyboardMarkup(
                        new InlineKeyboardButton[][]{
                                {new InlineKeyboardButton("Лезги чIал").callbackData(SELECT_LEZGI),
                                        new InlineKeyboardButton("Русский язык").callbackData(SELECT_RUS)}
                        });
                bot.execute(new SendMessage(chatId, "ЧIал хкягъа \uD83C\uDF10 Выберите язык")
                        .replyMarkup(selectLanguag));
                break;
            case "\uD83D\uDCDDБотдин къайдаяр":
                bot.execute(new SendMessage(chatId, "Ахпа кхьида"));
                break;
            case "✍\uD83C\uDFFCКъимет гун":
                bot.execute(new SendMessage(chatId, "<Телеграмдин ссылка>"));
                break;
        }
    }
}
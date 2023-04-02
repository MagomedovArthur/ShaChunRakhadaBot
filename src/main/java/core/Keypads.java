package core;

import com.pengrad.telegrambot.request.SendMessage;

import static core.DailyBot.*;

public class Keypads {
    public static void keypadButtonProcessing(long chatId, String userMessage) {
        /* Добавить лезгинкие слова   */
        switch (userMessage) {
            case "Инструкция\uD83D\uDCDD":
                bot.execute(new SendMessage(chatId, "Это инструкция, допишу потом"));
                break;
            case "Задать новое время отправки фраз\uD83D\uDD57":
                timeZoneFixation.remove(chatId);
                timeToSendMessages.remove(chatId);
                dictionaryLanguage.remove(chatId);
                bot.execute(new SendMessage(chatId, "Вы сбросили время, чтобы задать другое время отправки" +
                        "фраз. Введите ваше время сейчас:"));
                break;
            case "Остановить бота\uD83D\uDE45\uD83C\uDFFB\u200D♂️":
                timeZoneFixation.remove(chatId);
                timeToSendMessages.remove(chatId);
                bot.execute(new SendMessage(chatId, "Бот остановлен"));
                break;
            case "Написать отзыв✍\uD83C\uDFFC":

                break;
            case "Лезгинско-русский словарь\uD83D\uDCD7":
                dictionaryLanguage.put(chatId, LEZGI_RUS);
                bot.execute(new SendMessage(chatId, "Выбран Лезгинско-Русский словарь.\n\n" +
                        "Введите слово на лезгинском языке.\n" +
                        "Символ «I» (палочка: кI, тI, пI...) вводите через латинкую букву «i»."));

                break;
            case "Русско-лезгинский словарь\uD83D\uDCD5":
                dictionaryLanguage.put(chatId, RUS_LEZGI);
                bot.execute(new SendMessage(chatId, "Выбран Русско-Лезгинский словарь.\n " +
                        "\nВведите слово на русском языке."));
                break;
            case "Выключить словари\uD83D\uDCA4":
                bot.execute(new SendMessage(chatId, "Словари выключены"));
                dictionaryLanguage.remove(chatId);
                clickButtonsLezgi.remove(chatId);
                clickButtonsRus.remove(chatId);
                break;
            case "Изменить язык\uD83C\uDF10":

                break;
        }
    }
}

package core;

import java.util.List;

import static core.DailyBot.*;

public class IgnoredWords {

    public static List<String> listOfIgnoredWords = List.of(
            START, HELP, INFO, RESET_TIME, STOP_SEND, LEZGI_RUS, RUS_LEZGI, DICT_OFF,
            "инструкция\uD83D\uDCDD",
            "лезгинско-русский словарь\uD83D\uDCD7",
            "задать новое время отправки фраз\uD83D\uDD57",
            "русско-лезгинский словарь\uD83D\uDCD5",
            "остановить отправку фраз\uD83D\uDE45\uD83C\uDFFB\u200D♂️",
            "выключить словари\uD83D\uDCA4",
            "написать отзыв✍\uD83C\uDFFC",
            "изменить язык\uD83C\uDF10"
    );
}

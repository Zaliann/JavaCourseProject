package ru.croc;

/**
 * Данный класс необходим для удобного хранения карточек, в которых содержатся:
 * - слово на английском языке
 * - корректный перевод
 * - некорректный перевод
 * - некорректный перевод
 */
public class Card {
    private final String word;
    private final String option1;
    private final String option2;
    private final String option3;

    public Card(String word, String option1, String option2, String option3) {
        this.word = word;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
    }

    public String getWord() {
        return this.word;
    }

    public String getOption1Correct() {
        return this.option1;
    }

    public String getOption2Incorrect() {
        return this.option2;
    }

    public String getOption3Incorrect() {
        return this.option3;
    }
}

package ru.croc;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.function.BiFunction;
import static java.lang.Math.min;

public class CardConnection {
    private final Connection connection;
    public CardConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Данный метод считывает со стандартного потока ввода 4 слова для карточки:
     * - слово на английском языке
     * - корректный перевод
     * - некорректный перевод
     * - некорректный перевод
     * А затем записывает получившуюся карточку в таблицу `cards` в базе данных
     * @param scanner сканер, для считывания со стандартного потока ввода
     * @throws SQLException если возникли проблемы при работе с базой данных
     */
    public void createCard(Scanner scanner) throws SQLException {
        System.out.print("Word: ");
        String word = scanner.nextLine();

        System.out.print("Option 1/3 (correct): ");
        String option1 = scanner.nextLine();

        System.out.print("Option 2/3 (incorrect): ");
        String option2 = scanner.nextLine();

        System.out.print("Option 3/3 (incorrect): ");
        String option3 = scanner.nextLine();
        System.out.println();

        String sql = "insert into cards " +
                " (word, option1correct, option2incorrect, option3incorrect)" +
                " values (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, word);
            statement.setString(2, option1);
            statement.setString(3, option2);
            statement.setString(4, option3);

            statement.executeUpdate();
        }
    }

    /**
     * Данный метод позволяет просмотреть все имеющиеся в базе данных карточки
     * из таблицы `cards`. Карточки выводятся по одной
     * @param scanner сканер, для считывания со стандартного потока ввода
     * @throws SQLException если возникли проблемы при работе с базой данных
     */
    public void showCards(Scanner scanner) throws SQLException {
        String sql = "select * from cards";
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    System.out.println("Card " + resultSet.getInt("id") + ":");
                    System.out.println("Word: " + resultSet.getString("word"));

                    System.out.println("Option 1/3 (correct): " + resultSet.getString("option1correct"));
                    System.out.println("Option 2/3 (incorrect): " + resultSet.getString("option2incorrect"));
                    System.out.println("Option 3/3 (incorrect): " + resultSet.getString("option3incorrect"));

                    while (true) {
                        System.out.print("Show next [Y/n]: ");
                        String response = scanner.nextLine();
                        System.out.println();
                        if (response.equals("") || response.equals("Y")) {
                            break;
                        }
                        if (response.equals("n")) {
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Данный метод удаляет из таблицы `cards` карточку со значением `id`,
     * которое вводится со стандартного потока ввода
     * @param scanner сканер, для считывания со стандартного потока ввода
     * @throws SQLException если возникли проблемы при работе с базой данных
     */
    public void deleteCard(Scanner scanner) throws SQLException {
        System.out.print("Enter id of card: ");
        int cardId;
        try {
            cardId = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return;
        }

        String sql = "delete from cards c where c.id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, cardId);
            int result = statement.executeUpdate();
            if (result == 0) {
                System.out.println("No such card!");
            }
        }
        System.out.println();
    }

    public void createUserIfAbsent(String username) throws SQLException {
        String sql = "insert into users (username) select ? where not exists " +
                "(select username from users where username = ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, username);

            statement.executeUpdate();
        }
    }

    public int getUserId(String username) throws SQLException {
        String sql = "SELECT id \n" +
                     "FROM users \n" +
                     "WHERE username = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        }

        throw new RuntimeException("username does not exist: " + username);
    }

    /**
     * Данный метод генерирует тест, состоящий из 6 карточек,
     * для пользователя с `user_id`, равным `userId`,
     * на основе данных из таблицы `usercards`
     * @param userId ID пользователя (`user_id`)
     * @param scanner сканер, для считывания со стандартного потока ввода
     * @throws SQLException если возникли проблемы при работе с базой данных
     */
    public void runUserTesting(int userId, Scanner scanner) throws SQLException {
        ArrayList<Integer> selectedCardIds = selectCardIdsForTesting(userId);
        int amountOfCards = selectedCardIds.size();

        for (int iCard = 0; iCard < selectedCardIds.size(); iCard++) {
            Card card = getCardFromDatabase(selectedCardIds.get(iCard));
            System.out.format("[%d/%d] Translate word: %s\n", iCard + 1,
                    amountOfCards, card.getWord());

            ArrayList<String> optionsRandomized = getOptionsRandomized(card);
            for (int iOption = 0; iOption < 3; iOption++) {
                System.out.format("%d. %s\n", iOption + 1,
                        optionsRandomized.get(iOption));
            }

            int chosenOption;
            while(true) {
                System.out.print("Choose translation: ");
                try {
                    chosenOption = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    chosenOption = 0;
                }
                if (1 <= chosenOption && chosenOption <= 3) {
                    break;
                }
            }

            boolean isChoiceRight =
                    optionsRandomized.get(chosenOption - 1).equals(card.getOption1Correct());
            int shift;
            if (isChoiceRight) {
                shift = 1;
                System.out.println("Yes\n");
            } else {
                shift = -1;
                System.out.format("No, the correct translation is '%s'.\n\n",
                        card.getOption1Correct());
            }

            shiftBoxOfCard(userId, selectedCardIds.get(iCard), shift);
        }
    }

    private void shiftBoxOfCard(int userId, int cardId, int shift) throws SQLException {
        int boxOfCard = getBoxOfCard(userId, cardId);
        if (shift == 1 && boxOfCard == 3 || shift == -1 && boxOfCard == 1) {
            return;
        }
        boxOfCard += shift;

        String sql = "UPDATE usercards\n" +
                "SET box = ?\n" +
                "WHERE user_id = ? AND card_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, boxOfCard);
            statement.setInt(2, userId);
            statement.setInt(3, cardId);

            statement.executeUpdate();
        }
    }

    private int getBoxOfCard(int userId, int cardId) throws SQLException {
        String sql = "SELECT box \n" +
                "FROM usercards " +
                "WHERE user_id = ? AND card_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, cardId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while(resultSet.next()) {
                    return resultSet.getInt("box");
                }
            }
        }
        throw new RuntimeException("User with id " + userId + " doesn't have " +
                "card with id " + cardId);
    }

    /**
     * Данный метод расставляет в случайном порядке варианты ответа
     * @param card карточка слова
     * @return список с вариантами ответа, расставленными в случайном порядке
     */
    private ArrayList<String> getOptionsRandomized(Card card) {
        ArrayList<String> options = new ArrayList<>();
        options.add(card.getOption1Correct());
        options.add(card.getOption2Incorrect());
        options.add(card.getOption3Incorrect());

        ArrayList<String> optionsRandomized = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < 3; i++) {
            int randomNumberOfOption = rand.nextInt(options.size());
            optionsRandomized.add(options.get(randomNumberOfOption));
            options.remove(randomNumberOfOption);
        }
        return optionsRandomized;
    }

    private Card getCardFromDatabase(int cardId) throws SQLException {
        String sql = "SELECT word, option1correct, option2incorrect, option3incorrect\n" +
                     " FROM cards \n" +
                     "WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, cardId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while(resultSet.next()) {
                    return new Card(resultSet.getString("word"),
                            resultSet.getString("option1correct"),
                            resultSet.getString("option2incorrect"),
                            resultSet.getString("option3incorrect")
                    );
                }
            }
        }
        throw new RuntimeException("No card with id: " + cardId);
    }

    /**
     * Данный метод выбирает подходящие для теста карточки. Тест должен состоять
     * из 3 карточек из 1 коробки, 2 карточек из 2 коробки
     * и из 1 карточки из 3 коробки, согласно системе Лейтнера.
     * Кроме того, данный метод при запуске обновляет таблицу `usercards`.
     * Так сделано для того, чтобы добавленные карточки попадали в общий
     * пул всех карточек
     * @param userId ID пользователя (`user_id`)
     * @return список ID карточек, которые выбраны для теста
     * @throws SQLException если возникли проблемы при работе с базой данных
     */
    private ArrayList<Integer> selectCardIdsForTesting(int userId) throws SQLException {
        String sql = "INSERT INTO usercards (user_id, card_id) \n" +
                "SELECT ?, id \n" +
                "FROM cards \n" +
                "WHERE id NOT IN (\n" +
                "\tSELECT card_id FROM usercards WHERE user_id = ?\n" +
                ")";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, userId);

            statement.executeUpdate();
        }

        LinkedList<Integer> cardsFromBox1 = findCardsFromBox(userId, 1, 6);
        LinkedList<Integer> cardsFromBox2 = findCardsFromBox(userId, 2, 6);
        LinkedList<Integer> cardsFromBox3 = findCardsFromBox(userId, 3, 6);

        int countSelected =
                min(cardsFromBox1.size() + cardsFromBox2.size() + cardsFromBox3.size(), 6);
        ArrayList<Integer> selectedCardIds = new ArrayList<>();

        BiFunction<LinkedList<Integer>, Integer, Void> addFromBox = (box,
                                                                     countBox) -> {
            int count = min(countSelected - selectedCardIds.size(),
                    min(box.size(), countBox));
            for(int i = 0; i < count; i++) {
                selectedCardIds.add(box.remove());
            }
            return null;
        };

        while (selectedCardIds.size() != countSelected) {
            addFromBox.apply(cardsFromBox1, 3);
            addFromBox.apply(cardsFromBox2, 2);
            addFromBox.apply(cardsFromBox3, 1);
        }
        return selectedCardIds;
    }

    private LinkedList<Integer> findCardsFromBox(int userId, int box,
                                                 int limit) throws SQLException {
        String sql = "SELECT card_id\n" +
                     "FROM usercards\n" +
                     "WHERE user_id = ? AND box = ?\n" +
                     "ORDER BY card_id\n" +
                     "LIMIT ?";

        LinkedList<Integer> cardsFromBox = new LinkedList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, box);
            statement.setInt(3, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    cardsFromBox.add(resultSet.getInt("card_id"));
                }
            }
        }
        return cardsFromBox;
    }

    /**
     * Данный метод сохраняет результаты пользователя в файл
     * с расширением .xml, название которого создаётся следующим образом:
     * `username` + "Result.xml"
     * @param username имя пользователя
     * @param userId ID пользователя (`user_id`)
     * @throws SQLException если возникли проблемы при работе с базой данных
     * @throws JAXBException если возникли проблемы при маршализации
     * @throws IOException если возникли проблемы при выводе в файл
     */
    public void showResults(String username, int userId) throws SQLException,
            JAXBException, IOException {
        ArrayList<Result> results = getResults(userId);

        JAXBContext context = JAXBContext.newInstance(Result.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);

        FileWriter writer = new FileWriter(username + "Results.xml");
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        for (int iResult = 0; iResult < results.size(); iResult++) {
            marshaller.marshal(results.get(iResult), bufferedWriter);
        }
        writer.close();
    }

    private ArrayList<Result> getResults(int userId) throws SQLException {
        ArrayList<Result> results = new ArrayList<>();

        String sql = "SELECT cards.word, usercards.box \n" +
                "FROM cards, usercards\n" +
                "WHERE usercards.user_id = ? AND cards.id = usercards.card_id;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while(resultSet.next()) {
                    results.add(createResult(resultSet.getString("word"),
                            resultSet.getInt("box")));
                }
            }
        }
        return results;
    }

    private Result createResult(String word, int box) {
        Result result = new Result();
        result.word = word;
        result.box = box;
        return result;
    }
}
